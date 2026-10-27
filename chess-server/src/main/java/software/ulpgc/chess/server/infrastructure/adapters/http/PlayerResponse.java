package software.ulpgc.chess.server.infrastructure.adapters.http;

import software.ulpgc.chess.server.application.domain.Seat;

public record PlayerResponse(String name, boolean joined) {
    public static PlayerResponse from(Seat seat) {
        return seat == null ? new PlayerResponse(null, false) : new PlayerResponse(seat.name(), true);
    }
}
