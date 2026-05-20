package com.danwoog.todo.service;

import com.danwoog.todo.domain.todo.TodoAssignee;
import com.danwoog.todo.domain.todo.TodoCategory;
import com.danwoog.todo.domain.todo.TodoStatus;
import com.danwoog.todo.domain.user.User;
import com.danwoog.todo.dto.note.MyNoteRequest;
import com.danwoog.todo.dto.note.MyNoteResponse;
import com.danwoog.todo.dto.todo.CategorySummaryDto;
import com.danwoog.todo.dto.todo.MyCompletedTodoDto;
import com.danwoog.todo.dto.todo.MyCompletedTodoResponse;
import com.danwoog.todo.dto.todo.MyTodoDto;
import com.danwoog.todo.dto.todo.MyTodoResponse;
import com.danwoog.todo.dto.todo.MyTodoStatisticsResponse;
import com.danwoog.todo.repository.todo.TodoAssigneeRepository;
import com.danwoog.todo.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PersonalTodoService {
    private final UserRepository userRepository;
    private final TodoAssigneeRepository todoAssigneeRepository;

    public MyTodoResponse getMyTodos(Long memberId) {
        List<TodoAssignee> assignees = todoAssigneeRepository.findByMemberIdAndStatusWithTodoAndGroup(memberId, TodoStatus.IN_PROGRESS);
        List<MyTodoDto> dtos = assignees.stream().map(MyTodoDto::new).collect(Collectors.toList());
        return new MyTodoResponse(dtos);
    }

    public MyCompletedTodoResponse getMyCompletedTodos(Long memberId) {
        List<TodoAssignee> assignees = todoAssigneeRepository.findByMemberIdAndStatusWithTodoAndGroup(memberId, TodoStatus.COMPLETED);
        List<MyCompletedTodoDto> dtos = assignees.stream().map(MyCompletedTodoDto::new).collect(Collectors.toList());
        return new MyCompletedTodoResponse(dtos);
    }

    public MyNoteResponse getMyNote(Long memberId) {
        User user = userRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return new MyNoteResponse(user.getPersonalNote());
    }

    @Transactional
    public void updateMyNote(Long memberId, MyNoteRequest request) {
        User user = userRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        user.setPersonalNote(request.getContent());
    }

    public MyTodoStatisticsResponse getMyStatistics(Long memberId) {
        List<TodoAssignee> allAssignees = todoAssigneeRepository.findByMemberIdWithTodo(memberId);

        int totalTodos = allAssignees.size();
        int completedTodos = (int) allAssignees.stream()
                .filter(a -> a.getTodo().getStatus() == TodoStatus.COMPLETED)
                .count();
        int progressRate = totalTodos == 0 ? 0 : (int) Math.round((double) completedTodos / totalTodos * 100);

        int expectedGarlic = allAssignees.stream()
                .filter(a -> a.getTodo().getStatus() == TodoStatus.IN_PROGRESS)
                .mapToInt(a -> a.getTodo().getGarlicReward() != null ? a.getTodo().getGarlicReward() : 0)
                .sum();

        Map<TodoCategory, List<TodoAssignee>> byCategory = allAssignees.stream()
                .filter(a -> a.getTodo().getCategory() != null)
                .collect(Collectors.groupingBy(a -> a.getTodo().getCategory()));

        List<CategorySummaryDto> categorySummaries = byCategory.entrySet().stream()
                .map(entry -> {
                    TodoCategory category = entry.getKey();
                    int total = entry.getValue().size();
                    int completed = (int) entry.getValue().stream()
                            .filter(a -> a.getTodo().getStatus() == TodoStatus.COMPLETED)
                            .count();
                    return new CategorySummaryDto(category, total, completed);
                })
                .collect(Collectors.toList());

        return MyTodoStatisticsResponse.builder()
                .progress_rate(progressRate)
                .expected_garlic(expectedGarlic)
                .category_summary(categorySummaries)
                .build();
    }
}
