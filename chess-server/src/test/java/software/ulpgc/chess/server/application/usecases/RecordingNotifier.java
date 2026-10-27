package software.ulpgc.chess.server.application.usecases;

import software.ulpgc.chess.server.application.domain.ChatMessage;
import software.ulpgc.chess.server.application.domain.Report;
import software.ulpgc.chess.server.application.ports.RoomNotifier;

import java.util.ArrayList;
import java.util.List;

public class RecordingNotifier implements RoomNotifier {
    public final List<RoomState> rooms = new ArrayList<>();
    public final List<ChatMessage> messages = new ArrayList<>();
    public final List<Report> reports = new ArrayList<>();

    @Override
    public void roomUpdated(RoomState state) {
        rooms.add(state);
    }

    @Override
    public void messageSent(ChatMessage message) {
        messages.add(message);
    }

    @Override
    public void reportFiled(Report report) {
        reports.add(report);
    }

    public RoomState lastRoom() {
        return rooms.getLast();
    }
}
