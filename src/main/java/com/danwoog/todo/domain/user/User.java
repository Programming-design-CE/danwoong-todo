package com.danwoog.todo.domain.user;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "login_id", length = 50)
    private String loginId;

    @Column(name = "password", length = 255)
    private String password;

    @Column(name = "nickname", length = 50)
    private String nickname;

    @Column(name = "profile_image", length = 255)
    private String profileImage;

    @Column(name = "garlic_count")
    private Integer garlicCount = 0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Builder
    public User(String loginId, String password, String nickname, String profileImage, Integer garlicCount) {
        this.loginId = loginId;
        this.password = password;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.garlicCount = garlicCount != null ? garlicCount : 0;
    }
}
