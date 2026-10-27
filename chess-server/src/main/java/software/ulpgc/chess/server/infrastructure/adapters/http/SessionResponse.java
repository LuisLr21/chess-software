package software.ulpgc.chess.server.infrastructure.adapters.http;

import software.ulpgc.chess.server.application.usecases.PlayerSession;

public record SessionResponse(String color, String playerToken, RoomResponse room) {
    public static SessionResponse from(PlayerSession session) {
        return new SessionResponse(session.color().name(), session.playerToken(), RoomResponse.from(session.state(), session.playerToken()));
    }
}
