package software.ulpgc.chess.server.application.usecases;

import org.junit.jupiter.api.Test;
import software.ulpgc.chess.core.Color;
import software.ulpgc.chess.core.GameStatus;
import software.ulpgc.chess.core.PieceType;
import software.ulpgc.chess.core.Square;
import software.ulpgc.chess.server.application.domain.NotAPlayerException;
import software.ulpgc.chess.server.application.domain.Report;
import software.ulpgc.chess.server.application.domain.WaitingForOpponentException;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AbandonAndReportTest {
    private final RoomFixture fixture = new RoomFixture();

    private void move(PlayerSession player, String code, String from, String to) {
        fixture.move.execute(new RoomMove(code, player.playerToken(), Square.at(from), Square.at(to), PieceType.Queen));
    }

    @Test
    void abandoningGivesTheWinToTheOpponent() {
        String code = fixture.startGame(0);
        RoomState state = fixture.abandon.execute(new PlayerInRoom(code, fixture.black.playerToken()));
        assertEquals(GameStatus.Abandonment, state.game().status());
        assertEquals(Color.White, state.game().winner());
        assertEquals(state, fixture.notifier.lastRoom());
        assertEquals(state.game(), fixture.games.findById(state.game().id()).orElseThrow());
    }

    @Test
    void leavingBeforeTheGameStartsOrAfterItEndsChangesNothing() {
        PlayerSession white = fixture.createRoom.execute(new NewRoom("Luis", 0));
        String waiting = white.state().room().code();
        assertEquals(GameStatus.InProgress, fixture.abandon.execute(new PlayerInRoom(waiting, white.playerToken())).game().status());

        String code = fixture.startGame(0);
        fixture.resign.execute(new PlayerInRoom(code, fixture.white.playerToken()));
        assertEquals(GameStatus.Resignation, fixture.abandon.execute(new PlayerInRoom(code, fixture.black.playerToken())).game().status());
    }

    @Test
    void aReportShowsHowMuchTimeTheOpponentSpent() {
        String code = fixture.startGame(20);
        fixture.clock.advance(Duration.ofSeconds(5));
        move(fixture.white, code, "e2", "e4");
        fixture.clock.advance(Duration.ofMinutes(19));
        move(fixture.black, code, "e7", "e5");

        Report report = fixture.report.execute(new NewReport(code, fixture.white.playerToken(), "  Se queda parado  "));
        assertEquals(Color.White, report.reporter());
        assertEquals(Color.Black, report.reported());
        assertEquals("Se queda parado", report.reason());
        assertEquals(Duration.ofMinutes(19).toMillis(), report.reportedTimeUsedMillis());
        assertEquals(1, report.reportedMoves());
        assertEquals(List.of(report), fixture.notifier.reports);
        assertEquals(List.of(report), fixture.getReports.execute(code));
    }

    @Test
    void aReportCountsTheTimeTheOpponentIsSpendingRightNow() {
        String code = fixture.startGame(20);
        move(fixture.white, code, "e2", "e4");
        fixture.clock.advance(Duration.ofMinutes(12));

        Report report = fixture.report.execute(new NewReport(code, fixture.white.playerToken(), null));
        assertEquals(Duration.ofMinutes(12).toMillis(), report.reportedTimeUsedMillis());
        assertEquals(0, report.reportedMoves());
        assertEquals("Mala deportividad", report.reason());
    }

    @Test
    void withoutClockTheReportHasNoTime() {
        String code = fixture.startGame(0);
        move(fixture.white, code, "e2", "e4");
        assertNull(fixture.report.execute(new NewReport(code, fixture.black.playerToken(), null)).reportedTimeUsedMillis());
    }

    @Test
    void eachPlayerCanReportOnlyOncePerGame() {
        String code = fixture.startGame(0);
        fixture.report.execute(new NewReport(code, fixture.white.playerToken(), null));
        assertThrows(AlreadyReportedException.class, () -> fixture.report.execute(new NewReport(code, fixture.white.playerToken(), null)));
        fixture.report.execute(new NewReport(code, fixture.black.playerToken(), null));
        assertEquals(2, fixture.getReports.execute(code).size());
    }

    @Test
    void onlyPlayersOfAStartedGameCanReport() {
        PlayerSession white = fixture.createRoom.execute(new NewRoom("Luis", 0));
        assertThrows(WaitingForOpponentException.class,
                () -> fixture.report.execute(new NewReport(white.state().room().code(), white.playerToken(), null)));

        String code = fixture.startGame(0);
        assertThrows(NotAPlayerException.class, () -> fixture.report.execute(new NewReport(code, "stranger", null)));
        assertThrows(IllegalArgumentException.class, () -> fixture.report.execute(new NewReport(code, fixture.white.playerToken(), "x".repeat(201))));
        assertThrows(RoomNotFoundException.class, () -> fixture.getReports.execute("NOPE00"));
    }
}
