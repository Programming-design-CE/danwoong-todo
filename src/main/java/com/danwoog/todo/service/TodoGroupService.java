package com.danwoog.todo.service;

import com.danwoog.todo.domain.todo.TodoStatus;
import com.danwoog.todo.domain.todogroup.GroupMemberRole;
import com.danwoog.todo.domain.todogroup.GroupStatus;
import com.danwoog.todo.domain.todogroup.Priority;
import com.danwoog.todo.domain.todogroup.TodoGroup;
import com.danwoog.todo.domain.todogroup.TodoGroupMember;
import com.danwoog.todo.domain.user.User;
import com.danwoog.todo.dto.todogroup.GarlicDistributionDto;
import com.danwoog.todo.dto.todogroup.MemberPreviewResponse;
import com.danwoog.todo.dto.todogroup.TodoGroupCreateRequest;
import com.danwoog.todo.dto.todogroup.TodoGroupCreateResponse;
import com.danwoog.todo.dto.todogroup.TodoGroupDeleteResponse;
import com.danwoog.todo.dto.todogroup.TodoGroupGarlicDistributionRequest;
import com.danwoog.todo.dto.todogroup.TodoGroupInviteRequest;
import com.danwoog.todo.dto.todogroup.TodoGroupInviteResponse;
import com.danwoog.todo.dto.todogroup.TodoGroupListResponse;
import com.danwoog.todo.dto.todogroup.TodoGroupRemoveMemberResponse;
import com.danwoog.todo.dto.todogroup.TodoGroupSummaryResponse;
import com.danwoog.todo.dto.todogroup.TodoGroupUpdateRequest;
import com.danwoog.todo.dto.todogroup.TodoGroupUpdateResponse;
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
    private final FileService fileService;

    public TodoGroupCreateResponse createGroup(Long loginUserId, TodoGroupCreateRequest request) {
        User leader = userRepository.findById(loginUserId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

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
        todoGroupMemberRepository.save(new TodoGroupMember(savedGroup, leader, GroupMemberRole.LEADER));
        fileService.initializeProjectFolder(savedGroup, leader);

        if (request.getInviteeIds() != null) {
            request.getInviteeIds().stream()
                    .filter(inviteeId -> !inviteeId.equals(loginUserId))
                    .distinct()
                    .forEach(inviteeId -> {
                        User invitee = userRepository.findById(inviteeId)
                                .orElseThrow(() -> new IllegalArgumentException("초대할 사용자를 찾을 수 없습니다."));

                        todoGroupMemberRepository.save(
                                new TodoGroupMember(savedGroup, invitee, GroupMemberRole.MEMBER)
                        );
                    });
        }

        List<TodoGroupMember> groupMembers =
                todoGroupMemberRepository.findByGroup_GroupId(savedGroup.getGroupId());

        if (request.getTotalGarlicReward() != null) {
            savedGroup.setGarlicBudget(request.getTotalGarlicReward());
        } else {
            savedGroup.initializeGarlicBudget(groupMembers.size());
        }

        List<MemberPreviewResponse> members = groupMembers.stream()
                .map(groupMember -> new MemberPreviewResponse(
                        groupMember.getUser().getUserId(),
                        groupMember.getUser().getNickname(),
                        groupMember.getUser().getProfileImage()
                ))
                .toList();

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

    public TodoGroupListResponse getMyGroups(Long userId) {
        cleanupDeletedProjectFiles();

        List<TodoGroupMember> myMemberships =
                todoGroupMemberRepository.findByUser_UserId(userId);

        List<TodoGroupSummaryResponse> groups = myMemberships.stream()
                .filter(myMember -> myMember.getGroup().getStatus() != GroupStatus.DELETED)
                .map(myMember -> {
                    TodoGroup group = myMember.getGroup();
                    List<TodoGroupMember> groupMembers =
                            todoGroupMemberRepository.findByGroup_GroupId(group.getGroupId());

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
                    int completedTodos = todoRepository.countByGroup_GroupIdAndStatus(
                            group.getGroupId(),
                            TodoStatus.COMPLETED
                    );

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
                            completedTodos,
                            group.getCreatedBy() != null ? group.getCreatedBy().getUserId() : null
                    );
                })
                .toList();

        return new TodoGroupListResponse(groups);
    }

    private void cleanupDeletedProjectFiles() {
        List<TodoGroup> deletedGroups = todoGroupRepository.findByStatus(GroupStatus.DELETED);
        for (TodoGroup deletedGroup : deletedGroups) {
            fileService.deleteProjectFiles(deletedGroup);
        }
    }

    public TodoGroupUpdateResponse updateGroup(
            Long userId,
            Long groupId,
            TodoGroupUpdateRequest request
    ) {
        TodoGroup group = todoGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));

        String previousGroupName = group.getGroupName();

        group.update(
                request.getGroupName(),
                parseGroupColor(request.getGroupIconUrl()),
                normalizeGroupCategory(request.getGroupCategory()),
                request.getDeadline() != null ? request.getDeadline().atStartOfDay() : null,
                Priority.valueOf(request.getPriority()),
                GroupStatus.valueOf(request.getStatus())
        );

        fileService.syncRootFolderName(group, previousGroupName);

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

    public TodoGroupDeleteResponse deleteGroup(Long userId, Long groupId) {
        TodoGroup group = todoGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));

        fileService.deleteProjectFiles(group);
        group.delete();

        return new TodoGroupDeleteResponse(groupId, "DELETED");
    }

    public TodoGroupInviteResponse inviteMembers(
            Long loginUserId,
            Long groupId,
            TodoGroupInviteRequest request
    ) {
        TodoGroup group = todoGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));

        int invitedCount = 0;

        if (request.getMemberIds() != null) {
            for (Long memberId : request.getMemberIds()) {
                if (memberId.equals(loginUserId)) {
                    continue;
                }

                User member = userRepository.findById(memberId)
                        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

                boolean alreadyMember =
                        todoGroupMemberRepository.existsByGroup_GroupIdAndUser_UserId(groupId, memberId);

                if (alreadyMember) {
                    continue;
                }

                todoGroupMemberRepository.save(
                        new TodoGroupMember(group, member, GroupMemberRole.MEMBER)
                );
                invitedCount++;
            }
        }

        group.increaseGarlicBudget(invitedCount);

        return new TodoGroupInviteResponse(groupId, invitedCount);
    }

    public TodoGroupRemoveMemberResponse removeMembers(
            Long loginUserId,
            Long groupId,
            TodoGroupInviteRequest request
    ) {
        TodoGroup group = todoGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));

        if (group.getCreatedBy() == null || !group.getCreatedBy().getUserId().equals(loginUserId)) {
            throw new IllegalArgumentException("프로젝트 리더만 멤버를 제거할 수 있습니다.");
        }

        int removedCount = 0;

        if (request.getMemberIds() != null) {
            for (Long memberId : request.getMemberIds()) {
                if (memberId == null || memberId.equals(loginUserId)) {
                    continue;
                }

                TodoGroupMember member = todoGroupMemberRepository
                        .findByGroup_GroupIdAndUser_UserId(groupId, memberId)
                        .orElse(null);

                if (member == null || member.getRole() == GroupMemberRole.LEADER) {
                    continue;
                }

                todoGroupMemberRepository.delete(member);
                removedCount++;
            }
        }

        return new TodoGroupRemoveMemberResponse(groupId, removedCount);
    }

    public void distributeGarlic(Long loginUserId, Long groupId, TodoGroupGarlicDistributionRequest request) {
        TodoGroup group = todoGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));

        if (group.getCreatedBy() == null || !group.getCreatedBy().getUserId().equals(loginUserId)) {
            throw new IllegalArgumentException("마늘 분배는 프로젝트 리더만 가능합니다.");
        }

        group.update(
                group.getGroupName(),
                group.getGroupColor(),
                group.getGroupCategory(),
                group.getDeadline(),
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

        if (group.getRemainingGarlicReward() != null && group.getRemainingGarlicReward() > 0) {
            group.allocateGarlicReward(group.getRemainingGarlicReward());
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
