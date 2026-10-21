package software.ulpgc.chess.infrastructure.http;

public record RoomRequest(String name, Integer minutes) {
    public int minutesOrZero() {
        return minutes == null ? 0 : minutes;
    }
}
