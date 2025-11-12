package com.guideon.repository;

import com.guideon.model.DocumentMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<DocumentMetadata, String> {
    List<DocumentMetadata> findAllByOrderByUploadTimeDesc();
}
