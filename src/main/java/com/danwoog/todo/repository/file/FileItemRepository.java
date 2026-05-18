package com.danwoog.todo.repository;

import com.danwoog.todo.domain.file.FileEntity;
import com.danwoog.todo.domain.file.Folder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileItemRepository extends JpaRepository<FileEntity, Long> {
    // group은 TodoGroup 객체이므로 group.id 탐색 → group_Id
    List<FileEntity> findByGroup_IdAndFolder(Long groupId, Folder folder);
    List<FileEntity> findByGroup_IdAndFolderIsNull(Long groupId);
}
