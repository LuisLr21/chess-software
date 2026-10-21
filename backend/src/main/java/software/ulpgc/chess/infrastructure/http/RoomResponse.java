package software.ulpgc.chess.infrastructure.http;

import software.ulpgc.chess.application.RoomState;

public record RoomResponse(String code, boolean whiteJoined, boolean blackJoined, GameResponse game) {
    public static RoomResponse from(RoomState state) {
        return new RoomResponse(state.room().code(), true, state.room().isFull(), GameResponse.from(state.game()));
    }
}
