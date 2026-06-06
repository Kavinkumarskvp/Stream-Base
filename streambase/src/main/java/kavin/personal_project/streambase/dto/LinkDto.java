package kavin.personal_project.streambase.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LinkDto {

    private String shortUrl;
    private String code;
    private Long videoId;
    private Long clickCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiresAt;
}
