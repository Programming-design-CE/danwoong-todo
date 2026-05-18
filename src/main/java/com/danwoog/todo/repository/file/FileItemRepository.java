package com.danwoog.todo.repository.file;

import com.danwoog.todo.domain.file.FileEntity;
import com.danwoog.todo.domain.file.Folder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileItemRepository extends JpaRepository<FileEntity, Long> {

    List<FileEntity> findByGroup_GroupIdAndFolder(Long groupId, Folder folder);

    List<FileEntity> findByGroup_GroupIdAndFolderIsNull(Long groupId);
}