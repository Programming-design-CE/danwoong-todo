package com.danwoog.todo.service;

import com.danwoog.todo.domain.todogroup.*;
import com.danwoog.todo.domain.user.User;
import com.danwoog.todo.dto.todogroup.*;
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


    // 1. POST : 공동 할 일 그룹 생성 및 멤버 바로 추가
    public TodoGroupCreateResponse createGroup(Long loginUserId, TodoGroupCreateRequest request) {

        // 로그인 사용자 조회 (그룹 생성자)
        User leader = userRepository.findById(loginUserId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 그룹 생성
        TodoGroup group = new TodoGroup(
                request.getGroupName(),
                null,
                request.getDeadline().atStartOfDay(),
                Priority.valueOf(request.getPriority()),
                leader
        );

        TodoGroup savedGroup = todoGroupRepository.save(group);

        // 생성자를 LEADER 권한으로 그룹에 추가
        todoGroupMemberRepository.save(
                new TodoGroupMember(
                        savedGroup,
                        leader,
                        GroupMemberRole.LEADER
                )
        );

        // invitee_ids 사용자들을 MEMBER 권한으로 그룹에 추가
        if (request.getInviteeIds() != null) {
            request.getInviteeIds().stream()

                    // 생성자 자기 자신은 제외
                    .filter(inviteeId -> !inviteeId.equals(loginUserId))

                    // 중복 초대 제거
                    .distinct()

                    .forEach(inviteeId -> {
                        User invitee = userRepository.findById(inviteeId)
                                .orElseThrow(() ->
                                        new IllegalArgumentException("초대할 사용자를 찾을 수 없습니다.")
                                );

                        todoGroupMemberRepository.save(
                                new TodoGroupMember(
                                        savedGroup,
                                        invitee,
                                        GroupMemberRole.MEMBER
                                )
                        );
                    });
        }

        // 현재 그룹 멤버 전체 조회
        List<TodoGroupMember> groupMembers =
                todoGroupMemberRepository.findByGroup_GroupId(savedGroup.getGroupId());
        savedGroup.initializeGarlicBudget(groupMembers.size());

        // 응답에 넣을 전체 멤버 목록 생성
        List<MemberPreviewResponse> members = groupMembers.stream()
                .map(groupMember -> new MemberPreviewResponse(
                        groupMember.getUser().getUserId(),
                        groupMember.getUser().getProfileImage()
                ))
                .toList();

        // 생성된 그룹 정보 반환
        return new TodoGroupCreateResponse(
                savedGroup.getGroupId(),
                savedGroup.getGroupName(),
                savedGroup.getDeadline().toLocalDate(),
                savedGroup.getPriority(),
                savedGroup.getStatus(),
                savedGroup.getTotalGarlicReward(),
                savedGroup.getRemainingGarlicReward(),
                members,
                groupMembers.size()
        );
    }


    // 2. GET : 내가 속한 공동 할 일 그룹 목록 조회
    public TodoGroupListResponse getMyGroups(Long userId) {

        // 현재 로그인한 사용자가 속한 그룹 멤버십 조회
        List<TodoGroupMember> myMemberships =
                todoGroupMemberRepository.findByUser_UserId(userId);

        List<TodoGroupSummaryResponse> groups = myMemberships.stream()

                // 삭제되지 않은 그룹만 조회
                .filter(myMember ->
                        myMember.getGroup().getStatus() != GroupStatus.DELETED
                )

                .map(myMember -> {
                    TodoGroup group = myMember.getGroup();

                    // 해당 그룹의 전체 멤버 조회
                    List<TodoGroupMember> groupMembers =
                            todoGroupMemberRepository.findByGroup_GroupId(group.getGroupId());

                    // 응답에 넣을 전체 멤버 목록 생성
                    List<MemberPreviewResponse> members = groupMembers.stream()
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
                            group.getTotalGarlicReward(),
                            group.getRemainingGarlicReward(),
                            members,
                            groupMembers.size()
                    );
                })
                .toList();

        return new TodoGroupListResponse(groups);
    }


    // 3. PATCH : 그룹 정보 수정
    public TodoGroupUpdateResponse updateGroup(
            Long userId,
            Long groupId,
            TodoGroupUpdateRequest request
    ) {

        // 수정할 그룹 조회
        TodoGroup group = todoGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));

        // 그룹 정보 수정
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
                group.getStatus(),
                group.getTotalGarlicReward(),
                group.getRemainingGarlicReward()
        );
    }


    // 4. DELETE : 그룹 삭제
    public TodoGroupDeleteResponse deleteGroup(Long userId, Long groupId) {

        // 삭제할 그룹 조회
        TodoGroup group = todoGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));

        // soft delete 처리
        group.delete();

        return new TodoGroupDeleteResponse(
                groupId,
                "DELETED"
        );
    }


    // 5. POST : 그룹 멤버 추가
    public TodoGroupInviteResponse inviteMembers(
            Long loginUserId,
            Long groupId,
            TodoGroupInviteRequest request
    ) {

        // 멤버를 추가할 그룹 조회
        TodoGroup group = todoGroupRepository.findById(groupId)
                .orElseThrow(() ->
                        new IllegalArgumentException("그룹을 찾을 수 없습니다.")
                );

        int invitedCount = 0;

        if (request.getMemberIds() != null) {

            for (Long memberId : request.getMemberIds()) {

                // 자기 자신은 이미 그룹에 속해 있을 가능성이 높으므로 제외
                if (memberId.equals(loginUserId)) {
                    continue;
                }

                // 추가할 사용자 조회
                User member = userRepository.findById(memberId)
                        .orElseThrow(() ->
                                new IllegalArgumentException("사용자를 찾을 수 없습니다.")
                        );

                // 이미 그룹 멤버인지 확인
                boolean alreadyMember =
                        todoGroupMemberRepository.existsByGroup_GroupIdAndUser_UserId(
                                groupId,
                                memberId
                        );

                // 이미 멤버라면 중복 추가하지 않음
                if (alreadyMember) {
                    continue;
                }

                // 그룹 멤버로 추가
                todoGroupMemberRepository.save(
                        new TodoGroupMember(
                                group,
                                member,
                                GroupMemberRole.MEMBER
                        )
                );

                invitedCount++;
            }
        }

        group.increaseGarlicBudget(invitedCount);

        return new TodoGroupInviteResponse(
                groupId,
                invitedCount
        );
    }
}
