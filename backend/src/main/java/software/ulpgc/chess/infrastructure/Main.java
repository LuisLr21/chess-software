package software.ulpgc.chess.infrastructure;

import io.javalin.Javalin;
import io.javalin.plugin.bundled.CorsPluginConfig;
import software.ulpgc.chess.application.CreateGameUseCase;
import software.ulpgc.chess.application.CreateRoomUseCase;
import software.ulpgc.chess.application.GameRepository;
import software.ulpgc.chess.application.GetGameUseCase;
import software.ulpgc.chess.application.GetLegalMovesUseCase;
import software.ulpgc.chess.application.GetRoomUseCase;
import software.ulpgc.chess.application.JoinRoomUseCase;
import software.ulpgc.chess.application.MoveInRoomUseCase;
import software.ulpgc.chess.application.MovePieceUseCase;
import software.ulpgc.chess.application.RoomRepository;
import software.ulpgc.chess.infrastructure.http.GameController;
import software.ulpgc.chess.infrastructure.http.RoomController;
import software.ulpgc.chess.infrastructure.persistence.InMemoryGameRepository;
import software.ulpgc.chess.infrastructure.persistence.InMemoryRoomRepository;
import software.ulpgc.chess.infrastructure.persistence.SqliteDatabase;
import software.ulpgc.chess.infrastructure.persistence.SqliteGameRepository;
import software.ulpgc.chess.infrastructure.persistence.SqliteRoomRepository;
import software.ulpgc.chess.infrastructure.websocket.WebSocketRoomNotifier;

public class Main {
    public static void main(String[] args) {
        GameRepository games;
        RoomRepository rooms;
        if ("memory".equals(System.getenv("STORAGE"))) {
            games = new InMemoryGameRepository();
            rooms = new InMemoryRoomRepository();
        } else {
            SqliteDatabase database = new SqliteDatabase(System.getenv().getOrDefault("DATABASE", "chess.db"));
            games = new SqliteGameRepository(database);
            rooms = new SqliteRoomRepository(database);
        }

        WebSocketRoomNotifier notifier = new WebSocketRoomNotifier();
        GetRoomUseCase getRoom = new GetRoomUseCase(games, rooms);

        GameController gameController = new GameController(
                new CreateGameUseCase(games),
                new GetGameUseCase(games),
                new MovePieceUseCase(games),
                new GetLegalMovesUseCase(games)
        );
        RoomController roomController = new RoomController(
                new CreateRoomUseCase(games, rooms),
                new JoinRoomUseCase(games, rooms, notifier),
                getRoom,
                new MoveInRoomUseCase(games, getRoom, notifier)
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
