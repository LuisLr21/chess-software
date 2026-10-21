package software.ulpgc.chess.application;

import software.ulpgc.chess.domain.Game;
import software.ulpgc.chess.domain.Room;

public class GetRoomUseCase {
    private final GameRepository games;
    private final RoomRepository rooms;

    public GetRoomUseCase(GameRepository games, RoomRepository rooms) {
        this.games = games;
        this.rooms = rooms;
    }

    public RoomState execute(String code) {
        Room room = rooms.findByCode(code).orElseThrow(() -> new RoomNotFoundException(code));
        Game game = games.findById(room.gameId()).orElseThrow(() -> new GameNotFoundException(room.gameId()));
        return new RoomState(room, game);
    }
}
