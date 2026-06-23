package kavin.personal_project.streambase.repository;

import kavin.personal_project.streambase.entity.VideoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface VideoRepository extends JpaRepository<VideoEntity, Long> {

    List<VideoEntity> findTop50ByUploadedByInOrderByCreatedAtDesc(Collection<String> uploadedBy);

    @Query(value = """
            SELECT * FROM videos
            WHERE LOWER(title) LIKE LOWER(?1) OR LOWER(description) LIKE LOWER(?2)
            ORDER BY created_at DESC
            LIMIT 50
            """,
            nativeQuery = true
    )
    List<VideoEntity> findTop50ByTitleIlikeOrDescriptionIlikeOrderByCreatedAtDesc(String titlePattern, String descriptionPattern);
}
