package kavin.personal_project.streambase.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VideoDto {

    private Long id;
    private String name;
    private String description;
    private String location;
    private String author;
    private LocalDateTime uploadedTime;
}
