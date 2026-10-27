package software.ulpgc.chess.server.application.ports;

import software.ulpgc.chess.server.application.domain.ChatMessage;

import java.util.List;

public interface ChatRepository {
    void save(ChatMessage message);
    List<ChatMessage> findByRoom(String roomCode);
}
