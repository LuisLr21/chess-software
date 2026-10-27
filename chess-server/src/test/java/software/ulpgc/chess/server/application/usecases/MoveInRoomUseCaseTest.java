package software.ulpgc.chess.server.application.usecases;

import org.junit.jupiter.api.Test;
import software.ulpgc.chess.core.Color;
import software.ulpgc.chess.core.GameStatus;
import software.ulpgc.chess.core.PieceType;
import software.ulpgc.chess.core.Square;
import software.ulpgc.chess.server.application.domain.NotAPlayerException;
import software.ulpgc.chess.server.application.domain.NotYourTurnException;
import software.ulpgc.chess.server.application.domain.WaitingForOpponentException;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MoveInRoomUseCaseTest {
    private final RoomFixture fixture = new RoomFixture();

    private RoomState move(PlayerSession player, String code, String from, String to) {
        return fixture.move.execute(new RoomMove(code, player.playerToken(), Square.at(from), Square.at(to), PieceType.Queen));
    }

    @Test
    void aMoveIsSavedAndNotified() {
        String code = fixture.startGame(0);
        RoomState state = move(fixture.white, code, "e2", "e4");

        assertEquals(Color.Black, state.game().position().sideToMove());
        assertEquals(state.game(), fixture.games.findById(state.game().id()).orElseThrow());
        assertEquals(state, fixture.notifier.lastRoom());
    }

    @Test
    void cannotMoveBeforeTheOpponentJoins() {
        PlayerSession white = fixture.createRoom.execute(new NewRoom("Luis", 0));
        String code = white.state().room().code();
        assertThrows(WaitingForOpponentException.class,
                () -> fixture.move.execute(new RoomMove(code, white.playerToken(), Square.at("e2"), Square.at("e4"), PieceType.Queen)));
    }

    @Test
    void eachPlayerMovesOnlyOnTheirTurn() {
        String code = fixture.startGame(0);
        assertThrows(NotYourTurnException.class, () -> move(fixture.black, code, "e7", "e5"));
        assertThrows(NotAPlayerException.class,
                () -> fixture.move.execute(new RoomMove(code, "stranger", Square.at("e2"), Square.at("e4"), PieceType.Queen)));
    }

    @Test
    void movingCancelsAPendingDrawOffer() {
        String code = fixture.startGame(0);
        fixture.offerDraw.execute(new PlayerInRoom(code, fixture.black.playerToken()));
        RoomState state = move(fixture.white, code, "e2", "e4");
        assertNull(state.room().drawOfferedBy());
        assertNull(fixture.rooms.findByCode(code).orElseThrow().drawOfferedBy());
    }

    @Test
    void theClockRunsWithTheServerTime() {
        String code = fixture.startGame(5);
        fixture.clock.advance(Duration.ofSeconds(10));
        RoomState state = move(fixture.white, code, "e2", "e4");
        assertEquals(290_000, state.game().clock().whiteMillis());
    }

    @Test
    void readingTheRoomEndsTheGameWhenTimeRunsOut() {
        String code = fixture.startGame(1);
        fixture.clock.advance(Duration.ofSeconds(61));

        RoomState state = fixture.getRoom.execute(code);
        assertEquals(GameStatus.Timeout, state.game().status());
        assertEquals(Color.Black, state.game().winner());
        assertEquals(GameStatus.Timeout, fixture.games.findById(state.game().id()).orElseThrow().status());
        assertEquals(state, fixture.notifier.lastRoom());
    }

    @Test
    void readingAMissingRoomFails() {
        assertThrows(RoomNotFoundException.class, () -> fixture.getRoom.execute("NOPE00"));
    }
}
