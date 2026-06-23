package kavin.personal_project.streambase.controller;

import jakarta.validation.Valid;
import kavin.personal_project.streambase.dto.CreateVideoRequest;
import kavin.personal_project.streambase.dto.UpdateVideoRequest;
import kavin.personal_project.streambase.dto.VideoDto;
import kavin.personal_project.streambase.entity.VideoEntity;
import kavin.personal_project.streambase.exception.VideoNotFoundException;
import kavin.personal_project.streambase.mapper.VideoMapper;
import kavin.personal_project.streambase.repository.VideoRepository;
import kavin.personal_project.streambase.service.VideoService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/api/videos")
public class VideoController {

    private final VideoService videoService;
    private final VideoRepository videoRepository;
    private final VideoMapper videoMapper;

    @GetMapping
    public List<VideoDto> getAllVideos() {
        return videoService.getAllVideos();
    }

    @GetMapping("/{id:\\d+}")
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

    @GetMapping("/search-sql")
    public Map<String, Object> searchSql(
            @RequestParam("q") String query
    ) {

        long start = Instant.now().toEpochMilli();

        String like = "%" + query + "%";
        List<VideoEntity> videos = videoRepository.findTop50ByTitleIlikeOrDescriptionIlikeOrderByCreatedAtDesc(like, like);

        return  Map.of(
                "query", query,
                "results", videos.stream()
                        .map(videoMapper::toDto)
                        .toList(),
                "totalElements", videos.size(),
                "latency", Instant.now().toEpochMilli() - start
        );
    }
}
