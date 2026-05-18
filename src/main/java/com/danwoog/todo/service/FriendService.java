package com.danwoog.todo.service;

import com.danwoog.todo.domain.friend.Friend;
import com.danwoog.todo.domain.friend.FriendRequest;
import com.danwoog.todo.domain.friend.FriendRequestStatus;
import com.danwoog.todo.domain.user.User;
import com.danwoog.todo.dto.friend.*;
import com.danwoog.todo.repository.friend.FriendRepository;
import com.danwoog.todo.repository.friend.FriendRequestRepository;
import com.danwoog.todo.repository.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class FriendService {

    private final FriendRepository friendRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final UserRepository userRepository;

    public FriendService(
            FriendRepository friendRepository,
            FriendRequestRepository friendRequestRepository,
            UserRepository userRepository
    ) {
        this.friendRepository = friendRepository;
        this.friendRequestRepository = friendRequestRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public FriendRequestResponse sendFriendRequest(Long senderId, FriendRequestCreateRequest request) {
        User sender = findUser(senderId);
        User receiver = findUser(request.getReceiver_id());

        if (sender.getUserId().equals(receiver.getUserId())) {
            throw new IllegalArgumentException("자기 자신에게 친구 요청을 보낼 수 없습니다.");
        }

        if (friendRepository.existsByUserAndFriendUser(sender, receiver)) {
            throw new IllegalArgumentException("이미 친구인 사용자입니다.");
        }

        if (friendRequestRepository.existsBySenderAndReceiverAndStatus(sender, receiver, FriendRequestStatus.PENDING)) {
            throw new IllegalArgumentException("이미 대기 중인 친구 요청이 있습니다.");
        }

        FriendRequest friendRequest = new FriendRequest(sender, receiver);
        FriendRequest savedRequest = friendRequestRepository.save(friendRequest);

        return new FriendRequestResponse(
                savedRequest.getRequestId(),
                savedRequest.getStatus().name()
        );
    }

    public FriendListResponse getFriends(Long userId) {
        User user = findUser(userId);

        List<FriendResponse> friends = friendRepository.findByUser(user)
                .stream()
                .map(friend -> {
                    User friendUser = friend.getFriendUser();

                    return new FriendResponse(
                            friendUser.getUserId(),
                            friendUser.getNickname(),
                            friendUser.getProfileImage()
                    );
                })
                .toList();

        return new FriendListResponse(friends);
    }

    public ReceivedFriendRequestListResponse getReceivedRequests(Long receiverId) {
        User receiver = findUser(receiverId);

        List<ReceivedFriendRequestResponse> requests =
                friendRequestRepository.findByReceiverAndStatus(receiver, FriendRequestStatus.PENDING)
                        .stream()
                        .map(request -> new ReceivedFriendRequestResponse(
                                request.getRequestId(),
                                request.getSender().getUserId(),
                                request.getSender().getNickname(),
                                request.getStatus().name()
                        ))
                        .toList();

        return new ReceivedFriendRequestListResponse(requests);
    }

    @Transactional
    public FriendRequestResponse acceptRequest(Long receiverId, Long requestId) {
        User receiver = findUser(receiverId);

        FriendRequest request = friendRequestRepository.findByRequestIdAndReceiver(requestId, receiver)
                .orElseThrow(() -> new IllegalArgumentException("친구 요청을 찾을 수 없습니다."));

        if (request.getStatus() != FriendRequestStatus.PENDING) {
            throw new IllegalArgumentException("이미 처리된 친구 요청입니다.");
        }

        request.accept();

        User sender = request.getSender();

        if (!friendRepository.existsByUserAndFriendUser(receiver, sender)) {
            friendRepository.save(new Friend(receiver, sender));
        }

        if (!friendRepository.existsByUserAndFriendUser(sender, receiver)) {
            friendRepository.save(new Friend(sender, receiver));
        }

        return new FriendRequestResponse(
                request.getRequestId(),
                request.getStatus().name()
        );
    }

    @Transactional
    public FriendRequestResponse rejectRequest(Long receiverId, Long requestId) {
        User receiver = findUser(receiverId);

        FriendRequest request = friendRequestRepository.findByRequestIdAndReceiver(requestId, receiver)
                .orElseThrow(() -> new IllegalArgumentException("친구 요청을 찾을 수 없습니다."));

        if (request.getStatus() != FriendRequestStatus.PENDING) {
            throw new IllegalArgumentException("이미 처리된 친구 요청입니다.");
        }

        request.reject();

        return new FriendRequestResponse(
                request.getRequestId(),
                request.getStatus().name()
        );
    }

    public UserSearchResponse searchUsers(Long currentUserId, String keyword) {
        List<FriendResponse> users = userRepository.findByNicknameContaining(keyword)
                .stream()
                .filter(user -> !user.getUserId().equals(currentUserId))
                .map(user -> new FriendResponse(
                        user.getUserId(),
                        user.getNickname(),
                        user.getProfileImage()
                ))
                .toList();

        return new UserSearchResponse(users);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }
}
