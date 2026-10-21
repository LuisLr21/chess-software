package software.ulpgc.chess.application;

import software.ulpgc.chess.domain.ChatMessage;

public interface RoomNotifier {
    void roomUpdated(RoomState state);
    void messageSent(ChatMessage message);
}
