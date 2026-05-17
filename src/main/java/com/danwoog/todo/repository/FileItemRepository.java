package com.danwoog.todo.repository;

import com.danwoog.todo.domain.FileItem;
import com.danwoog.todo.domain.Folder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileItemRepository extends JpaRepository<FileItem, Long> {
    // group은 TodoGroup 객체이므로 group.id 탐색 → group_Id
    List<FileItem> findByGroup_IdAndFolder(Long groupId, Folder folder);
    List<FileItem> findByGroup_IdAndFolderIsNull(Long groupId);
}
