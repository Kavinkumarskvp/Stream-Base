package kavin.personal_project.streambase.consumer;

import kavin.personal_project.streambase.entity.VideoEntity;
import kavin.personal_project.streambase.event.VideoPublishedEvent;
import kavin.personal_project.streambase.event.VideoUploadedEvent;
import kavin.personal_project.streambase.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;

@Component
@Log
@RequiredArgsConstructor
public class VideoProcesscor {

    private final VideoRepository videoRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "video.uploaded",
            groupId = "video-processor",
            containerFactory = "videoUploadedListenerContainerFactory")
    public void process(VideoUploadedEvent event) {

        try {
            log.info("Processing video: " + event.videoId() + " - " + event.title());
            updateStatus(event.videoId(), VideoEntity.VideoStatus.PROCESSING);

            Thread.sleep(3_000);  // simulate thumbnail generation and metadata extraction

            updateStatus(event.videoId(), VideoEntity.VideoStatus.READY);
            log.info("Video ready: " + event.videoId());

            videoRepository.findById(event.videoId()).ifPresent(video -> {

                VideoPublishedEvent publishedEvent = new VideoPublishedEvent(
                        video.getId(),
                        video.getTitle(),
                        video.getUploadedBy(),
                        video.getCreatedAt().atZone(ZoneOffset.UTC).toInstant().toEpochMilli());
                kafkaTemplate.send("video.published", publishedEvent);
            });

        } catch (Exception e) {
            log.warning("Failed to process video " + event.videoId() + ": " + e.getMessage());
            kafkaTemplate.send("video.uploaded.DLT", event);
        }
    }

    private void updateStatus(Long videoId, VideoEntity.VideoStatus status) {

        videoRepository.findById(videoId).ifPresent(videoEntity -> {
            videoEntity.setStatus(status);
            videoRepository.save(videoEntity);
        });
    }
}
