package software.ulpgc.chess.application;

import org.junit.jupiter.api.Test;
import software.ulpgc.chess.domain.Color;
import software.ulpgc.chess.domain.GameOverException;
import software.ulpgc.chess.domain.GameStatus;
import software.ulpgc.chess.domain.NotAPlayerException;
import software.ulpgc.chess.domain.WaitingForOpponentException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EndingAndRematchTest {
    private final RoomFixture fixture = new RoomFixture();

    @Test
    void resigningGivesTheWinToTheOpponent() {
        String code = fixture.startGame(0);
        RoomState state = fixture.resign.execute(code, fixture.black.playerToken());
        assertEquals(GameStatus.Resignation, state.game().status());
        assertEquals(Color.White, state.game().winner());
        assertEquals(state, fixture.notifier.lastRoom());
    }

    @Test
    void cannotResignBeforeTheGameStartsOrWithoutBeingAPlayer() {
        PlayerSession white = fixture.createRoom.execute("Luis", 0);
        assertThrows(WaitingForOpponentException.class,
                () -> fixture.resign.execute(white.state().room().code(), white.playerToken()));

        String code = fixture.startGame(0);
        assertThrows(NotAPlayerException.class, () -> fixture.resign.execute(code, "stranger"));
    }

    @Test
    void aDrawNeedsBothPlayers() {
        String code = fixture.startGame(0);
        RoomState offered = fixture.offerDraw.execute(code, fixture.white.playerToken());
        assertEquals(Color.White, offered.room().drawOfferedBy());
        assertEquals(GameStatus.InProgress, offered.game().status());

        RoomState accepted = fixture.offerDraw.execute(code, fixture.black.playerToken());
        assertEquals(GameStatus.DrawByAgreement, accepted.game().status());
        assertNull(accepted.room().drawOfferedBy());
    }

    @Test
    void aDrawOfferCanBeDeclined() {
        String code = fixture.startGame(0);
        fixture.offerDraw.execute(code, fixture.white.playerToken());
        RoomState declined = fixture.declineDraw.execute(code, fixture.black.playerToken());
        assertNull(declined.room().drawOfferedBy());
        assertEquals(GameStatus.InProgress, declined.game().status());
    }

    @Test
    void cannotOfferADrawWhenTheGameIsOver() {
        String code = fixture.startGame(0);
        fixture.resign.execute(code, fixture.white.playerToken());
        assertThrows(GameOverException.class, () -> fixture.offerDraw.execute(code, fixture.black.playerToken()));
    }

    @Test
    void aRematchNeedsAFinishedGame() {
        String code = fixture.startGame(0);
        assertThrows(GameNotOverException.class, () -> fixture.rematch.execute(code, fixture.white.playerToken()));
    }

    @Test
    void aRematchStartsANewGameWithColorsSwapped() {
        String code = fixture.startGame(5);
        String firstGame = fixture.resign.execute(code, fixture.white.playerToken()).game().id();

        RoomState requested = fixture.rematch.execute(code, fixture.white.playerToken());
        assertEquals(Color.White, requested.room().rematchOfferedBy());
        assertEquals(firstGame, requested.game().id());

        RoomState rematch = fixture.rematch.execute(code, fixture.black.playerToken());
        assertNotEquals(firstGame, rematch.game().id());
        assertEquals(GameStatus.InProgress, rematch.game().status());
        assertEquals("Ana", rematch.room().white().name());
        assertEquals("Luis", rematch.room().black().name());
        assertEquals(Color.White, rematch.room().colorOf(fixture.black.playerToken()).orElseThrow());
        assertNull(rematch.room().rematchOfferedBy());
        assertEquals(300_000, rematch.game().clock().whiteMillis());
        assertTrue(rematch.game().clock().isRunning());
    }
}
