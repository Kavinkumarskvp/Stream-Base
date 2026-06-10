package kavin.personal_project.streambase.controller;

import kavin.personal_project.streambase.dto.SubscribeRequest;
import kavin.personal_project.streambase.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping
    public ResponseEntity<Void> subscribe(@RequestBody SubscribeRequest request) {

        subscriptionService.subscribe(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
