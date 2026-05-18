package com.danwoog.todo.global;

import com.danwoog.todo.domain.todo.Todo;
import com.danwoog.todo.domain.todo.TodoAssignee;
import com.danwoog.todo.domain.todogroup.TodoGroup;
import com.danwoog.todo.domain.todogroup.TodoGroupMember;
import com.danwoog.todo.domain.todogroup.GroupMemberRole;
import com.danwoog.todo.domain.todogroup.Priority;
import com.danwoog.todo.domain.user.User;
import com.danwoog.todo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final TodoGroupRepository todoGroupRepository;
    private final TodoRepository todoRepository;
    private final TodoAssigneeRepository todoAssigneeRepository;
    private final UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        // 더미 데이터 생성
        User user = new User("test", "1234", "서준영");
        user.setPersonalNote("오늘 해야 할 일을 정리해보세요.");
        userRepository.save(user);

        TodoGroup group = new TodoGroup("단웅의 전설", "단웅의 전설 그룹", user);
        todoGroupRepository.save(group);

        TodoGroupMember member = new TodoGroupMember(group, user, GroupMemberRole.LEADER);
        memberRepository.save(member);

        // 진행 중인 할 일
        Todo todo1 = new Todo(group, "API 명세서 작성", "API 명세서 꼼꼼히 작성하기", user);
        todo1.setDeadline(LocalDateTime.of(2026, 5, 10, 23, 59));
        todo1.setCategory("개발");
        todo1.setGarlicReward(5);
        todo1.setPriority(Priority.HIGH);
        todoRepository.save(todo1);
        
        TodoAssignee assignee1 = new TodoAssignee(todo1, user);
        todoAssigneeRepository.save(assignee1);

        // 완료된 할 일
        Todo todo2 = new Todo(group, "ERD 작성", "데이터베이스 설계", user);
        todo2.setDeadline(LocalDateTime.of(2026, 5, 2, 23, 59));
        todo2.setCategory("개발");
        todo2.setGarlicReward(3);
        todo2.setPriority(Priority.MEDIUM);
        todo2.complete(user);
        todoRepository.save(todo2);
        
        TodoAssignee assignee2 = new TodoAssignee(todo2, user);
        todoAssigneeRepository.save(assignee2);
        
        // 추가 할 일 (통계용)
        Todo todo3 = new Todo(group, "발표자료(PPT) 제작", "발표자료 준비", user);
        todo3.setDeadline(LocalDateTime.of(2026, 5, 30, 23, 59));
        todo3.setCategory("발표 준비");
        todo3.setGarlicReward(4);
        todo3.setPriority(Priority.HIGH);
        todoRepository.save(todo3);

        TodoAssignee assignee3 = new TodoAssignee(todo3, user);
        todoAssigneeRepository.save(assignee3);
    }
}
