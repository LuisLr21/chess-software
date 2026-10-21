package software.ulpgc.chess.domain;

public class RoomFullException extends RuntimeException {
    public RoomFullException(String code) {
        super("Room is full: " + code);
    }
}
