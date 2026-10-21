package software.ulpgc.chess.infrastructure.persistence;

import software.ulpgc.chess.application.ChatRepository;
import software.ulpgc.chess.domain.ChatMessage;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class InMemoryChatRepository implements ChatRepository {
    private final List<ChatMessage> messages = new CopyOnWriteArrayList<>();

    @Override
    public void save(ChatMessage message) {
        messages.add(message);
    }

    @Override
    public List<ChatMessage> findByRoom(String roomCode) {
        return messages.stream().filter(message -> message.roomCode().equals(roomCode)).toList();
    }
}
