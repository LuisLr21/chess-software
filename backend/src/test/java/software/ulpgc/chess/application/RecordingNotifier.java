package software.ulpgc.chess.application;

import software.ulpgc.chess.domain.ChatMessage;

import java.util.ArrayList;
import java.util.List;

public class RecordingNotifier implements RoomNotifier {
    public final List<RoomState> rooms = new ArrayList<>();
    public final List<ChatMessage> messages = new ArrayList<>();

    @Override
    public void roomUpdated(RoomState state) {
        rooms.add(state);
    }

    @Override
    public void messageSent(ChatMessage message) {
        messages.add(message);
    }

    public RoomState lastRoom() {
        return rooms.getLast();
    }
}
