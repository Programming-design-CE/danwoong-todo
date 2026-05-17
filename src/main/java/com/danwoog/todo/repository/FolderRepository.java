package com.danwoog.todo.repository;

import com.danwoog.todo.domain.Folder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FolderRepository extends JpaRepository<Folder, Long> {
    // group은 TodoGroup 객체이므로 group.id 탐색 → group_Id
    Optional<Folder> findByGroup_IdAndParentFolderIdIsNull(Long groupId);
    List<Folder> findByGroup_IdAndParentFolderId(Long groupId, Long parentFolderId);
}
