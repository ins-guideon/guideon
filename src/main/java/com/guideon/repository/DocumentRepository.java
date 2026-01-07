package com.guideon.repository;

import com.guideon.model.DocumentMetadata;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<DocumentMetadata, String> {
    List<DocumentMetadata> findAllByOrderByUploadTimeDesc();
    
    /**
     * 문서 상세 조회 시 uploader도 함께 로드
     */
    @EntityGraph(attributePaths = {"uploader"})
    @Query("SELECT d FROM DocumentMetadata d WHERE d.id = :id")
    Optional<DocumentMetadata> findByIdWithUploader(@Param("id") String id);
}
