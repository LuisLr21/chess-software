package software.ulpgc.chess.core;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChessClockTest {
    private static final Instant START = Instant.parse("2026-10-22T10:00:00Z");
    private final ChessClock clock = ChessClock.of(Duration.ofMinutes(5));

    @Test
    void aStoppedClockDoesNotCount() {
        assertFalse(clock.isRunning());
        assertEquals(300_000, clock.remaining(Color.White, Color.White, START.plusSeconds(60)));
        assertFalse(clock.isExpired(Color.White, START.plusSeconds(600)));
    }

    @Test
    void onlyTheSideToMoveLosesTime() {
        ChessClock running = clock.start(START);
        assertEquals(290_000, running.remaining(Color.White, Color.White, START.plusSeconds(10)));
        assertEquals(300_000, running.remaining(Color.Black, Color.White, START.plusSeconds(10)));
    }

    @Test
    void movingStoresTheTimeLeftAndStartsTheOpponentsTurn() {
        ChessClock afterMove = clock.start(START).afterMove(Color.White, START.plusSeconds(10));
        assertEquals(290_000, afterMove.whiteMillis());
        assertEquals(START.plusSeconds(10), afterMove.turnStartedAt());
        assertEquals(295_000, afterMove.remaining(Color.Black, Color.Black, START.plusSeconds(15)));
    }

    @Test
    void theTimeLeftNeverGoesBelowZero() {
        ChessClock running = clock.start(START);
        assertEquals(0, running.remaining(Color.White, Color.White, START.plusSeconds(600)));
        assertTrue(running.isExpired(Color.White, START.plusSeconds(300)));
        assertFalse(running.isExpired(Color.White, START.plusSeconds(299)));
    }

    @Test
    void stoppingKeepsTheTimeLeft() {
        ChessClock stopped = clock.start(START).stop(Color.Black, START.plusSeconds(20));
        assertFalse(stopped.isRunning());
        assertEquals(280_000, stopped.blackMillis());
        assertEquals(300_000, stopped.whiteMillis());
    }

    @Test
    void movingWithAStoppedClockChangesNothing() {
        assertSame(clock, clock.afterMove(Color.White, START));
    }
}
