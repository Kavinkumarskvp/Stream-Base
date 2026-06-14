package kavin.personal_project.streambase.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import kavin.personal_project.streambase.dto.CommentDto;
import kavin.personal_project.streambase.dto.IncomingCommentMessage;
import kavin.personal_project.streambase.service.CommentService;
import kavin.personal_project.streambase.service.PresenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommentWebSocketHandler extends TextWebSocketHandler {

    // videoId -> set of sessions currently watching that video
    private final Map<Long, Map<String, WebSocketSession>> sessionsByVideo = new ConcurrentHashMap<>();
    private final Map<String, String> userIdBySession = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper;
    private final CommentService commentService;
    private final PresenceService presenceService;
    private final StringRedisTemplate redisTemplate;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

        Long videoId = extractVideoId(session);
        if (videoId == null) {
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        sessionsByVideo
                .computeIfAbsent(videoId, k -> new ConcurrentHashMap<>())
                .put(session.getId(), session);

        log.info("WS connected: session={} videoId={} totalOnVideo={}",
                session.getId(),
                videoId,
                sessionsByVideo.get(videoId).size());

        // Send last 50 comments to the new joiner
        List<CommentDto> comments = commentService.getRecentComments(videoId);
        for (CommentDto comment : comments) {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(comment)));
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        Long videoId = extractVideoId(session);
        IncomingCommentMessage incomingMessage = objectMapper.readValue(message.getPayload(), IncomingCommentMessage.class);

        if (incomingMessage.userId() != null) {
            userIdBySession.put(session.getId(), incomingMessage.userId());
        }

        String messageType = incomingMessage.type() == null ? "comment" : incomingMessage.type();
        if ("join".equals(messageType)) {
            presenceService.heartbeat(videoId, incomingMessage.userId());
            publishUserEvent(videoId, incomingMessage.userId(), "joined");
            broadcastViewerCount(videoId);

        } else if ("heartbeat".equals(messageType)) {
            presenceService.heartbeat(videoId, incomingMessage.userId());

        } else { // messageType == comment

            CommentDto savedComment = commentService.saveComment(videoId, incomingMessage.userId(), incomingMessage.text());

            // Publish to Redis — every app instance (including this one) will receive & broadcast
            String channel = "comments:" + videoId;
            String json = objectMapper.writeValueAsString(savedComment);
            redisTemplate.convertAndSend(channel, json);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {

        Long videoId = extractVideoId(session);
        String userId = userIdBySession.remove(session.getId());

        if (videoId != null) {
            Map<String, WebSocketSession> sessions = sessionsByVideo.get(videoId);

            if (sessions != null) {
                sessions.remove(session.getId());

                if (sessions.isEmpty()) {
                    sessionsByVideo.remove(videoId);
                }
            }

            if (userId != null) {
                presenceService.leave(videoId, userId);
                publishUserEvent(videoId, userId, "left");
                broadcastViewerCount(videoId);
            }
        }
        log.info("WS closed: session={} userId={} status={}",
                session.getId(),
                userId,
                status);
    }

    /**
     * Called by CommentBroadcastListener when a Redis pub/sub
     * message arrives.
     * Broadcasts the JSON to all WS clients on THIS app
     * instance watching this video.
     */
    public void broadcastJsonToLocalClients(Long videoId, String json) {

        Map<String, WebSocketSession> sessions = sessionsByVideo.get(videoId);
        if (sessions == null) return;

        TextMessage message = new TextMessage(json);

        for (WebSocketSession session : sessions.values()) {

            if (session.isOpen()) {

                synchronized (session) { // WebSocketSession.sendMessage is not thread-safe
                    try {
                        session.sendMessage(message);

                    } catch (IOException e) {
                        log.warn("Failed to send to session {}: {}",
                                session.getId(),
                                e.getMessage());
                    }

                }
            }
        }
    }

    private void broadcastViewerCount(Long videoId) {
        try {
            long count = presenceService.activeViewerCount(videoId);
            String json = objectMapper.writeValueAsString(
                    Map.of(
                            "type", "viewer_count",
                            "videoId", videoId,
                            "count", count)
            );
            redisTemplate.convertAndSend("comments:" + videoId, json);

        } catch (Exception e) {
            log.warn("Failed to broadcast viewer count for video {}: {}",
                    videoId,
                    e.getMessage());
        }
    }

    private void publishUserEvent(Long videoId, String userId, String event) {

        try {
            String json = objectMapper.writeValueAsString(
                    Map.of(
                            "type", "user_event",
                            "userId", userId,
                            "event", event)
            );
            redisTemplate.convertAndSend("comments:" + videoId, json);

        } catch (Exception e) {
            log.warn("Failed to publish user event for {}: {}",
                    userId,
                    e.getMessage());
        }
    }

    private Long extractVideoId(WebSocketSession session) {

        String path = session.getUri().getPath();
        String[] parts = path.split("/");
        try {
            return Long.parseLong(parts[parts.length - 1]);
        } catch (Exception e) {
            return null;
        }
    }
}
