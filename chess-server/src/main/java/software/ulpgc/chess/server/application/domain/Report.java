package software.ulpgc.chess.server.application.domain;

import software.ulpgc.chess.core.ChessClock;
import software.ulpgc.chess.core.Color;
import software.ulpgc.chess.core.Game;

import java.time.Instant;

public record Report(String roomCode, String gameId, Color reporter, Color reported, String reason,
                     Long reportedTimeUsedMillis, int reportedMoves, Instant reportedAt) {
    private static final int MAX_REASON_LENGTH = 200;
    private static final String DEFAULT_REASON = "Mala deportividad";

    public static Report of(String roomCode, Game game, Color reporter, String reason, Instant now) {
        Color reported = reporter.opposite();
        return new Report(roomCode, game.id(), reporter, reported, clean(reason),
                timeUsed(game, reported, now), movesOf(game, reported), now);
    }

    private static String clean(String reason) {
        String clean = reason == null || reason.isBlank() ? DEFAULT_REASON : reason.trim();
        if (clean.length() > MAX_REASON_LENGTH) throw new IllegalArgumentException("Reason longer than " + MAX_REASON_LENGTH + " characters");
        return clean;
    }

    private static Long timeUsed(Game game, Color color, Instant now) {
        ChessClock clock = game.clock();
        if (clock == null) return null;
        return clock.initialMillis() - clock.remaining(color, game.position().sideToMove(), now);
    }

    private static int movesOf(Game game, Color color) {
        int plies = game.history().size();
        return color == Color.White ? (plies + 1) / 2 : plies / 2;
    }
}
