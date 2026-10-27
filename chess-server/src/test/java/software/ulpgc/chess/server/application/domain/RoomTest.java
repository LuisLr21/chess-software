package software.ulpgc.chess.server.application.domain;

import org.junit.jupiter.api.Test;
import software.ulpgc.chess.core.Color;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoomTest {
    private final Seat luis = new Seat("token-luis", "Luis");
    private final Seat ana = new Seat("token-ana", "Ana");
    private final Room open = Room.open("ABC123", "game-1", luis);
    private final Room full = open.join(ana);

    @Test
    void anOpenRoomWaitsForASecondPlayer() {
        assertFalse(open.isFull());
        assertThrows(WaitingForOpponentException.class, open::ensureStarted);
        assertThrows(WaitingForOpponentException.class, () -> open.ensureCanMove("token-luis", Color.White));
    }

    @Test
    void theSecondPlayerTakesBlack() {
        assertTrue(full.isFull());
        assertEquals(Optional.of(Color.White), full.colorOf("token-luis"));
        assertEquals(Optional.of(Color.Black), full.colorOf("token-ana"));
        assertDoesNotThrow(full::ensureStarted);
    }

    @Test
    void aFullRoomCannotBeJoined() {
        assertThrows(RoomFullException.class, () -> full.join(new Seat("token-other", "Other")));
    }

    @Test
    void strangersAreNotPlayers() {
        assertEquals(Optional.empty(), full.colorOf("token-other"));
        assertEquals(Optional.empty(), full.colorOf(null));
        assertThrows(NotAPlayerException.class, () -> full.playerColor("token-other"));
    }

    @Test
    void eachPlayerMovesOnlyOnTheirTurn() {
        assertDoesNotThrow(() -> full.ensureCanMove("token-luis", Color.White));
        assertThrows(NotYourTurnException.class, () -> full.ensureCanMove("token-ana", Color.White));
        assertThrows(NotAPlayerException.class, () -> full.ensureCanMove("token-other", Color.White));
    }

    @Test
    void offersCanBeMadeAndWithdrawn() {
        Room offered = full.withDrawOffer(Color.Black).withRematchOffer(Color.White);
        assertEquals(Color.Black, offered.drawOfferedBy());
        assertEquals(Color.White, offered.rematchOfferedBy());
        assertNull(offered.withoutDrawOffer().drawOfferedBy());
    }

    @Test
    void aRematchSwapsColorsAndClearsOffers() {
        Room rematch = full.withDrawOffer(Color.Black).withRematchOffer(Color.White).rematch("game-2");
        assertEquals("game-2", rematch.gameId());
        assertEquals(ana, rematch.white());
        assertEquals(luis, rematch.black());
        assertNull(rematch.drawOfferedBy());
        assertNull(rematch.rematchOfferedBy());
    }
}
