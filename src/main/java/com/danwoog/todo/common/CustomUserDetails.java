package com.danwoong.common;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;

/**
 * =====================================================
 * [팀원 A 연결 포인트]
 * 이 클래스는 팀원 A(인증 담당)의 CustomUserDetails와 맞춰야 함
 *
 * 팀원 A에게 확인할 것:
 *   1. 세션에 저장되는 UserDetails 구현체 클래스명
 *   2. userId를 꺼내는 메서드명 (getUserId()인지 확인)
 *   3. 이 파일을 팀원 A 버전으로 교체하거나, 팀원 A 코드를 여기에 맞춰달라고 요청
 * =====================================================
 */
public class CustomUserDetails implements UserDetails {

    private final Long userId;
    private final String username;
    private final String password;

    public CustomUserDetails(Long userId, String username, String password) {
        this.userId = userId;
        this.username = username;
        this.password = password;
    }

    /** userId 반환 — 팀원 A 버전 메서드명과 일치시킬 것 */
    public Long getUserId() {
        return userId;
    }

    @Override public String getUsername() { return username; }
    @Override public String getPassword() { return password; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }
}
