package com.danwoog.todo.service;

import com.danwoog.todo.domain.todogroup.GroupMemberRole;
import com.danwoog.todo.domain.todogroup.GroupStatus;
import com.danwoog.todo.domain.todogroup.Priority;
import com.danwoog.todo.domain.todogroup.TodoGroup;
import com.danwoog.todo.domain.todogroup.TodoGroupMember;
import com.danwoog.todo.domain.user.User;
import com.danwoog.todo.dto.todogroup.MemberPreviewResponse;
import com.danwoog.todo.dto.todogroup.TodoGroupCreateRequest;
import com.danwoog.todo.dto.todogroup.TodoGroupCreateResponse;
import com.danwoog.todo.dto.todogroup.TodoGroupDeleteResponse;
import com.danwoog.todo.dto.todogroup.TodoGroupListResponse;
import com.danwoog.todo.dto.todogroup.TodoGroupSummaryResponse;
import com.danwoog.todo.dto.todogroup.TodoGroupUpdateRequest;
import com.danwoog.todo.dto.todogroup.TodoGroupUpdateResponse;
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

        // 미리보기용 멤버 2명 조회
        List<MemberPreviewResponse> previews = groupMembers.stream()
                .limit(2)
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
                        previews,
                        groupMembers.size()
                );
        }


    // 2. GET : 내가 속한 공동 할 일 그룹 목록 조회
    public TodoGroupListResponse getMyGroups(Long userId) {

        List<TodoGroupMember> myMemberships =
                todoGroupMemberRepository.findByUser_UserId(userId);

        List<TodoGroupSummaryResponse> groups = myMemberships.stream()

                // 삭제되지 않은 그룹만 조회
                .filter(myMember ->
                        myMember.getGroup().getStatus() != GroupStatus.DELETED
                )

                .map(myMember -> {

                    TodoGroup group = myMember.getGroup();

                    List<TodoGroupMember> groupMembers =
                            todoGroupMemberRepository.findByGroup_GroupId(group.getGroupId());

                    // 멤버 미리보기 2명
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


    // 3. PATCH : 그룹 정보 수정
    public TodoGroupUpdateResponse updateGroup(
            Long userId,
            Long groupId,
            TodoGroupUpdateRequest request
    ) {

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


    // 4. DELETE : 그룹 삭제
    public TodoGroupDeleteResponse deleteGroup(Long userId, Long groupId) {

        TodoGroup group = todoGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));

        // soft delete 처리
        group.delete();

        return new TodoGroupDeleteResponse(
                groupId,
                "DELETED"
        );
    }
}