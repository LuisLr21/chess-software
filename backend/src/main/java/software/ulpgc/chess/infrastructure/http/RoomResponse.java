package software.ulpgc.chess.infrastructure.http;

import software.ulpgc.chess.application.RoomState;
import software.ulpgc.chess.domain.Color;
import software.ulpgc.chess.domain.Room;

import java.time.Instant;

public record RoomResponse(String code, String you, PlayerResponse white, PlayerResponse black, String drawOfferedBy,
                           String rematchOfferedBy, ClockResponse clock, GameResponse game) {
    public static RoomResponse from(RoomState state, String playerToken) {
        Room room = state.room();
        return new RoomResponse(
                room.code(),
                name(room.colorOf(playerToken).orElse(null)),
                PlayerResponse.from(room.white()),
                PlayerResponse.from(room.black()),
                name(room.drawOfferedBy()),
                name(room.rematchOfferedBy()),
                ClockResponse.from(state.game(), Instant.now()),
                GameResponse.from(state.game())
        );
    }

    private static String name(Color color) {
        return color == null ? null : color.name();
    }
}
