package kavin.personal_project.streambase.controller;

import kavin.personal_project.streambase.dto.VideoDto;
import kavin.personal_project.streambase.exception.VideoNotFoundException;
import kavin.personal_project.streambase.service.VideoService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/videos")
public class VideoController {

    private final VideoService videoService;

    @GetMapping
    public List<VideoDto> getAllVideos() {
        return videoService.getAllVideos();
    }

    @GetMapping("/{id}")
    public VideoDto getVideo(@PathVariable("id") Long id) {

        return videoService.getVideo(id);
    }

    @ExceptionHandler(VideoNotFoundException.class)
    public ResponseEntity<Void> handleVideoNotFound() {
        return ResponseEntity.notFound().build();
    }

}
