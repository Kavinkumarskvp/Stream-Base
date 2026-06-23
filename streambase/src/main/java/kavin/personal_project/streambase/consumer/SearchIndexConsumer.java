package kavin.personal_project.streambase.consumer;

import kavin.personal_project.streambase.entity.VideoEntity;
import kavin.personal_project.streambase.event.VideoPublishedEvent;
import kavin.personal_project.streambase.repository.VideoRepository;
import kavin.personal_project.streambase.search.VideoSearchDocument;
import kavin.personal_project.streambase.search.VideoSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SearchIndexConsumer {

    private final VideoRepository videoRepository;
    private final VideoSearchRepository videoSearchRepository;

    @KafkaListener(
            topics = "video.published",
            groupId = "search-indexer",
            containerFactory = "videoPublishedConcurrentKafkaListenerContainerFactory"
    )
    public void indexVideo(VideoPublishedEvent event) {

        try {

            VideoEntity video = videoRepository.findById(event.videoId()).orElse(null);
            if (video == null) {

                log.warn(
                        "Video {} not found for indexing — skipping",
                        event.videoId()
                );
                return;
            }

            VideoSearchDocument document = VideoSearchDocument.builder()
                    .id(video.getId())
                    .title(video.getTitle())
                    .description(video.getDescription())
                    .uploadedBy(video.getUploadedBy())
                    .createdAt(video.getCreatedAt())
                    .build();
            videoSearchRepository.save(document);

            log.info(
                    "Indexed video: id={} title='{}'",
                    video.getId(),
                    video.getTitle()
            );
        } catch (Exception e) {

            log.error(
                    "Failed to index video {}: {}",
                    event.videoId(),
                    e.getMessage()
            );
            throw e;
        }
    }
}
