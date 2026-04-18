package kavin.personal_project.streambase.controller;

import jakarta.validation.Valid;
import kavin.personal_project.streambase.dto.CreateVideoRequest;
import kavin.personal_project.streambase.dto.UpdateVideoRequest;
import kavin.personal_project.streambase.dto.VideoDto;
import kavin.personal_project.streambase.exception.VideoNotFoundException;
import kavin.personal_project.streambase.service.VideoService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;

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

    @PostMapping
    public ResponseEntity<VideoDto> createVideo(@Valid @RequestBody CreateVideoRequest request, UriComponentsBuilder uriComponentsBuilder) {
        var videoDto = videoService.createVideo(request);

        var uri = uriComponentsBuilder.path("/api/videos/{id}").buildAndExpand(videoDto.getId()).toUri();

        return ResponseEntity.created(uri).body(videoDto);
    }

    @PutMapping("/{id}")
    public VideoDto updateVideo(@PathVariable("id") Long id, @Valid @RequestBody UpdateVideoRequest request) {
        return videoService.updateVideo(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVideo(@PathVariable("id") Long id) {
        videoService.deleteVideo(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(VideoNotFoundException.class)
    public ResponseEntity<Void> handleVideoNotFound() {
        return ResponseEntity.notFound().build();
    }

}
