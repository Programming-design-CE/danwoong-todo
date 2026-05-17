package com.danwoog.todo.domain.friend;

import com.danwoog.todo.domain.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "friends")
public class Friend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "friend_id")
    private Long friendId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "friend_user_id", nullable = false)
    private User friendUser;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    protected Friend() {
    }

    public Friend(User user, User friendUser) {
        this.user = user;
        this.friendUser = friendUser;
        this.createdAt = LocalDateTime.now();
    }

    public Long getFriendId() {
        return friendId;
    }

    public User getUser() {
        return user;
    }

    public User getFriendUser() {
        return friendUser;
    }
}