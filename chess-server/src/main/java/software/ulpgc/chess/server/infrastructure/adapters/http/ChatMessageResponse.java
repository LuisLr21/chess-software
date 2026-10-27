package software.ulpgc.chess.server.infrastructure.adapters.http;

import software.ulpgc.chess.server.application.domain.ChatMessage;

public record ChatMessageResponse(String author, String authorName, String text, String sentAt) {
    public static ChatMessageResponse from(ChatMessage message) {
        return new ChatMessageResponse(message.author().name(), message.authorName(), message.text(), message.sentAt().toString());
    }
}
