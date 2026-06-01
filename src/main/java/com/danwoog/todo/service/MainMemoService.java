package com.danwoog.todo.service;

import com.danwoog.todo.domain.memo.MainMemo;
import com.danwoog.todo.domain.user.User;
import com.danwoog.todo.dto.memo.MainMemoCreateRequest;
import com.danwoog.todo.dto.memo.MainMemoDto;
import com.danwoog.todo.dto.memo.MainMemoListResponse;
import com.danwoog.todo.repository.memo.MainMemoRepository;
import com.danwoog.todo.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MainMemoService {

    private final MainMemoRepository mainMemoRepository;
    private final UserRepository userRepository;

    public MainMemoListResponse getMemos(Long userId) {
        List<MainMemoDto> memos = mainMemoRepository.findByUserUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(MainMemoDto::new)
                .collect(Collectors.toList());
        return new MainMemoListResponse(memos);
    }

    @Transactional
    public MainMemoDto createMemo(Long userId, MainMemoCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        MainMemo memo = new MainMemo(user, request.getContent());
        return new MainMemoDto(mainMemoRepository.save(memo));
    }

    @Transactional
    public MainMemoDto updateMemo(Long userId, Long memoId, MainMemoCreateRequest request) {
        MainMemo memo = mainMemoRepository.findById(memoId)
                .orElseThrow(() -> new IllegalArgumentException("메모를 찾을 수 없습니다."));
        memo.updateContent(request.getContent());
        return new MainMemoDto(memo);
    }

    @Transactional
    public void deleteMemo(Long userId, Long memoId) {
        MainMemo memo = mainMemoRepository.findById(memoId)
                .orElseThrow(() -> new IllegalArgumentException("메모를 찾을 수 없습니다."));
        mainMemoRepository.delete(memo);
    }
}