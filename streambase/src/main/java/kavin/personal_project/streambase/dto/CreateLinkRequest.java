package kavin.personal_project.streambase.dto;

import lombok.Data;

@Data
public class CreateLinkRequest {
    private Long videoId;
    private Long validityInMonths;
    private String customPrefix;
}
