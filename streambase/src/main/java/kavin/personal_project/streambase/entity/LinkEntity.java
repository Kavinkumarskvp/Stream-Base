package kavin.personal_project.streambase.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "links")
public class LinkEntity {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code")
    private String code;

    @Column(name = "prefix")
    private String prefix;

    @Column(name = "video_id", nullable = false)
    private Long videoId;

    @Column(name = "click_count")
    private Long clickCount = 0L;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate () {
        this.createdAt = LocalDateTime.now();
    }

}
