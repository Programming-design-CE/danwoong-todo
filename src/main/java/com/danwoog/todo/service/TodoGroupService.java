package com.danwoog.todo.service;

import com.danwoog.todo.domain.todogroup.GroupMemberRole;
import com.danwoog.todo.domain.todogroup.Priority;
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
                request.getDeadline().atStartOfDay(),
                Priority.valueOf(request.getPriority()),
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

        List<TodoGroupMember> myMemberships =
                todoGroupMemberRepository.findByUser_UserId(userId);

        List<TodoGroupSummaryResponse> groups = myMemberships.stream()
                .map(myMember -> {
                        TodoGroup group = myMember.getGroup();

                        List<TodoGroupMember> groupMembers =
                                todoGroupMemberRepository.findByGroup_GroupId(group.getGroupId());

                        List<MemberPreviewResponse> previews = groupMembers.stream()
                                .limit(2)
                                .map(groupMember -> {
                                User memberUser = groupMember.getUser();

                                return new MemberPreviewResponse(
                                        memberUser.getUserId(),
                                        memberUser.getProfileImage()
                                );
                                })
                                .toList();

                        return new TodoGroupSummaryResponse(
                                group.getGroupId(),
                                group.getGroupName(),
                                group.getDeadline().toLocalDate(),
                                group.getPriority(),
                                group.getStatus(),
                                previews,
                                groupMembers.size()
                        );
                })
                .toList();

        return new TodoGroupListResponse(groups);
        }
}