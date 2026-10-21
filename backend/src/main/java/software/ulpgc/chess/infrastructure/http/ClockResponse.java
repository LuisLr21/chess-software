package software.ulpgc.chess.infrastructure.http;

import software.ulpgc.chess.domain.ChessClock;
import software.ulpgc.chess.domain.Color;
import software.ulpgc.chess.domain.Game;

import java.time.Instant;

public record ClockResponse(long initialMillis, long whiteMillis, long blackMillis, String running) {
    public static ClockResponse from(Game game, Instant now) {
        ChessClock clock = game.clock();
        if (clock == null) return null;
        Color side = game.position().sideToMove();
        boolean running = clock.isRunning() && !game.isOver();
        return new ClockResponse(
                clock.initialMillis(),
                clock.remaining(Color.White, side, now),
                clock.remaining(Color.Black, side, now),
                running ? side.name() : null
        );
    }
}
