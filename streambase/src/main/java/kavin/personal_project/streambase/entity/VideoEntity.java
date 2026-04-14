package kavin.personal_project.streambase.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@Table(name = "videos")
@AllArgsConstructor
public class VideoEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "url")
    private String url;

    @Column(name = "uploaded_by")
    private String uploadedBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
