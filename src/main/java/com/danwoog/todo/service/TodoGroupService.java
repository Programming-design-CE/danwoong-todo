package com.danwoog.todo.service;

import com.danwoog.todo.domain.todogroup.*;
import com.danwoog.todo.domain.user.User;
import com.danwoog.todo.dto.todogroup.*;
import com.danwoog.todo.repository.MemberRepository;
import com.danwoog.todo.repository.TodoGroupRepository;
import com.danwoog.todo.repository.todo.TodoRepository;
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
    private final TodoRepository todoRepository;


    // 1. POST : 공동 할 일 그룹 생성 및 멤버 바로 추가
    public TodoGroupCreateResponse createGroup(Long loginUserId, TodoGroupCreateRequest request) {

        // 로그인 사용자 조회 (그룹 생성자)
        User leader = userRepository.findById(loginUserId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 그룹 생성
        TodoGroup group = new TodoGroup(
                request.getGroupName(),
                null,
                request.getDeadline() != null ? request.getDeadline().atStartOfDay() : null,
                Priority.valueOf(request.getPriority()),
                leader
        );
        group.setGroupColor(parseGroupColor(request.getGroupIconUrl()));
        group.setGroupCategory(normalizeGroupCategory(request.getGroupCategory()));

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

        // 마늘 보상 세팅 (사용자 입력 값이 없으면 기본값으로 0 또는 멤버 수 기준)
        if (request.getTotalGarlicReward() != null) {
            savedGroup.setGarlicBudget(request.getTotalGarlicReward());
        } else {
            savedGroup.initializeGarlicBudget(groupMembers.size());
        }

        // 응답에 넣을 전체 멤버 목록 생성
        List<MemberPreviewResponse> members = groupMembers.stream()
                .map(groupMember -> new MemberPreviewResponse(
                        groupMember.getUser().getUserId(),
                        groupMember.getUser().getNickname(),
                        groupMember.getUser().getProfileImage()
                ))
                .toList();

        // 생성된 그룹 정보 반환
        return new TodoGroupCreateResponse(
                savedGroup.getGroupId(),
                savedGroup.getGroupName(),
                savedGroup.getGroupColor(),
                savedGroup.getGroupCategory(),
                savedGroup.getDeadline() != null ? savedGroup.getDeadline().toLocalDate() : null,
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
                                        memberUser.getNickname(),
                                        memberUser.getProfileImage()
                                );
                            })
                            .toList();

                    int totalTodos = todoRepository.countByGroup_GroupId(group.getGroupId());
                    int completedTodos = todoRepository.countByGroup_GroupIdAndStatus(group.getGroupId(), com.danwoog.todo.domain.todo.TodoStatus.COMPLETED);

                    return new TodoGroupSummaryResponse(
                            group.getGroupId(),
                            group.getGroupName(),
                            group.getGroupColor(),
                            group.getGroupCategory(),
                            group.getDeadline() != null ? group.getDeadline().toLocalDate() : null,
                            group.getPriority(),
                            group.getStatus(),
                            group.getTotalGarlicReward(),
                            group.getRemainingGarlicReward(),
                            members,
                            groupMembers.size(),
                            totalTodos,
                            completedTodos
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
                parseGroupColor(request.getGroupIconUrl()),
                normalizeGroupCategory(request.getGroupCategory()),
                request.getDeadline() != null ? request.getDeadline().atStartOfDay() : null,
                Priority.valueOf(request.getPriority()),
                GroupStatus.valueOf(request.getStatus())
        );

        return new TodoGroupUpdateResponse(
                group.getGroupId(),
                group.getGroupName(),
                group.getGroupColor(),
                group.getGroupCategory(),
                group.getDeadline() != null ? group.getDeadline().toLocalDate() : null,
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

    // 6. POST : 마늘 분배 및 그룹 완료 처리
    public void distributeGarlic(Long loginUserId, Long groupId, TodoGroupGarlicDistributionRequest request) {
        TodoGroup group = todoGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));

        // 그룹의 상태를 완료로 변경
        group.update(
                group.getGroupName(),
                group.getGroupColor(),
                group.getGroupCategory(),
                group.getDeadline() != null ? group.getDeadline() : null,
                group.getPriority(),
                GroupStatus.COMPLETED
        );

        if (request.getDistributions() != null) {
            for (GarlicDistributionDto dto : request.getDistributions()) {
                if (dto.getRewardAmount() == null || dto.getRewardAmount() <= 0) {
                    continue;
                }
                User member = userRepository.findById(dto.getUserId())
                        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

                int currentGarlic = member.getGarlicCount() != null ? member.getGarlicCount() : 0;
                member.updateGarlicCount(currentGarlic + dto.getRewardAmount());
            }
        }
    }

    private String normalizeGroupCategory(String groupCategory) {
        if (groupCategory == null || groupCategory.isBlank()) {
            return null;
        }
        return groupCategory.trim();
    }

    private String parseGroupColor(String groupIconUrl) {
        if (groupIconUrl == null || groupIconUrl.isBlank()) {
            return null;
        }

        int colorIndex = groupIconUrl.indexOf("\"color\"");
        if (colorIndex < 0) {
            return null;
        }

        int colonIndex = groupIconUrl.indexOf(':', colorIndex);
        if (colonIndex < 0) {
            return null;
        }

        int startQuote = groupIconUrl.indexOf('"', colonIndex + 1);
        if (startQuote < 0) {
            return null;
        }

        int endQuote = groupIconUrl.indexOf('"', startQuote + 1);
        if (endQuote < 0) {
            return null;
        }

        String color = groupIconUrl.substring(startQuote + 1, endQuote).trim();
        return color.isBlank() ? null : color;
    }
}
