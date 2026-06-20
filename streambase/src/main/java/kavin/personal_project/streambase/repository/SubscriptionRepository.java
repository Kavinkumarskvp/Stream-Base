package kavin.personal_project.streambase.repository;

import kavin.personal_project.streambase.entity.SubscriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SubscriptionRepository extends JpaRepository<SubscriptionEntity, Long> {
    List<SubscriptionEntity> findByCreatorId(String creatorId);

    boolean existsBySubscriberIdAndCreatorId(String subscriberId, String creatorId);

    @Query("Select s.creatorId From SubscriptionEntity s Where s.subscriberId = :subscriberId")
    List<String> findCreatorIdsBySubscriberId(@Param("subscriberId") String subscriberId);

    long countByCreatorId(String creatorId);
}