package kavin.personal_project.streambase.service;

import kavin.personal_project.streambase.dto.NotificationDto;
import kavin.personal_project.streambase.mapper.NotificationMapper;
import kavin.personal_project.streambase.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    @Transactional(readOnly = true)
    public List<NotificationDto> getUserNotifications(String userId) {
        return notificationRepository.findBySubscriberIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(notificationMapper::toDto)
                .toList();
    }
}
