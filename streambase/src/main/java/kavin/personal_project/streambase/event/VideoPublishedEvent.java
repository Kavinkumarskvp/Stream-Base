package kavin.personal_project.streambase.event;

public record VideoPublishedEvent(
        Long videoId,
        String videoTitle,
        String creatorId,
        Long publishedAt) {
}
