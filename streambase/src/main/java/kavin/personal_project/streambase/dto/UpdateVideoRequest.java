package kavin.personal_project.streambase.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateVideoRequest {

    @NotNull(message = "Video title must not be empty")
    @Size(min = 1, max = 255, message = "Video title must be between 1 and 255 characters")
    private String name;

    private String description;

    @NotNull(message = "Video URL must not be empty")
    @Size(min = 1, max = 500, message = "Video URL must not exceed 500 characters")
    private String url;
}