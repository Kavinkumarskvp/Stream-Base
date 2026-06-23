package kavin.personal_project.streambase.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "videos")
@Setting(settingPath = "elasticsearch/autocomplete_settings.json")
public class VideoSearchDocument {

    @Id
    private Long id;

    @MultiField(
            mainField = @Field(
                    type = FieldType.Text,
                    analyzer = "standard"
            ),
            otherFields = {
                    @InnerField(
                            suffix = "autocomplete",
                            type = FieldType.Text,
                            analyzer = "autocomplete_analyzer",
                            searchAnalyzer = "autocomplete_search_analyzer"
                    )
            }
    )
    private String title;

    @Field(
            type = FieldType.Text,
            analyzer = "standard"
    )
    private String description;

    @Field(type = FieldType.Keyword)
    private String uploadedBy;

    @Field(
            type = FieldType.Date,
            format = {},
            pattern = "uuuu-MM-dd'T'HH:mm:ss.SSSSSS"
    )
    private LocalDateTime createdAt;
}
