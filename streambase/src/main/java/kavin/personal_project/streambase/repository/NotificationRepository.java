package kavin.personal_project.streambase.repository;

import kavin.personal_project.streambase.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    List<NotificationEntity> findBySubscriberIdOrderByCreatedAtDesc(String subscriberId);
}
