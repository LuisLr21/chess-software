package software.ulpgc.chess.application;

import software.ulpgc.chess.domain.ChatMessage;

import java.util.List;

public class GetMessagesUseCase {
    private final RoomRepository rooms;
    private final ChatRepository chat;

    public GetMessagesUseCase(RoomRepository rooms, ChatRepository chat) {
        this.rooms = rooms;
        this.chat = chat;
    }

    public List<ChatMessage> execute(String code) {
        if (rooms.findByCode(code).isEmpty()) throw new RoomNotFoundException(code);
        return chat.findByRoom(code);
    }
}
