package kavin.personal_project.streambase.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommentBroadcastListener implements MessageListener {

    private final CommentWebSocketHandler commentWebSocketHandler;

    @Override
    public void onMessage(Message message, byte @Nullable [] pattern) {

        String channel = new String(message.getChannel(), StandardCharsets.UTF_8);
        String json = new String(message.getBody(), StandardCharsets.UTF_8);

        // Channel format: "comments:42" → extract videoId
        String[] parts = channel.split(":");
        if(parts.length != 2) {
            log.warn("Unexpected channel name: {}", channel);
            return;
        }

        Long videoId;
        try {
            videoId = Long.parseLong(parts[1]);
        } catch (Exception e) {
            log.warn("Invalid videoId in channel {}", channel);
            return;
        }

        commentWebSocketHandler.broadcastJsonToLocalClients(videoId, json);
    }
}
