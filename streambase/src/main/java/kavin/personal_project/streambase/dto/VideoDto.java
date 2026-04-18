package kavin.personal_project.streambase.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VideoDto {

    private Long id;
    private String name;
    private String description;
    private String location;
    private String author;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime uploadedTime;
}
