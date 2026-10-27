package software.ulpgc.chess.server.application.usecases;

import org.junit.jupiter.api.Test;
import software.ulpgc.chess.core.Color;
import software.ulpgc.chess.server.application.domain.ChatMessage;
import software.ulpgc.chess.server.application.domain.NotAPlayerException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ChatUseCasesTest {
    private final RoomFixture fixture = new RoomFixture();

    @Test
    void messagesAreStoredInOrderAndNotified() {
        String code = fixture.startGame(0);
        ChatMessage hello = fixture.sendMessage.execute(new NewMessage(code, fixture.white.playerToken(), "hola"));
        ChatMessage luck = fixture.sendMessage.execute(new NewMessage(code, fixture.black.playerToken(), "suerte"));

        assertEquals(Color.White, hello.author());
        assertEquals("Luis", hello.authorName());
        assertEquals("Ana", luck.authorName());
        assertEquals(RoomFixture.START, hello.sentAt());
        assertEquals(List.of(hello, luck), fixture.getMessages.execute(code));
        assertEquals(List.of(hello, luck), fixture.notifier.messages);
    }

    @Test
    void onlyPlayersCanWrite() {
        String code = fixture.startGame(0);
        assertThrows(NotAPlayerException.class, () -> fixture.sendMessage.execute(new NewMessage(code, "stranger", "hola")));
    }

    @Test
    void emptyMessagesAreRejected() {
        String code = fixture.startGame(0);
        assertThrows(IllegalArgumentException.class, () -> fixture.sendMessage.execute(new NewMessage(code, fixture.white.playerToken(), " ")));
    }

    @Test
    void aMissingRoomHasNoChat() {
        assertThrows(RoomNotFoundException.class, () -> fixture.getMessages.execute("NOPE00"));
        assertThrows(RoomNotFoundException.class, () -> fixture.sendMessage.execute(new NewMessage("NOPE00", "token", "hola")));
    }
}
