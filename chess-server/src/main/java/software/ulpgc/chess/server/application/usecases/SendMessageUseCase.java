package software.ulpgc.chess.server.application.usecases;

import software.ulpgc.chess.core.Color;
import software.ulpgc.chess.server.application.UseCase;
import software.ulpgc.chess.server.application.domain.ChatMessage;
import software.ulpgc.chess.server.application.domain.Room;
import software.ulpgc.chess.server.application.ports.ChatRepository;
import software.ulpgc.chess.server.application.ports.RoomNotifier;
import software.ulpgc.chess.server.application.ports.RoomRepository;

import java.time.Clock;

public class SendMessageUseCase implements UseCase<NewMessage, ChatMessage> {
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

    @Override
    public ChatMessage execute(NewMessage newMessage) {
        String code = newMessage.code();
        Room room = rooms.findByCode(code).orElseThrow(() -> new RoomNotFoundException(code));
        Color color = room.playerColor(newMessage.player());
        String name = color == Color.White ? room.white().name() : room.black().name();
        ChatMessage message = ChatMessage.create(code, color, name, newMessage.text(), clock.instant());
        chat.save(message);
        notifier.messageSent(message);
        return message;
    }
}
