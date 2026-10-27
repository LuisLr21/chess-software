package software.ulpgc.chess.server;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import software.ulpgc.chess.server.application.ports.ChatRepository;
import software.ulpgc.chess.server.application.ports.ChessEngine;
import software.ulpgc.chess.server.application.ports.GameRepository;
import software.ulpgc.chess.server.application.ports.ReportRepository;
import software.ulpgc.chess.server.application.ports.RoomRepository;
import software.ulpgc.chess.server.application.usecases.AbandonGameUseCase;
import software.ulpgc.chess.server.application.usecases.CreateGameUseCase;
import software.ulpgc.chess.server.application.usecases.CreateRoomUseCase;
import software.ulpgc.chess.server.application.usecases.DeclineDrawUseCase;
import software.ulpgc.chess.server.application.usecases.GetAiLevelsUseCase;
import software.ulpgc.chess.server.application.usecases.GetGameUseCase;
import software.ulpgc.chess.server.application.usecases.GetLegalMovesUseCase;
import software.ulpgc.chess.server.application.usecases.GetMessagesUseCase;
import software.ulpgc.chess.server.application.usecases.GetReportsUseCase;
import software.ulpgc.chess.server.application.usecases.GetRoomUseCase;
import software.ulpgc.chess.server.application.usecases.JoinRoomUseCase;
import software.ulpgc.chess.server.application.usecases.MoveInRoomUseCase;
import software.ulpgc.chess.server.application.usecases.MovePieceUseCase;
import software.ulpgc.chess.server.application.usecases.OfferDrawUseCase;
import software.ulpgc.chess.server.application.usecases.PlayAiMoveUseCase;
import software.ulpgc.chess.server.application.usecases.RematchUseCase;
import software.ulpgc.chess.server.application.usecases.ReportOpponentUseCase;
import software.ulpgc.chess.server.application.usecases.ResignUseCase;
import software.ulpgc.chess.server.application.usecases.SendMessageUseCase;
import software.ulpgc.chess.server.infrastructure.adapters.ai.HttpChessEngine;
import software.ulpgc.chess.server.infrastructure.adapters.http.ErrorHandlers;
import software.ulpgc.chess.server.infrastructure.adapters.http.endpoints.AiLevelsEndPoint;
import software.ulpgc.chess.server.infrastructure.adapters.http.endpoints.AiMoveEndPoint;
import software.ulpgc.chess.server.infrastructure.adapters.http.endpoints.CreateGameEndPoint;
import software.ulpgc.chess.server.infrastructure.adapters.http.endpoints.CreateRoomEndPoint;
import software.ulpgc.chess.server.infrastructure.adapters.http.endpoints.GetGameEndPoint;
import software.ulpgc.chess.server.infrastructure.adapters.http.endpoints.GetMessagesEndPoint;
import software.ulpgc.chess.server.infrastructure.adapters.http.endpoints.GetReportsEndPoint;
import software.ulpgc.chess.server.infrastructure.adapters.http.endpoints.GetRoomEndPoint;
import software.ulpgc.chess.server.infrastructure.adapters.http.endpoints.JoinRoomEndPoint;
import software.ulpgc.chess.server.infrastructure.adapters.http.endpoints.LegalMovesEndPoint;
import software.ulpgc.chess.server.infrastructure.adapters.http.endpoints.MoveInRoomEndPoint;
import software.ulpgc.chess.server.infrastructure.adapters.http.endpoints.MovePieceEndPoint;
import software.ulpgc.chess.server.infrastructure.adapters.http.endpoints.ReportOpponentEndPoint;
import software.ulpgc.chess.server.infrastructure.adapters.http.endpoints.RoomActionEndPoint;
import software.ulpgc.chess.server.infrastructure.adapters.http.endpoints.SendMessageEndPoint;
import software.ulpgc.chess.server.infrastructure.adapters.persistence.InMemoryChatRepository;
import software.ulpgc.chess.server.infrastructure.adapters.persistence.InMemoryGameRepository;
import software.ulpgc.chess.server.infrastructure.adapters.persistence.InMemoryReportRepository;
import software.ulpgc.chess.server.infrastructure.adapters.persistence.InMemoryRoomRepository;
import software.ulpgc.chess.server.infrastructure.adapters.persistence.SqliteChatRepository;
import software.ulpgc.chess.server.infrastructure.adapters.persistence.SqliteDatabase;
import software.ulpgc.chess.server.infrastructure.adapters.persistence.SqliteGameRepository;
import software.ulpgc.chess.server.infrastructure.adapters.persistence.SqliteReportRepository;
import software.ulpgc.chess.server.infrastructure.adapters.persistence.SqliteRoomRepository;
import software.ulpgc.chess.server.infrastructure.adapters.websocket.AbandonmentWatcher;
import software.ulpgc.chess.server.infrastructure.adapters.websocket.WebSocketRoomNotifier;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;

