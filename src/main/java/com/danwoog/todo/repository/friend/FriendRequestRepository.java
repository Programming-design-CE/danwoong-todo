package com.danwoog.todo.repository.friend;

import com.danwoog.todo.domain.friend.FriendRequest;
import com.danwoog.todo.domain.friend.FriendRequestStatus;
import com.danwoog.todo.domain.user.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {

    List<FriendRequest> findByReceiverAndStatus(User receiver, FriendRequestStatus status);

    boolean existsBySenderAndReceiverAndStatus(User sender, User receiver, FriendRequestStatus status);

    Optional<FriendRequest> findByRequestIdAndReceiver(Long requestId, User receiver);
}