package software.ulpgc.chess.application;

public class RoomNotFoundException extends RuntimeException {
    public RoomNotFoundException(String code) {
        super("Room not found: " + code);
    }
}
