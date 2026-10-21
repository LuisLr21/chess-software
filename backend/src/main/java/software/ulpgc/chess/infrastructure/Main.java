package software.ulpgc.chess.infrastructure;

import io.javalin.Javalin;
import io.javalin.plugin.bundled.CorsPluginConfig;
import software.ulpgc.chess.application.ChatRepository;
import software.ulpgc.chess.application.CreateGameUseCase;
import software.ulpgc.chess.application.CreateRoomUseCase;
import software.ulpgc.chess.application.DeclineDrawUseCase;
import software.ulpgc.chess.application.GameRepository;
import software.ulpgc.chess.application.GetGameUseCase;
import software.ulpgc.chess.application.GetLegalMovesUseCase;
import software.ulpgc.chess.application.GetMessagesUseCase;
import software.ulpgc.chess.application.GetRoomUseCase;
import software.ulpgc.chess.application.JoinRoomUseCase;
import software.ulpgc.chess.application.MoveInRoomUseCase;
import software.ulpgc.chess.application.MovePieceUseCase;
import software.ulpgc.chess.application.OfferDrawUseCase;
import software.ulpgc.chess.application.RematchUseCase;
import software.ulpgc.chess.application.ResignUseCase;
import software.ulpgc.chess.application.RoomRepository;
import software.ulpgc.chess.application.SendMessageUseCase;
import software.ulpgc.chess.infrastructure.http.GameController;
import software.ulpgc.chess.infrastructure.http.RoomController;
import software.ulpgc.chess.infrastructure.persistence.InMemoryChatRepository;
import software.ulpgc.chess.infrastructure.persistence.InMemoryGameRepository;
import software.ulpgc.chess.infrastructure.persistence.InMemoryRoomRepository;
import software.ulpgc.chess.infrastructure.persistence.SqliteChatRepository;
import software.ulpgc.chess.infrastructure.persistence.SqliteDatabase;
import software.ulpgc.chess.infrastructure.persistence.SqliteGameRepository;
import software.ulpgc.chess.infrastructure.persistence.SqliteRoomRepository;
import software.ulpgc.chess.infrastructure.websocket.WebSocketRoomNotifier;

import java.time.Clock;

public class Main {
    public static void main(String[] args) {
        GameRepository games;
        RoomRepository rooms;
        ChatRepository chat;
        if ("memory".equals(System.getenv("STORAGE"))) {
            games = new InMemoryGameRepository();
            rooms = new InMemoryRoomRepository();
            chat = new InMemoryChatRepository();
        } else {
            SqliteDatabase database = new SqliteDatabase(System.getenv().getOrDefault("DATABASE", "chess.db"));
            games = new SqliteGameRepository(database);
            rooms = new SqliteRoomRepository(database);
            chat = new SqliteChatRepository(database);
        }

        Clock clock = Clock.systemUTC();
        WebSocketRoomNotifier notifier = new WebSocketRoomNotifier(rooms);
        GetRoomUseCase getRoom = new GetRoomUseCase(games, rooms, notifier, clock);

        GameController gameController = new GameController(
                new CreateGameUseCase(games),
                new GetGameUseCase(games),
                new MovePieceUseCase(games),
                new GetLegalMovesUseCase(games)
        );
        RoomController roomController = new RoomController(
                new CreateRoomUseCase(games, rooms),
                new JoinRoomUseCase(games, rooms, notifier, clock),
                getRoom,
                new MoveInRoomUseCase(games, rooms, getRoom, notifier, clock),
                new ResignUseCase(games, rooms, getRoom, notifier, clock),
                new OfferDrawUseCase(games, rooms, getRoom, notifier, clock),
                new DeclineDrawUseCase(rooms, getRoom, notifier),
                new RematchUseCase(games, rooms, getRoom, notifier, clock),
                new SendMessageUseCase(rooms, chat, notifier, clock),
                new GetMessagesUseCase(rooms, chat)
        );

        Javalin app = Javalin.create(config -> {
            config.bundledPlugins.enableCors(cors -> cors.addRule(CorsPluginConfig.CorsRule::anyHost));
            gameController.register(config.routes);
            roomController.register(config.routes);
            notifier.register(config.routes);
        });

        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "7070"));
        app.start(port);
    }
}
