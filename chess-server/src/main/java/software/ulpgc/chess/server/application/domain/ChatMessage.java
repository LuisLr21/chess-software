package software.ulpgc.chess.server.application.domain;

import software.ulpgc.chess.core.Color;

import java.time.Instant;

public record ChatMessage(String roomCode, Color author, String authorName, String text, Instant sentAt) {
    private static final int MAX_LENGTH = 200;

    public static ChatMessage create(String roomCode, Color author, String authorName, String text, Instant sentAt) {
        String clean = text == null ? "" : text.trim();
        if (clean.isEmpty()) throw new IllegalArgumentException("Empty message");
        if (clean.length() > MAX_LENGTH) throw new IllegalArgumentException("Message longer than " + MAX_LENGTH + " characters");
        return new ChatMessage(roomCode, author, authorName, clean, sentAt);
    }
}
