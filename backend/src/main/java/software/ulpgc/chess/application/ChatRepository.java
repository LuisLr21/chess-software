package software.ulpgc.chess.application;

import software.ulpgc.chess.domain.ChatMessage;

import java.util.List;

public interface ChatRepository {
    void save(ChatMessage message);
    List<ChatMessage> findByRoom(String roomCode);
}
