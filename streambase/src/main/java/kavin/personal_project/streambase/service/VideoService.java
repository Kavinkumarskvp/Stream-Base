package kavin.personal_project.streambase.service;

import kavin.personal_project.streambase.dto.CreateVideoRequest;
import kavin.personal_project.streambase.dto.UpdateVideoRequest;
import kavin.personal_project.streambase.dto.VideoDto;
import kavin.personal_project.streambase.entity.VideoEntity;
import kavin.personal_project.streambase.exception.VideoNotFoundException;
import kavin.personal_project.streambase.mapper.VideoMapper;
import kavin.personal_project.streambase.repository.VideoRepository;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class VideoService {

    private final VideoRepository videoRepository;
    private final VideoMapper videoMapper;


    @Transactional(readOnly = true)
    public List<VideoDto> getAllVideos() {

        return videoRepository.findAll().stream().map(videoMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "videos", key = "#id")
    public VideoDto getVideo(Long id) {

        var video = videoRepository.findById(id).orElseThrow(VideoNotFoundException::new);
        return videoMapper.toDto(video);
    }

    @Transactional
    public VideoDto createVideo(CreateVideoRequest request) {

        VideoEntity entity = videoMapper.toEntity(request);
        entity = videoRepository.save(entity);

        return videoMapper.toDto(entity);
    }

    @Transactional
    @CacheEvict(value = "videos", key = "#id")
    public VideoDto updateVideo(Long id, UpdateVideoRequest request) {

        var entity = videoRepository.findById(id).orElseThrow(VideoNotFoundException::new);
        videoMapper.updateEntity(request, entity);
        entity = videoRepository.save(entity);

        return videoMapper.toDto(entity);
    }

    @Transactional
    @CacheEvict(value = "videos", key = "#id")
    public void deleteVideo(Long id) {

        if (!videoRepository.existsById(id)) {
            throw new VideoNotFoundException();
        }
        videoRepository.deleteById(id);
    }
}
