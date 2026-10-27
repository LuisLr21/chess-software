package software.ulpgc.chess.server.application.usecases;

import software.ulpgc.chess.server.application.UseCase;
import software.ulpgc.chess.server.application.domain.ChatMessage;
import software.ulpgc.chess.server.application.ports.ChatRepository;
import software.ulpgc.chess.server.application.ports.RoomRepository;

import java.util.List;

public class GetMessagesUseCase implements UseCase<String, List<ChatMessage>> {
    private final RoomRepository rooms;
    private final ChatRepository chat;

    public GetMessagesUseCase(RoomRepository rooms, ChatRepository chat) {
        this.rooms = rooms;
        this.chat = chat;
    }

    @Override
    public List<ChatMessage> execute(String code) {
        if (rooms.findByCode(code).isEmpty()) throw new RoomNotFoundException(code);
        return chat.findByRoom(code);
    }
}