public class Main {
    public static void main(String[] args) {
        GameRepository games;
        RoomRepository rooms;
        ChatRepository chat;
        ReportRepository reports;
        if ("memory".equals(System.getenv("STORAGE"))) {
            games = new InMemoryGameRepository();
            rooms = new InMemoryRoomRepository();
            chat = new InMemoryChatRepository();
            reports = new InMemoryReportRepository();
        } else {
            SqliteDatabase database = new SqliteDatabase(System.getenv().getOrDefault("DATABASE", "chess.db"));
            games = new SqliteGameRepository(database);
            rooms = new SqliteRoomRepository(database);
            chat = new SqliteChatRepository(database);
            reports = new SqliteReportRepository(database);
        }

        ChessEngine engine = new HttpChessEngine(System.getenv().getOrDefault("AI_SERVICE", "http://localhost:7071"));

        Clock clock = Clock.systemUTC();
        WebSocketRoomNotifier notifier = new WebSocketRoomNotifier(rooms);
        GetRoomUseCase getRoom = new GetRoomUseCase(games, rooms, notifier, clock);
        Duration abandonAfter = Duration.ofSeconds(Long.parseLong(System.getenv().getOrDefault("ABANDON_SECONDS", "60")));
        notifier.watchAbandonment(new AbandonmentWatcher(new AbandonGameUseCase(games, rooms, getRoom, notifier, clock), abandonAfter));

        CreateGameUseCase createGame = new CreateGameUseCase(games);
        GetGameUseCase getGame = new GetGameUseCase(games);
        GetLegalMovesUseCase legalMoves = new GetLegalMovesUseCase(games);
        MovePieceUseCase movePiece = new MovePieceUseCase(games);
        PlayAiMoveUseCase aiMove = new PlayAiMoveUseCase(games, engine);
        GetAiLevelsUseCase aiLevels = new GetAiLevelsUseCase(engine);
        CreateRoomUseCase createRoom = new CreateRoomUseCase(games, rooms);
        JoinRoomUseCase joinRoom = new JoinRoomUseCase(games, rooms, notifier, clock);
        MoveInRoomUseCase moveInRoom = new MoveInRoomUseCase(games, rooms, getRoom, notifier, clock);
        ResignUseCase resign = new ResignUseCase(games, rooms, getRoom, notifier, clock);
        OfferDrawUseCase offerDraw = new OfferDrawUseCase(games, rooms, getRoom, notifier, clock);
        DeclineDrawUseCase declineDraw = new DeclineDrawUseCase(rooms, getRoom, notifier);
        RematchUseCase rematch = new RematchUseCase(games, rooms, getRoom, notifier, clock);
        GetMessagesUseCase getMessages = new GetMessagesUseCase(rooms, chat);
        SendMessageUseCase sendMessage = new SendMessageUseCase(rooms, chat, notifier, clock);
        ReportOpponentUseCase reportOpponent = new ReportOpponentUseCase(reports, getRoom, notifier, clock);
        GetReportsUseCase getReports = new GetReportsUseCase(rooms, reports);

        Javalin app = Javalin.create(config -> {
            config.staticFiles.add(staticFiles -> {
                staticFiles.hostedPath = "/";
                staticFiles.directory = frontendDirectory();
                staticFiles.location = Location.EXTERNAL;
            });
            config.routes.post("/games", ctx -> CreateGameEndPoint.use(createGame, ctx).execute());
            config.routes.get("/games/{id}", ctx -> GetGameEndPoint.use(getGame, ctx).execute());
            config.routes.get("/games/{id}/moves", ctx -> LegalMovesEndPoint.use(legalMoves, ctx).execute());
            config.routes.post("/games/{id}/moves", ctx -> MovePieceEndPoint.use(movePiece, ctx).execute());
            config.routes.post("/games/{id}/ai-move", ctx -> AiMoveEndPoint.use(aiMove, ctx).execute());
            config.routes.get("/ai/levels", ctx -> AiLevelsEndPoint.use(aiLevels, ctx).execute());

            config.routes.post("/rooms", ctx -> CreateRoomEndPoint.use(createRoom, ctx).execute());
            config.routes.post("/rooms/{code}/players", ctx -> JoinRoomEndPoint.use(joinRoom, ctx).execute());
            config.routes.get("/rooms/{code}", ctx -> GetRoomEndPoint.use(getRoom, ctx).execute());
            config.routes.post("/rooms/{code}/moves", ctx -> MoveInRoomEndPoint.use(moveInRoom, ctx).execute());
            config.routes.post("/rooms/{code}/resign", ctx -> RoomActionEndPoint.use(resign, ctx).execute());
            config.routes.post("/rooms/{code}/draw-offer", ctx -> RoomActionEndPoint.use(offerDraw, ctx).execute());
            config.routes.delete("/rooms/{code}/draw-offer", ctx -> RoomActionEndPoint.use(declineDraw, ctx).execute());
            config.routes.post("/rooms/{code}/rematch", ctx -> RoomActionEndPoint.use(rematch, ctx).execute());
            config.routes.get("/rooms/{code}/messages", ctx -> GetMessagesEndPoint.use(getMessages, ctx).execute());
            config.routes.post("/rooms/{code}/messages", ctx -> SendMessageEndPoint.use(sendMessage, ctx).execute());
            config.routes.post("/rooms/{code}/reports", ctx -> ReportOpponentEndPoint.use(reportOpponent, ctx).execute());
            config.routes.get("/rooms/{code}/reports", ctx -> GetReportsEndPoint.use(getReports, ctx).execute());
            ErrorHandlers.register(config.routes);
            notifier.register(config.routes);
        });

        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "7070"));
        app.start(port);
    }

    private static String frontendDirectory() {
        String configured = System.getenv("FRONTEND");
        if (configured != null) return configured;
        return Files.isDirectory(Path.of("frontend")) ? "frontend" : "../frontend";
    }
}
