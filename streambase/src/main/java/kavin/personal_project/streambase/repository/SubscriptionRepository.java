package kavin.personal_project.streambase.repository;

import kavin.personal_project.streambase.entity.SubscriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubscriptionRepository extends JpaRepository<SubscriptionEntity, Long> {
    List<SubscriptionEntity> findByCreatorId(String creatorId);

    boolean existsBySubscriberIdAndCreatorId(String subscriberId, String creatorId);
}