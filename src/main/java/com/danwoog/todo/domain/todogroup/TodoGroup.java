package com.danwoog.todo.domain.todogroup;

import com.danwoog.todo.domain.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "todo_groups")
public class TodoGroup {

    private static final int GARLIC_REWARD_PER_MEMBER = 40;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id")
    private Long groupId;

    @Column(name = "group_name", length = 100, nullable = false)
    private String groupName;

    @Column(name = "description", length = 100)
    private String description;

    @Column(name = "group_color")
    private String groupColor;

    @Column(name = "group_category", length = 100)
    private String groupCategory;

    @Column(name = "deadline")
    private LocalDateTime deadline;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 20)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private GroupStatus status;

    @Column(name = "total_garlic_reward")
    private Integer totalGarlicReward;

    @Column(name = "remaining_garlic_reward")
    private Integer remainingGarlicReward;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected TodoGroup() {
    }

    public TodoGroup(String groupName, String description, User createdBy) {
        this.groupName = groupName;
        this.description = description;
        this.createdBy = createdBy;
        this.status = GroupStatus.IN_PROGRESS;
        this.totalGarlicReward = 0;
        this.remainingGarlicReward = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }


    // 생성자 오버로딩 (테스트는 간단용이라 위에 생성자 써가지고..)
    // 사실 위에 지우고 이걸로 해야하는게 맞는거 같긴 합니다...근데 테스트에서 형식 바꾸는게 싫어서 ㅠㅠ
    public TodoGroup(String groupName, String description,
                    LocalDateTime deadline, Priority priority,
                    User createdBy) {
        this.groupName = groupName;
        this.description = description;
        this.deadline = deadline;
        this.priority = priority;
        this.createdBy = createdBy;
        this.status = GroupStatus.IN_PROGRESS;
        this.totalGarlicReward = 0;
        this.remainingGarlicReward = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void setGroupColor(String groupColor) {
        this.groupColor = groupColor;
        this.updatedAt = LocalDateTime.now();
    }

    public void setGroupCategory(String groupCategory) {
        this.groupCategory = groupCategory;
        this.updatedAt = LocalDateTime.now();
    }

    // PATCH용 업데이트 메서드
    public void update(String groupName,
                    String groupColor,
                    String groupCategory,
                    LocalDateTime deadline,
                    Priority priority,
                    GroupStatus status) {
        this.groupName = groupName;
        this.groupColor = groupColor;
        this.groupCategory = groupCategory;
        this.deadline = deadline;
        this.priority = priority;
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }


    // 삭제
    public void delete() {
        this.status = GroupStatus.DELETED;
        this.updatedAt = LocalDateTime.now();
    }

    public void initializeGarlicBudget(int memberCount) {
        int budget = Math.max(memberCount, 0) * GARLIC_REWARD_PER_MEMBER;
        this.totalGarlicReward = budget;
        this.remainingGarlicReward = budget;
        this.updatedAt = LocalDateTime.now();
    }

    public void setGarlicBudget(int garlicReward) {
        int budget = Math.max(garlicReward, 0);
        this.totalGarlicReward = budget;
        this.remainingGarlicReward = budget;
        this.updatedAt = LocalDateTime.now();
    }

    public void increaseGarlicBudget(int additionalMemberCount) {
        if (additionalMemberCount <= 0) {
            return;
        }

        int additionalBudget = additionalMemberCount * GARLIC_REWARD_PER_MEMBER;
        this.totalGarlicReward = getSafeGarlicValue(totalGarlicReward) + additionalBudget;
        this.remainingGarlicReward = getSafeGarlicValue(remainingGarlicReward) + additionalBudget;
        this.updatedAt = LocalDateTime.now();
    }

    public void allocateGarlicReward(int garlicReward) {
        adjustAllocatedGarlicReward(0, garlicReward);
    }

    public void adjustAllocatedGarlicReward(int previousReward, int newReward) {
        int previous = Math.max(previousReward, 0);
        int next = Math.max(newReward, 0);
        int diff = next - previous;

        if (diff <= 0) {
            this.remainingGarlicReward = getSafeGarlicValue(remainingGarlicReward) + Math.abs(diff);
            this.updatedAt = LocalDateTime.now();
            return;
        }

        if (getSafeGarlicValue(remainingGarlicReward) < diff) {
            throw new IllegalArgumentException("그룹에 남아 있는 마늘보다 더 많이 배정할 수 없습니다.");
        }

        this.remainingGarlicReward = getSafeGarlicValue(remainingGarlicReward) - diff;
        this.updatedAt = LocalDateTime.now();
    }

    public void restoreGarlicReward(int garlicReward) {
        if (garlicReward <= 0) {
            return;
        }

        this.remainingGarlicReward = getSafeGarlicValue(remainingGarlicReward) + garlicReward;
        this.updatedAt = LocalDateTime.now();
    }

    private int getSafeGarlicValue(Integer value) {
        return value != null ? value : 0;
    }


    public Long getGroupId() {
        return groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getGroupColor() {
        return groupColor;
    }

    public String getGroupCategory() {
        return groupCategory;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public Priority getPriority() {
        return priority;
    }

    public GroupStatus getStatus() {
        return status;
    }

    public Integer getTotalGarlicReward() {
        return totalGarlicReward;
    }

    public Integer getRemainingGarlicReward() {
        return remainingGarlicReward;
    }
}
