package com.danwoog.todo.global;

import com.danwoog.todo.domain.todo.*;
import com.danwoog.todo.domain.user.User;
import com.danwoog.todo.repository.TodoAssigneeRepository;
import com.danwoog.todo.repository.TodoGroupRepository;
import com.danwoog.todo.repository.TodoRepository;
import com.danwoog.todo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final TodoGroupRepository todoGroupRepository;
    private final TodoRepository todoRepository;
    private final TodoAssigneeRepository todoAssigneeRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        User user = User.builder()
                .loginId("testuser")
                .password("password")
                .nickname("서준영")
                .build();
        userRepository.save(user);

        TodoGroup group1 = new TodoGroup("문해프 프로젝트");
        todoGroupRepository.save(group1);

        Todo todo1 = Todo.builder()
                .group(group1)
                .todoName("API 명세서 작성")
                .description("할 일 1 설명")
                .deadline(LocalDate.of(2026, 5, 2).atStartOfDay())
                .garlicReward(5)
                .priority(TodoPriority.HIGH.name())
                .category("문서 작업")
                .build();
                
        Todo todo2 = Todo.builder()
                .group(group1)
                .todoName("ERD 작성")
                .description("할 일 2 설명")
                .deadline(LocalDate.of(2026, 5, 10).atStartOfDay())
                .garlicReward(3)
                .priority(TodoPriority.MEDIUM.name())
                .category("설계")
                .build();

        Todo todo3 = Todo.builder()
                .group(group1)
                .todoName("발표자료(PPT) 제작")
                .description("할 일 3 설명")
                .deadline(LocalDate.of(2026, 5, 30).atStartOfDay())
                .garlicReward(10)
                .priority(TodoPriority.HIGH.name())
                .category("발표 준비")
                .build();

        todoRepository.save(todo1);
        todoRepository.save(todo2);
        todoRepository.save(todo3);

        TodoAssignee assignee1 = TodoAssignee.builder()
                .todo(todo1)
                .user(user)
                .build();

        TodoAssignee assignee2 = TodoAssignee.builder()
                .todo(todo2)
                .user(user)
                .build();

        TodoAssignee assignee3 = TodoAssignee.builder()
                .todo(todo3)
                .user(user)
                .build();

        todoAssigneeRepository.save(assignee1);
        todoAssigneeRepository.save(assignee2);
        todoAssigneeRepository.save(assignee3);
    }
}
