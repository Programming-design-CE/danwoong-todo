package com.danwoog.todo.service;

import com.danwoog.todo.domain.todogroup.GroupMemberRole;
import com.danwoog.todo.domain.todogroup.TodoGroup;
import com.danwoog.todo.domain.todogroup.TodoGroupMember;
import com.danwoog.todo.domain.user.User;
import com.danwoog.todo.dto.todogroup.TodoGroupCreateRequest;
import com.danwoog.todo.dto.todogroup.TodoGroupCreateResponse;
import com.danwoog.todo.repository.MemberRepository;
import com.danwoog.todo.repository.TodoGroupRepository;
import com.danwoog.todo.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TodoGroupService {

    private final TodoGroupRepository todoGroupRepository;
    private final MemberRepository MemberRepository;
    private final UserRepository userRepository;

    public TodoGroupCreateResponse createGroup(Long loginUserId, TodoGroupCreateRequest request) {

        User leader = userRepository.findById(loginUserId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        TodoGroup group = new TodoGroup(
                request.getGroupName(),
                null,
                leader
        );

        TodoGroup savedGroup = todoGroupRepository.save(group);

        TodoGroupMember leaderMember = new TodoGroupMember(
                savedGroup,
                leader,
                GroupMemberRole.LEADER
        );

        MemberRepository.save(leaderMember);

        int invitationCount = request.getInviteeIds() == null
                ? 0
                : request.getInviteeIds().size();

        return new TodoGroupCreateResponse(
                savedGroup.getGroupId(),
                leader.getUserId(),
                GroupMemberRole.LEADER,
                invitationCount
        );
    }
}