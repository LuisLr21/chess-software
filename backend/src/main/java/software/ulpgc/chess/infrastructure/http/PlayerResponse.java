package software.ulpgc.chess.infrastructure.http;

import software.ulpgc.chess.domain.Seat;

public record PlayerResponse(String name, boolean joined) {
    public static PlayerResponse from(Seat seat) {
        return seat == null ? new PlayerResponse(null, false) : new PlayerResponse(seat.name(), true);
    }
}
