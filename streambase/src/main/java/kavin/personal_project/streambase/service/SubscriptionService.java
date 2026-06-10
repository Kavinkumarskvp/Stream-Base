package kavin.personal_project.streambase.service;

import kavin.personal_project.streambase.dto.SubscribeRequest;
import kavin.personal_project.streambase.entity.SubscriptionEntity;
import kavin.personal_project.streambase.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    @Transactional
    public void subscribe(SubscribeRequest request) {

        if (subscriptionRepository.existsBySubscriberIdAndCreatorId(request.getSubscriberId(), request.getCreatorId())) {
            return;
        }

        SubscriptionEntity entity = SubscriptionEntity.builder()
                .subscriberId(request.getSubscriberId())
                .creatorId(request.getCreatorId())
                .build();
        subscriptionRepository.save(entity);
    }
}
