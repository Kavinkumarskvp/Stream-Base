package kavin.personal_project.streambase.controller;

import kavin.personal_project.streambase.dto.NotificationDto;
import kavin.personal_project.streambase.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public List<NotificationDto> getNotifications(@RequestParam("userId") String userId) {
        return notificationService.getUserNotifications(userId);
    }
 }
