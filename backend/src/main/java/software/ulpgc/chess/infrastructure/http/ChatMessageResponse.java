package software.ulpgc.chess.infrastructure.http;

import software.ulpgc.chess.domain.ChatMessage;

public record ChatMessageResponse(String author, String authorName, String text, String sentAt) {
    public static ChatMessageResponse from(ChatMessage message) {
        return new ChatMessageResponse(message.author().name(), message.authorName(), message.text(), message.sentAt().toString());
    }
}
