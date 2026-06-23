package kavin.personal_project.streambase.search;

import kavin.personal_project.streambase.entity.VideoEntity;
import kavin.personal_project.streambase.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class SearchAdminController {

    private final VideoRepository videoRepository;
    private final VideoSearchRepository videoSearchRepository;

    @PostMapping("/reindex")
    public Map<String, Object> reIndexAllVideos() {

        long indexed = 0;
        long failed = 0;

        for (VideoEntity video : videoRepository.findAll()) {
            try {

                VideoSearchDocument document = VideoSearchDocument.builder()
                        .id(video.getId())
                        .title(video.getTitle())
                        .description(video.getDescription())
                        .uploadedBy(video.getUploadedBy())
                        .createdAt(video.getCreatedAt())
                        .build();
                videoSearchRepository.save(document);
                indexed++;

            } catch (Exception e) {
                failed++;
            }
        }

        return Map.of(
                "indexed", indexed,
                "failed", failed
        );
    }
}
