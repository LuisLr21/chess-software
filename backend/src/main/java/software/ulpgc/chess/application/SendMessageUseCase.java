package software.ulpgc.chess.application;

import software.ulpgc.chess.domain.ChatMessage;
import software.ulpgc.chess.domain.Color;
import software.ulpgc.chess.domain.Room;

import java.time.Clock;

public class SendMessageUseCase {
    private final RoomRepository rooms;
    private final ChatRepository chat;
    private final RoomNotifier notifier;
    private final Clock clock;

    public SendMessageUseCase(RoomRepository rooms, ChatRepository chat, RoomNotifier notifier, Clock clock) {
        this.rooms = rooms;
        this.chat = chat;
        this.notifier = notifier;
        this.clock = clock;
    }

    public ChatMessage execute(String code, String player, String text) {
        Room room = rooms.findByCode(code).orElseThrow(() -> new RoomNotFoundException(code));
        Color color = room.playerColor(player);
        String name = color == Color.White ? room.white().name() : room.black().name();
        ChatMessage message = ChatMessage.create(code, color, name, text, clock.instant());
        chat.save(message);
        notifier.messageSent(message);
        return message;
    }
}
