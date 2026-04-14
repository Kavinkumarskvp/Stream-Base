package kavin.personal_project.streambase.repository;

import kavin.personal_project.streambase.entity.VideoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoRepository extends JpaRepository<VideoEntity, Long> {
}
