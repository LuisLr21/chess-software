package software.ulpgc.chess.core;

import java.time.Duration;
import java.time.Instant;

public record ChessClock(long initialMillis, long whiteMillis, long blackMillis, Instant turnStartedAt) {

    public static ChessClock of(Duration timeControl) {
        long millis = timeControl.toMillis();
        return new ChessClock(millis, millis, millis, null);
    }

    public boolean isRunning() {
        return turnStartedAt != null;
    }

    public ChessClock start(Instant now) {
        return new ChessClock(initialMillis, whiteMillis, blackMillis, now);
    }

    public long remaining(Color color, Color sideToMove, Instant now) {
        long stored = color == Color.White ? whiteMillis : blackMillis;
        if (!isRunning() || color != sideToMove) return stored;
        return Math.max(0, stored - Duration.between(turnStartedAt, now).toMillis());
    }

    public boolean isExpired(Color sideToMove, Instant now) {
        return isRunning() && remaining(sideToMove, sideToMove, now) == 0;
    }

    public ChessClock afterMove(Color mover, Instant now) {
        if (!isRunning()) return this;
        long left = remaining(mover, mover, now);
        return mover == Color.White
                ? new ChessClock(initialMillis, left, blackMillis, now)
                : new ChessClock(initialMillis, whiteMillis, left, now);
    }

    public ChessClock stop(Color sideToMove, Instant now) {
        if (!isRunning()) return this;
        long left = remaining(sideToMove, sideToMove, now);
        return sideToMove == Color.White
                ? new ChessClock(initialMillis, left, blackMillis, null)
                : new ChessClock(initialMillis, whiteMillis, left, null);
    }
}
