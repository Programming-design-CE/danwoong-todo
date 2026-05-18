package com.danwoog.todo.global;

import com.danwoog.todo.domain.todo.Todo;
import com.danwoog.todo.domain.todo.TodoAssignee;
import com.danwoog.todo.domain.todogroup.TodoGroup;
import com.danwoog.todo.domain.todogroup.TodoGroupMember;
import com.danwoog.todo.domain.todogroup.GroupMemberRole;
import com.danwoog.todo.domain.todogroup.Priority;
import com.danwoog.todo.domain.user.User;
import com.danwoog.todo.repository.*;
import com.danwoog.todo.repository.todo.TodoAssigneeRepository;
import com.danwoog.todo.repository.todo.TodoRepository;
import com.danwoog.todo.repository.user.UserRepository;
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

        // 이미 더미 유저가 있으면 더미 데이터를 다시 넣지 않음
        // H2 file DB를 사용하면 서버를 재시작해도 데이터가 남아있기 때문에,
        // 중복 insert를 막아줘야 함
        if (userRepository.existsByLoginId("test")) {
            return;
        }

        // 더미 사용자 생성
        User user = new User("test", "1234", "서준영");
        user.setPersonalNote("오늘 해야 할 일을 정리해보세요.");
        userRepository.save(user);

        // 더미 그룹 생성
        TodoGroup group = new TodoGroup("단웅의 전설", "단웅의 전설 그룹", user);
        todoGroupRepository.save(group);

        // 그룹 생성자를 LEADER로 등록
        TodoGroupMember member = new TodoGroupMember(group, user, GroupMemberRole.LEADER);
        memberRepository.save(member);

        // 진행 중인 할 일
        Todo todo1 = new Todo(group, "API 명세서 작성", "API 명세서 꼼꼼히 작성하기", user);
        todo1.setDeadline(LocalDateTime.of(2026, 5, 10, 23, 59));
        todo1.setCategory("개발");
        todo1.setGarlicReward(5);
        todo1.setPriority(Priority.HIGH);
        todoRepository.save(todo1);

        // todo1 담당자 지정
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

        // todo2 담당자 지정
        TodoAssignee assignee2 = new TodoAssignee(todo2, user);
        todoAssigneeRepository.save(assignee2);

        // 추가 할 일
        Todo todo3 = new Todo(group, "발표자료(PPT) 제작", "발표자료 준비", user);
        todo3.setDeadline(LocalDateTime.of(2026, 5, 30, 23, 59));
        todo3.setCategory("발표 준비");
        todo3.setGarlicReward(4);
        todo3.setPriority(Priority.HIGH);
        todoRepository.save(todo3);

        // todo3 담당자 지정
        TodoAssignee assignee3 = new TodoAssignee(todo3, user);
        todoAssigneeRepository.save(assignee3);
    }
}