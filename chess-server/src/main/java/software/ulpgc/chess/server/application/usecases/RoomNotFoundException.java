package software.ulpgc.chess.server.application.usecases;

public class RoomNotFoundException extends RuntimeException {
    public RoomNotFoundException(String code) {
        super("Room not found: " + code);
    }
}
