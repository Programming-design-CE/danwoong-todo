package com.danwoog.todo.service;

import com.danwoog.todo.domain.todo.Todo;
import com.danwoog.todo.domain.todo.TodoStatus;
import com.danwoog.todo.domain.todogroup.GroupStatus;
import com.danwoog.todo.dto.calendar.CalendarDailyResponse;
import com.danwoog.todo.dto.calendar.CalendarDayDto;
import com.danwoog.todo.dto.calendar.CalendarMonthlyResponse;
import com.danwoog.todo.dto.calendar.CalendarTodoDto;
import com.danwoog.todo.repository.todo.TodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CalendarService {

    private final TodoRepository todoRepository;

    public CalendarDailyResponse getDailyTodos(Long userId, LocalDate date, String viewMode) {
        List<Todo> todos = getCalendarTodos(
                userId,
                date.atStartOfDay(),
                date.atTime(23, 59, 59),
                viewMode
        );

        List<CalendarTodoDto> todoDtos = todos.stream()
                .sorted(Comparator
                        .comparing(Todo::getDeadline, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Todo::getTodoName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .map(this::toCalendarTodoDto)
                .collect(Collectors.toList());

        return new CalendarDailyResponse(date.toString(), todoDtos.size(), todoDtos);
    }

    public CalendarMonthlyResponse getMonthlyTodos(Long userId, int year, int month, String viewMode) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Todo> todos = getCalendarTodos(
                userId,
                startDate.atStartOfDay(),
                endDate.atTime(23, 59, 59),
                viewMode
        );

        Map<LocalDate, List<CalendarTodoDto>> todosByDate = todos.stream()
                .filter(todo -> todo.getDeadline() != null)
                .sorted(Comparator
                        .comparing(Todo::getDeadline, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Todo::getTodoName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .collect(Collectors.groupingBy(
                        todo -> todo.getDeadline().toLocalDate(),
                        Collectors.mapping(this::toCalendarTodoDto, Collectors.toList())
                ));

        List<CalendarDayDto> days = todosByDate.entrySet().stream()
                .map(entry -> new CalendarDayDto(entry.getKey().toString(), entry.getValue().size(), entry.getValue()))
                .sorted((d1, d2) -> d1.getDate().compareTo(d2.getDate()))
                .collect(Collectors.toList());

        return new CalendarMonthlyResponse(year, month, days);
    }

    private List<Todo> getCalendarTodos(Long userId, LocalDateTime start, LocalDateTime end, String viewMode) {
        if ("group".equalsIgnoreCase(viewMode)) {
            return todoRepository.findCalendarTodosByGroupMemberAndDeadlineBetween(
                    userId,
                    start,
                    end,
                    GroupStatus.DELETED
            );
        }

        return todoRepository.findCalendarTodosByUserIdAndDeadlineBetween(
                userId,
                start,
                end,
                GroupStatus.DELETED
        );
    }

    private CalendarTodoDto toCalendarTodoDto(Todo todo) {
        return CalendarTodoDto.builder()
                .todoId(todo.getTodoId())
                .groupId(todo.getGroup() != null ? todo.getGroup().getGroupId() : null)
                .groupColor(todo.getGroup() != null ? todo.getGroup().getGroupColor() : null)
                .title(todo.getTodoName())
                .date(todo.getDeadline() != null ? todo.getDeadline().toLocalDate().toString() : null)
                .completed(todo.getStatus() == TodoStatus.COMPLETED)
                .groupName(todo.getGroup() != null ? todo.getGroup().getGroupName() : null)
                .category(todo.getCategory())
                .priority(todo.getPriority() != null ? todo.getPriority().name() : null)
                .build();
    }
}
