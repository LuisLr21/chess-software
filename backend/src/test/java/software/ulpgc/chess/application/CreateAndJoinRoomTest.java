package software.ulpgc.chess.application;

import org.junit.jupiter.api.Test;
import software.ulpgc.chess.domain.Color;
import software.ulpgc.chess.domain.RoomFullException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreateAndJoinRoomTest {
    private final RoomFixture fixture = new RoomFixture();

    @Test
    void theCreatorPlaysWhiteInANewRoom() {
        PlayerSession session = fixture.createRoom.execute("Luis", 5);
        String code = session.state().room().code();

        assertEquals(Color.White, session.color());
        assertTrue(code.matches("[A-HJ-NP-Z2-9]{6}"));
        assertEquals("Luis", session.state().room().white().name());
        assertFalse(session.state().room().isFull());
        assertTrue(fixture.rooms.findByCode(code).isPresent());
        assertTrue(fixture.games.findById(session.state().game().id()).isPresent());
    }

    @Test
    void aRoomWithoutMinutesHasNoClock() {
        assertNull(fixture.createRoom.execute("Luis", 0).state().game().clock());
        assertEquals(300_000, fixture.createRoom.execute("Luis", 5).state().game().clock().initialMillis());
    }

    @Test
    void rejectsInvalidMinutes() {
        assertThrows(IllegalArgumentException.class, () -> fixture.createRoom.execute("Luis", -1));
        assertThrows(IllegalArgumentException.class, () -> fixture.createRoom.execute("Luis", 61));
    }

    @Test
    void playersWithoutANameGetTheirColorAsName() {
        assertEquals("Blancas", fixture.createRoom.execute("  ", 0).state().room().white().name());
    }

    @Test
    void theSecondPlayerPlaysBlackAndStartsTheClock() {
        fixture.startGame(5);

        assertEquals(Color.Black, fixture.black.color());
        assertNotEquals(fixture.white.playerToken(), fixture.black.playerToken());
        assertTrue(fixture.black.state().room().isFull());
        assertEquals(RoomFixture.START, fixture.black.state().game().clock().turnStartedAt());
        assertEquals(fixture.black.state(), fixture.notifier.lastRoom());
    }

    @Test
    void cannotJoinAMissingOrFullRoom() {
        String code = fixture.startGame(0);
        assertThrows(RoomNotFoundException.class, () -> fixture.joinRoom.execute("NOPE00", "Ana"));
        assertThrows(RoomFullException.class, () -> fixture.joinRoom.execute(code, "Pepe"));
    }
}
