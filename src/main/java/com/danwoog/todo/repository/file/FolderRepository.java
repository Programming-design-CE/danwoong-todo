package com.danwoog.todo.repository.file;

import com.danwoog.todo.domain.file.Folder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FolderRepository extends JpaRepository<Folder, Long> {

    Optional<Folder> findByIdAndGroup_GroupId(Long id, Long groupId);

    List<Folder> findByGroup_GroupId(Long groupId);

    Optional<Folder> findByGroup_GroupIdAndParentFolderIdIsNull(Long groupId);

    List<Folder> findByGroup_GroupIdAndParentFolderId(Long groupId, Long parentFolderId);
}
