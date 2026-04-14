package kavin.personal_project.streambase.service;

import kavin.personal_project.streambase.dto.VideoDto;
import kavin.personal_project.streambase.exception.VideoNotFoundException;
import kavin.personal_project.streambase.mapper.VideoMapper;
import kavin.personal_project.streambase.repository.VideoRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class VideoService {

    private  final VideoRepository videoRepository;
    private final VideoMapper videoMapper;


    public List<VideoDto> getAllVideos() {

        return videoRepository.findAll().stream().map(videoMapper::toDto).toList();
    }

    public VideoDto getVideo(Long id) {

        var video = videoRepository.findById(id).orElseThrow(VideoNotFoundException::new);
        return videoMapper.toDto(video);
    }
}
