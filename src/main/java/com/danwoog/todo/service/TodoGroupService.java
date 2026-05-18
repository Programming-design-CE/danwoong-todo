package com.danwoog.todo.service;

import com.danwoog.todo.domain.todogroup.GroupMemberRole;
import com.danwoog.todo.domain.todogroup.TodoGroup;
import com.danwoog.todo.domain.todogroup.TodoGroupMember;
import com.danwoog.todo.domain.user.User;
import com.danwoog.todo.dto.todogroup.MemberPreviewResponse;
import com.danwoog.todo.dto.todogroup.TodoGroupCreateRequest;
import com.danwoog.todo.dto.todogroup.TodoGroupCreateResponse;
import com.danwoog.todo.dto.todogroup.TodoGroupListResponse;
import com.danwoog.todo.dto.todogroup.TodoGroupSummaryResponse;
import com.danwoog.todo.repository.MemberRepository;
import com.danwoog.todo.repository.TodoGroupRepository;
import com.danwoog.todo.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TodoGroupService {

    private final TodoGroupRepository todoGroupRepository;
    private final MemberRepository todoGroupMemberRepository;
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

        todoGroupMemberRepository.save(leaderMember);

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

    public TodoGroupListResponse getMyGroups(Long userId) {

        List<TodoGroupMember> members =
                todoGroupMemberRepository.findByUser_UserId(userId);

        List<TodoGroupSummaryResponse> groups = members.stream()
                .map(member -> {
                    TodoGroup group = member.getGroup();

                    List<MemberPreviewResponse> previews = List.of(
                            new MemberPreviewResponse(userId, null)
                    );

                    return new TodoGroupSummaryResponse(
                            group.getGroupId(),
                            group.getGroupName(),
                            group.getDeadline(),
                            group.getPriority(),
                            group.getStatus(),
                            previews,
                            1
                    );
                })
                .toList();

        return new TodoGroupListResponse(groups);
    }
}