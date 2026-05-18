package com.danwoog.todo.repository.friend;

import com.danwoog.todo.domain.friend.Friend;
import org.springframework.data.jpa.repository.JpaRepository;
import com.danwoog.todo.domain.user.*;
import java.util.List;

public interface FriendRepository extends JpaRepository<Friend, Long> {

    List<Friend> findByUser(User user);

    boolean existsByUserAndFriendUser(User user, User friendUser);
}