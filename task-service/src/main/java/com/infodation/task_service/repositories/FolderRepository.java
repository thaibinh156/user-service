package com.infodation.task_service.repositories;

import com.infodation.task_service.models.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Long> {
    boolean existsByPath(String path);
    Optional<Folder> findByPath(String path);

}
