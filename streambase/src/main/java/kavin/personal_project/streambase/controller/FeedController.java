package kavin.personal_project.streambase.controller;

import kavin.personal_project.streambase.dto.VideoDto;
import kavin.personal_project.streambase.service.FeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/api/feed")
@RequiredArgsConstructor
@RestController
public class FeedController {

    private final FeedService feedService;

    @GetMapping
    public List<VideoDto> getFeed(
            @RequestParam("userId") String userId,
            @RequestParam(value = "mode", defaultValue = "push") String mode
    ) {
        return "pull".equals(mode) ?
                feedService.getFeedPull(userId) :
                feedService.getFeedPush(userId);
    }
}
