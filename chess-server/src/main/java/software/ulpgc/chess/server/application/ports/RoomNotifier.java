package software.ulpgc.chess.server.application.ports;

import software.ulpgc.chess.server.application.domain.ChatMessage;
import software.ulpgc.chess.server.application.domain.Report;
import software.ulpgc.chess.server.application.usecases.RoomState;

public interface RoomNotifier {
    void roomUpdated(RoomState state);
    void messageSent(ChatMessage message);
    void reportFiled(Report report);
}
