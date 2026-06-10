package kavin.personal_project.streambase.event;

public record NotificationEvent(String subscriberId, String creatorId, Long videoId, String videoTitle) {
}
