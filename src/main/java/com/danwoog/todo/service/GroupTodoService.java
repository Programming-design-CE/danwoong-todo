package com.danwoog.todo.service;

import com.danwoog.todo.domain.note.GroupNote;
import com.danwoog.todo.domain.todo.GarlicDistributionType;
import com.danwoog.todo.domain.todo.Todo;
import com.danwoog.todo.domain.todo.TodoAssignee;
import com.danwoog.todo.domain.todo.TodoStatus;
import com.danwoog.todo.domain.todogroup.Priority;
import com.danwoog.todo.domain.todogroup.TodoGroup;
import com.danwoog.todo.domain.user.User;
import com.danwoog.todo.dto.note.GroupNoteRequest;
import com.danwoog.todo.dto.note.GroupNoteResponse;
import com.danwoog.todo.dto.todo.GroupTodoAssigneeRequest;
import com.danwoog.todo.dto.todo.GroupTodoAssigneeResponse;
import com.danwoog.todo.dto.todo.GroupTodoCompleteResponse;
import com.danwoog.todo.dto.todo.GroupTodoCreateRequest;
import com.danwoog.todo.dto.todo.GroupTodoCreateResponse;
import com.danwoog.todo.dto.todo.GroupTodoDetailResponse;
import com.danwoog.todo.dto.todo.GroupTodoDetailView;
import com.danwoog.todo.dto.todo.GroupTodoListResponse;
import com.danwoog.todo.dto.todo.GroupTodoSummaryDto;
import com.danwoog.todo.dto.todo.GroupTodoUpdateRequest;
import com.danwoog.todo.dto.todo.GroupTodoUpdateResponse;
import com.danwoog.todo.repository.MemberRepository;
import com.danwoog.todo.repository.TodoGroupRepository;
import com.danwoog.todo.repository.note.GroupTodoNoteRepository;
import com.danwoog.todo.repository.todo.GroupTodoAssigneeRepository;
import com.danwoog.todo.repository.todo.GroupTodoPrivateNoteRepository;
import com.danwoog.todo.repository.todo.GroupTodoRepository;
import com.danwoog.todo.repository.todo.TodoRepository;
import com.danwoog.todo.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class GroupTodoService {

    private final TodoRepository todoRepository;
    private final GroupTodoRepository groupTodoRepository;
    private final GroupTodoAssigneeRepository groupTodoAssigneeRepository;
    private final GroupTodoPrivateNoteRepository groupTodoPrivateNoteRepository;
    private final TodoGroupRepository todoGroupRepository;
    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final GroupTodoNoteRepository groupTodoNoteRepository;

    public GroupTodoCreateResponse createTodo(Long loginUserId, Long groupId, GroupTodoCreateRequest request) {
        validateGroupMember(loginUserId, groupId);

        TodoGroup group = getGroup(groupId);
        User loginUser = getUser(loginUserId);

        Todo todo = new Todo(group, request.getTodoName(), request.getDescription(), loginUser);
        todo.setDeadline(toStartOfDay(request.getDeadline()));
        todo.setGarlicReward(request.getGarlicReward());
        todo.setPriority(parsePriority(request.getPriority()));
        todo.setCategory(request.getCategory());
        todo.setDistributionType(parseDistributionType(request.getDistributionType()));

        List<AssigneeAllocation> allocations = buildValidatedAllocations(
                groupId,
                todo.getGarlicReward(),
                todo.getDistributionType(),
                request.getAssignees()
        );
        group.allocateGarlicReward(getSafeGarlicReward(todo.getGarlicReward()));

        Todo savedTodo = groupTodoRepository.save(todo);
        List<GroupTodoAssigneeResponse> savedAssignees = saveAssignees(savedTodo, allocations);

        return new GroupTodoCreateResponse(
                savedTodo.getTodoId(),
                savedTodo.getDistributionType(),
                savedAssignees
        );
    }

    @Transactional(readOnly = true)
    public GroupTodoListResponse getGroupTodos(Long loginUserId, Long groupId, TodoStatus status) {
        validateGroupMember(loginUserId, groupId);

        List<Todo> todos = groupTodoRepository.findGroupTodosByStatus(groupId, status);
        Map<Long, List<GroupTodoAssigneeResponse>> assigneeMap =
                getAssigneeMap(todos.stream().map(Todo::getTodoId).toList());

        List<GroupTodoSummaryDto> responses = todos.stream()
                .map(todo -> new GroupTodoSummaryDto(
                        todo,
                        assigneeMap.getOrDefault(todo.getTodoId(), List.of())
                ))
                .toList();

        return new GroupTodoListResponse(responses);
    }

    @Transactional(readOnly = true)
    public GroupTodoDetailResponse getTodoDetail(Long loginUserId, Long todoId) {
        Todo todo = getTodo(todoId);
        validateGroupMember(loginUserId, todo.getGroup().getGroupId());

        GroupTodoDetailView detailView = groupTodoRepository.findDetailViewByTodoId(todoId)
                .orElseThrow(() -> new IllegalArgumentException("할 일을 찾을 수 없습니다."));

        List<GroupTodoAssigneeResponse> assignees = buildAssigneeResponses(
                groupTodoAssigneeRepository.findByTodoIdWithUser(todoId)
        );

        return new GroupTodoDetailResponse(detailView, assignees);
    }

    public GroupTodoUpdateResponse updateTodo(Long loginUserId, Long todoId, GroupTodoUpdateRequest request) {
        Todo todo = getTodo(todoId);
        validateGroupMember(loginUserId, todo.getGroup().getGroupId());

        User loginUser = getUser(loginUserId);
        boolean wasCompleted = todo.getStatus() == TodoStatus.COMPLETED;
        LocalDateTime now = LocalDateTime.now();

        GarlicDistributionType distributionType = request.getDistributionType() != null
                ? parseDistributionType(request.getDistributionType())
                : todo.getDistributionType();
        Integer garlicReward = request.getGarlicReward() != null
                ? request.getGarlicReward()
                : todo.getGarlicReward();
        int previousGarlicReward = getSafeGarlicReward(todo.getGarlicReward());
        int nextGarlicReward = getSafeGarlicReward(garlicReward);

        todo.getGroup().adjustAllocatedGarlicReward(previousGarlicReward, nextGarlicReward);

        groupTodoRepository.updateTodoFields(
                todoId,
                request.getTodoName(),
                request.getDescription(),
                toStartOfDay(request.getDeadline()),
                garlicReward,
                parsePriority(request.getPriority()),
                request.getCategory(),
                distributionType,
                now
        );

        if (request.getAssignees() != null) {
            List<AssigneeAllocation> allocations = buildValidatedAllocations(
                    todo.getGroup().getGroupId(),
                    garlicReward,
                    distributionType,
                    request.getAssignees()
            );
            groupTodoAssigneeRepository.deleteByTodo_TodoId(todoId);
            saveAssignees(todo, allocations);
        }

        if (request.getStatus() != null) {
            TodoStatus requestedStatus = parseStatus(request.getStatus());

            if (requestedStatus == TodoStatus.COMPLETED && !wasCompleted) {
                groupTodoRepository.completeTodo(todoId, loginUser, now, now);
                rewardAssignees(groupTodoAssigneeRepository.findByTodoIdWithUser(todoId));
            } else if (requestedStatus == TodoStatus.IN_PROGRESS && wasCompleted) {
                groupTodoRepository.reopenTodo(todoId, now);
            }
        }

        return new GroupTodoUpdateResponse(todo.getTodoId());
    }

    public GroupTodoCompleteResponse completeTodo(Long loginUserId, Long todoId) {
        Todo todo = getTodo(todoId);
        validateGroupMember(loginUserId, todo.getGroup().getGroupId());

        User loginUser = getUser(loginUserId);
        List<TodoAssignee> assignees = groupTodoAssigneeRepository.findByTodoIdWithUser(todoId);

        if (todo.getStatus() != TodoStatus.COMPLETED) {
            LocalDateTime now = LocalDateTime.now();
            groupTodoRepository.completeTodo(todoId, loginUser, now, now);
            rewardAssignees(assignees);
        }

        return new GroupTodoCompleteResponse(
                todo.getTodoId(),
                TodoStatus.COMPLETED,
                todo.getGarlicReward(),
                buildAssigneeResponses(assignees)
        );
    }

    public void deleteTodo(Long loginUserId, Long todoId) {
        Todo todo = getTodo(todoId);
        validateGroupMember(loginUserId, todo.getGroup().getGroupId());
        todo.getGroup().restoreGarlicReward(getSafeGarlicReward(todo.getGarlicReward()));

        groupTodoPrivateNoteRepository.deleteByTodo_TodoId(todoId);
        groupTodoAssigneeRepository.deleteByTodo_TodoId(todoId);
        groupTodoRepository.delete(todo);
    }

    @Transactional(readOnly = true)
    public GroupNoteResponse getGroupNote(Long loginUserId, Long groupId) {
        validateGroupMember(loginUserId, groupId);

        return groupTodoNoteRepository.findResponseByGroupId(groupId)
                .orElseGet(() -> new GroupNoteResponse(null, ""));
    }

    public GroupNoteResponse upsertGroupNote(Long loginUserId, Long groupId, GroupNoteRequest request) {
        validateGroupMember(loginUserId, groupId);

        TodoGroup group = getGroup(groupId);
        User loginUser = getUser(loginUserId);

        if (groupTodoNoteRepository.existsByGroup_GroupId(groupId)) {
            groupTodoNoteRepository.updateContentByGroupId(
                    groupId,
                    request.getContent(),
                    LocalDateTime.now()
            );
        } else {
            groupTodoNoteRepository.save(new GroupNote(group, loginUser, null, request.getContent()));
        }

        return groupTodoNoteRepository.findResponseByGroupId(groupId)
                .orElseThrow(() -> new IllegalArgumentException("그룹 메모를 저장하지 못했습니다."));
    }

    private TodoGroup getGroup(Long groupId) {
        return todoGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));
    }

    private Todo getTodo(Long todoId) {
        return todoRepository.findById(todoId)
                .orElseThrow(() -> new IllegalArgumentException("할 일을 찾을 수 없습니다."));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    private void validateGroupMember(Long userId, Long groupId) {
        boolean isMember = memberRepository.existsByGroup_GroupIdAndUser_UserId(groupId, userId);
        if (!isMember) {
            throw new IllegalArgumentException("그룹에 속한 사용자만 접근할 수 있습니다.");
        }
    }

    private Map<Long, List<GroupTodoAssigneeResponse>> getAssigneeMap(List<Long> todoIds) {
        if (todoIds.isEmpty()) {
            return Map.of();
        }

        return groupTodoAssigneeRepository.findByTodoIdsWithUser(todoIds).stream()
                .collect(Collectors.groupingBy(
                        assignee -> assignee.getTodo().getTodoId(),
                        LinkedHashMap::new,
                        Collectors.mapping(
                                this::toAssigneeResponse,
                                Collectors.toList()
                        )
                ));
    }

    private List<GroupTodoAssigneeResponse> buildAssigneeResponses(List<TodoAssignee> assignees) {
        return assignees.stream()
                .map(this::toAssigneeResponse)
                .toList();
    }

    private GroupTodoAssigneeResponse toAssigneeResponse(TodoAssignee assignee) {
        return new GroupTodoAssigneeResponse(
                assignee.getUser().getUserId(),
                assignee.getUser().getNickname(),
                assignee.getRewardAmount()
        );
    }

    private List<AssigneeAllocation> buildValidatedAllocations(
            Long groupId,
            Integer garlicReward,
            GarlicDistributionType distributionType,
            List<GroupTodoAssigneeRequest> assigneeRequests
    ) {
        if (distributionType == null) {
            throw new IllegalArgumentException("마늘 분배 방식을 입력해야 합니다.");
        }

        if (garlicReward == null || garlicReward < 0) {
            throw new IllegalArgumentException("총 마늘 보상은 0 이상이어야 합니다.");
        }

        if (assigneeRequests == null || assigneeRequests.isEmpty()) {
            throw new IllegalArgumentException("담당자는 최소 1명 이상 지정해야 합니다.");
        }

        List<Long> userIds = assigneeRequests.stream()
                .map(GroupTodoAssigneeRequest::getUserId)
                .toList();
        Set<Long> uniqueUserIds = new LinkedHashSet<>(userIds);

        if (uniqueUserIds.size() != userIds.size()) {
            throw new IllegalArgumentException("담당자는 중복 지정할 수 없습니다.");
        }

        List<User> users = userRepository.findAllById(uniqueUserIds);
        if (users.size() != uniqueUserIds.size()) {
            throw new IllegalArgumentException("존재하지 않는 사용자가 포함되어 있습니다.");
        }

        Set<Long> groupMemberIds = memberRepository.findByGroup_GroupId(groupId).stream()
                .map(member -> member.getUser().getUserId())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (!groupMemberIds.containsAll(uniqueUserIds)) {
            throw new IllegalArgumentException("그룹에 속한 사용자만 담당자로 지정할 수 있습니다.");
        }

        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getUserId, Function.identity()));

        List<Integer> rewardAmounts = buildRewardAmounts(garlicReward, distributionType, assigneeRequests);
        int totalReward = rewardAmounts.stream().mapToInt(Integer::intValue).sum();
        if (totalReward != garlicReward) {
            throw new IllegalArgumentException("담당자별 마늘 보상의 합은 총 마늘 보상과 같아야 합니다.");
        }

        List<AssigneeAllocation> allocations = new ArrayList<>();
        for (int i = 0; i < assigneeRequests.size(); i++) {
            GroupTodoAssigneeRequest assigneeRequest = assigneeRequests.get(i);
            allocations.add(new AssigneeAllocation(
                    userMap.get(assigneeRequest.getUserId()),
                    rewardAmounts.get(i)
            ));
        }
        return allocations;
    }

    private List<Integer> buildRewardAmounts(
            Integer garlicReward,
            GarlicDistributionType distributionType,
            List<GroupTodoAssigneeRequest> assigneeRequests
    ) {
        if (distributionType == GarlicDistributionType.EVEN) {
            int assigneeCount = assigneeRequests.size();
            int baseReward = garlicReward / assigneeCount;
            int remainder = garlicReward % assigneeCount;

            List<Integer> rewards = new ArrayList<>();
            for (int i = 0; i < assigneeCount; i++) {
                rewards.add(baseReward + (i < remainder ? 1 : 0));
            }
            return rewards;
        }

        List<Integer> rewards = assigneeRequests.stream()
                .map(GroupTodoAssigneeRequest::getRewardAmount)
                .toList();

        if (rewards.stream().anyMatch(reward -> reward == null || reward < 0)) {
            throw new IllegalArgumentException("CUSTOM 분배에서는 각 담당자의 reward_amount를 0 이상으로 입력해야 합니다.");
        }

        return rewards;
    }

    private Priority parsePriority(String priority) {
        if (priority == null || priority.isBlank()) {
            return null;
        }
        return Priority.valueOf(priority.trim().toUpperCase());
    }

    private GarlicDistributionType parseDistributionType(String distributionType) {
        if (distributionType == null || distributionType.isBlank()) {
            return null;
        }
        return GarlicDistributionType.valueOf(distributionType.trim().toUpperCase());
    }

    private TodoStatus parseStatus(String status) {
        return TodoStatus.valueOf(status.trim().toUpperCase());
    }

    private LocalDateTime toStartOfDay(LocalDate date) {
        return date != null ? date.atStartOfDay() : null;
    }

    private int getSafeGarlicReward(Integer garlicReward) {
        return garlicReward != null ? garlicReward : 0;
    }

    private List<GroupTodoAssigneeResponse> saveAssignees(Todo todo, List<AssigneeAllocation> allocations) {
        return allocations.stream()
                .map(allocation -> groupTodoAssigneeRepository.save(
                        new TodoAssignee(todo, allocation.user(), allocation.rewardAmount())
                ))
                .map(this::toAssigneeResponse)
                .toList();
    }

    private void rewardAssignees(List<TodoAssignee> assignees) {
        for (TodoAssignee assignee : assignees) {
            Integer rewardAmount = assignee.getRewardAmount();
            if (rewardAmount == null || rewardAmount <= 0) {
                continue;
            }

            User user = assignee.getUser();
            int currentGarlic = user.getGarlicCount() != null ? user.getGarlicCount() : 0;
            user.updateGarlicCount(currentGarlic + rewardAmount);
        }
    }

    private record AssigneeAllocation(User user, Integer rewardAmount) {
    }
}
