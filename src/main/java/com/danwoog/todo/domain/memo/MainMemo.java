package com.danwoog.todo.domain.memo;

import com.danwoog.todo.domain.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "main_memos")
public class MainMemo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "memo_id")
    private Long memoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    protected MainMemo() {}

    public MainMemo(User user, String content) {
        this.user = user;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }

    public Long getMemoId() { return memoId; }
    public String getContent() { return content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void updateContent(String content) { this.content = content; }
}