package kavin.personal_project.streambase.repository;

import kavin.personal_project.streambase.entity.VideoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface VideoRepository extends JpaRepository<VideoEntity, Long> {

    List<VideoEntity> findTop50ByUploadedByInOrderByCreatedAtDesc(Collection<String> uploadedBy);
}
