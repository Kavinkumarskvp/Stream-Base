package kavin.personal_project.streambase.repository;

import kavin.personal_project.streambase.entity.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    List<CommentEntity> findTop50ByVideoIdOrderByCreatedAtDesc(Long videoId);
}
