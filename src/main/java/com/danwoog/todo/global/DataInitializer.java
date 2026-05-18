package com.danwoog.todo.global;

import com.danwoog.todo.domain.todo.Todo;
import com.danwoog.todo.domain.todo.TodoAssignee;
import com.danwoog.todo.domain.todogroup.TodoGroup;
import com.danwoog.todo.domain.todogroup.TodoGroupMember;
import com.danwoog.todo.repository.*;
import com.danwoog.todo.repository.todo.TodoAssigneeRepository;
import com.danwoog.todo.repository.todo.TodoRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final TodoGroupRepository todoGroupRepository;
    private final TodoRepository todoRepository;
    private final TodoAssigneeRepository todoAssigneeRepository;

    @Override
    public void run(String... args) throws Exception {
        // 더미 데이터 생성
        TodoGroupMember member = new TodoGroupMember("서준영");
        member.updateNote("오늘 해야 할 일을 정리해보세요.");
        memberRepository.save(member);

        TodoGroup group = new TodoGroup("단웅의 전설");
        todoGroupRepository.save(group);

        // 진행 중인 할 일
        Todo todo1 = Todo.builder()
                .title("API 명세서 작성")
                .group(group)
                .deadline(LocalDate.of(2026, 5, 10))
                .category("개발")
                .garlicReward(5)
                .priority(TodoPriority.HIGH)
                .description("API 명세서 꼼꼼히 작성하기")
                .build();
        todoRepository.save(todo1);
        
        TodoAssignee assignee1 = TodoAssignee.builder().todo(todo1).member(member).build();
        todoAssigneeRepository.save(assignee1);

        // 완료된 할 일
        Todo todo2 = Todo.builder()
                .title("ERD 작성")
                .group(group)
                .deadline(LocalDate.of(2026, 5, 2))
                .category("개발")
                .garlicReward(3)
                .priority(TodoPriority.MEDIUM)
                .description("데이터베이스 설계")
                .build();
        todoRepository.save(todo2);
        
        TodoAssignee assignee2 = TodoAssignee.builder().todo(todo2).member(member).build();
        assignee2.complete();
        todoAssigneeRepository.save(assignee2);
        
        // 추가 할 일 (통계용)
        Todo todo3 = Todo.builder()
                .title("발표자료(PPT) 제작")
                .group(group)
                .deadline(LocalDate.of(2026, 5, 30))
                .category("발표 준비")
                .garlicReward(4)
                .priority(TodoPriority.HIGH)
                .description("발표자료 준비")
                .build();
        todoRepository.save(todo3);
        TodoAssignee assignee3 = TodoAssignee.builder().todo(todo3).member(member).build();
        todoAssigneeRepository.save(assignee3);
    }
}
