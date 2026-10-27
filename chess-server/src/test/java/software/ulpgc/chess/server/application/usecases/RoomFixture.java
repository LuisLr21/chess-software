package software.ulpgc.chess.server.application.usecases;

import software.ulpgc.chess.server.application.ports.ChatRepository;
import software.ulpgc.chess.server.application.ports.GameRepository;
import software.ulpgc.chess.server.application.ports.ReportRepository;
import software.ulpgc.chess.server.application.ports.RoomRepository;
import software.ulpgc.chess.server.infrastructure.adapters.persistence.InMemoryChatRepository;
import software.ulpgc.chess.server.infrastructure.adapters.persistence.InMemoryGameRepository;
import software.ulpgc.chess.server.infrastructure.adapters.persistence.InMemoryReportRepository;
import software.ulpgc.chess.server.infrastructure.adapters.persistence.InMemoryRoomRepository;

import java.time.Instant;

public class RoomFixture {
    public static final Instant START = Instant.parse("2026-10-22T10:00:00Z");

    public final GameRepository games = new InMemoryGameRepository();
    public final RoomRepository rooms = new InMemoryRoomRepository();
    public final ChatRepository chat = new InMemoryChatRepository();
    public final ReportRepository reports = new InMemoryReportRepository();
    public final RecordingNotifier notifier = new RecordingNotifier();
    public final MutableClock clock = new MutableClock(START);

    public final GetRoomUseCase getRoom = new GetRoomUseCase(games, rooms, notifier, clock);
    public final CreateRoomUseCase createRoom = new CreateRoomUseCase(games, rooms);
    public final JoinRoomUseCase joinRoom = new JoinRoomUseCase(games, rooms, notifier, clock);
    public final MoveInRoomUseCase move = new MoveInRoomUseCase(games, rooms, getRoom, notifier, clock);
    public final ResignUseCase resign = new ResignUseCase(games, rooms, getRoom, notifier, clock);
    public final OfferDrawUseCase offerDraw = new OfferDrawUseCase(games, rooms, getRoom, notifier, clock);
    public final DeclineDrawUseCase declineDraw = new DeclineDrawUseCase(rooms, getRoom, notifier);
    public final RematchUseCase rematch = new RematchUseCase(games, rooms, getRoom, notifier, clock);
    public final SendMessageUseCase sendMessage = new SendMessageUseCase(rooms, chat, notifier, clock);
    public final GetMessagesUseCase getMessages = new GetMessagesUseCase(rooms, chat);
    public final AbandonGameUseCase abandon = new AbandonGameUseCase(games, rooms, getRoom, notifier, clock);
    public final ReportOpponentUseCase report = new ReportOpponentUseCase(reports, getRoom, notifier, clock);
    public final GetReportsUseCase getReports = new GetReportsUseCase(rooms, reports);

    public PlayerSession white;
    public PlayerSession black;

    public String startGame(int minutes) {
        white = createRoom.execute(new NewRoom("Luis", minutes));
        String code = white.state().room().code();
        black = joinRoom.execute(new RoomEntry(code, "Ana"));
        return code;
    }
}
