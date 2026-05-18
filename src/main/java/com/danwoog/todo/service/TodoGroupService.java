package com.danwoog.todo.service;

import com.danwoog.todo.domain.todogroup.GroupMemberRole;
import com.danwoog.todo.domain.todogroup.Priority;
import com.danwoog.todo.domain.todogroup.TodoGroup;
import com.danwoog.todo.domain.todogroup.TodoGroupMember;
import com.danwoog.todo.domain.user.User;
import com.danwoog.todo.domain.todogroup.GroupStatus;
import com.danwoog.todo.dto.todogroup.TodoGroupUpdateRequest;
import com.danwoog.todo.dto.todogroup.TodoGroupUpdateResponse;
import com.danwoog.todo.dto.todogroup.MemberPreviewResponse;
import com.danwoog.todo.dto.todogroup.TodoGroupCreateRequest;
import com.danwoog.todo.dto.todogroup.TodoGroupCreateResponse;
import com.danwoog.todo.dto.todogroup.TodoGroupListResponse;
import com.danwoog.todo.dto.todogroup.TodoGroupSummaryResponse;
import com.danwoog.todo.dto.todogroup.TodoGroupDeleteResponse;
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

    // 1. POST : 공동 할 일 그룹 생성 및 친구 초대
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



    // 2. 내가 속한 공동 할 일 그룹 목록 조회
    public TodoGroupListResponse getMyGroups(Long userId) {

        List<TodoGroupMember> myMemberships =
                todoGroupMemberRepository.findByUser_UserId(userId);

        List<TodoGroupSummaryResponse> groups = myMemberships.stream()
                .filter(myMember ->
                        myMember.getGroup().getStatus() != GroupStatus.DELETED
                )
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


        public TodoGroupUpdateResponse updateGroup(Long userId, Long groupId, TodoGroupUpdateRequest request) {

                TodoGroup group = todoGroupRepository.findById(groupId)
                        .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));

                group.update(
                        request.getGroupName(),
                        request.getDeadline().atStartOfDay(),
                        Priority.valueOf(request.getPriority()),
                        GroupStatus.valueOf(request.getStatus())
                );

                return new TodoGroupUpdateResponse(
                        group.getGroupId(),
                        group.getGroupName(),
                        group.getDeadline().toLocalDate(),
                        group.getPriority(),
                        group.getStatus()
                );
        }






        // 4. 그룹 삭제
        public TodoGroupDeleteResponse deleteGroup(Long userId, Long groupId) {

                TodoGroup group = todoGroupRepository.findById(groupId)
                        .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));

                group.delete();

                return new TodoGroupDeleteResponse(groupId, "DELETED" );
        }
}