package com.danwoog.todo.domain.todogroup;

import com.danwoog.todo.domain.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "todo_group_members")
public class TodoGroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_member_id")
    private Long groupMemberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private TodoGroup group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20)
    private GroupMemberRole role;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    protected TodoGroupMember() {
    }

    public TodoGroupMember(TodoGroup group, User user, GroupMemberRole role) {
        this.group = group;
        this.user = user;
        this.role = role;
        this.joinedAt = LocalDateTime.now();
    }

    public Long getGroupMemberId() {
        return groupMemberId;
    }

    public TodoGroup getGroup() {
        return group;
    }

    public User getUser() {
        return user;
    }

    public GroupMemberRole getRole() {
        return role;
    }
}
