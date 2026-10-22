package software.ulpgc.chess.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SeatAndChatMessageTest {
    private static final Instant NOW = Instant.parse("2026-10-22T10:00:00Z");

    @Test
    void aSeatUsesTheFallbackForBlankNames() {
        assertEquals("Blancas", Seat.of("token", null, "Blancas").name());
        assertEquals("Blancas", Seat.of("token", "   ", "Blancas").name());
    }

    @Test
    void aSeatTrimsAndShortensNames() {
        assertEquals("Luis", Seat.of("token", "  Luis  ", "Blancas").name());
        assertEquals("ABCDEFGHIJKLMNOP", Seat.of("token", "ABCDEFGHIJKLMNOPQRST", "Blancas").name());
    }

    @Test
    void aMessageIsTrimmed() {
        assertEquals("hola", ChatMessage.create("ABC123", Color.White, "Luis", "  hola ", NOW).text());
    }

    @Test
    void aMessageCannotBeEmpty() {
        assertThrows(IllegalArgumentException.class, () -> ChatMessage.create("ABC123", Color.White, "Luis", "   ", NOW));
        assertThrows(IllegalArgumentException.class, () -> ChatMessage.create("ABC123", Color.White, "Luis", null, NOW));
    }

    @Test
    void aMessageHasAtMostTwoHundredCharacters() {
        assertEquals(200, ChatMessage.create("ABC123", Color.White, "Luis", "a".repeat(200), NOW).text().length());
        assertThrows(IllegalArgumentException.class, () -> ChatMessage.create("ABC123", Color.White, "Luis", "a".repeat(201), NOW));
    }
}
