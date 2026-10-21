package software.ulpgc.chess.infrastructure.http;

import io.javalin.config.RoutesConfig;
import io.javalin.http.Context;
import software.ulpgc.chess.application.CreateRoomUseCase;
import software.ulpgc.chess.application.DeclineDrawUseCase;
import software.ulpgc.chess.application.GameNotOverException;
import software.ulpgc.chess.application.GetMessagesUseCase;
import software.ulpgc.chess.application.GetRoomUseCase;
import software.ulpgc.chess.application.JoinRoomUseCase;
import software.ulpgc.chess.application.MoveInRoomUseCase;
import software.ulpgc.chess.application.OfferDrawUseCase;
import software.ulpgc.chess.application.RematchUseCase;
import software.ulpgc.chess.application.ResignUseCase;
import software.ulpgc.chess.application.RoomNotFoundException;
import software.ulpgc.chess.application.RoomState;
import software.ulpgc.chess.application.SendMessageUseCase;
import software.ulpgc.chess.domain.NotAPlayerException;
import software.ulpgc.chess.domain.NotYourTurnException;
import software.ulpgc.chess.domain.RoomFullException;
import software.ulpgc.chess.domain.Square;
import software.ulpgc.chess.domain.WaitingForOpponentException;

public class RoomController {
    private static final String PLAYER_TOKEN_HEADER = "X-Player-Token";

    private final CreateRoomUseCase createRoom;
    private final JoinRoomUseCase joinRoom;
    private final GetRoomUseCase getRoom;
    private final MoveInRoomUseCase moveInRoom;
    private final ResignUseCase resign;
    private final OfferDrawUseCase offerDraw;
    private final DeclineDrawUseCase declineDraw;
    private final RematchUseCase rematch;
    private final SendMessageUseCase sendMessage;
    private final GetMessagesUseCase getMessages;

    public RoomController(CreateRoomUseCase createRoom, JoinRoomUseCase joinRoom, GetRoomUseCase getRoom,
                          MoveInRoomUseCase moveInRoom, ResignUseCase resign, OfferDrawUseCase offerDraw,
                          DeclineDrawUseCase declineDraw, RematchUseCase rematch, SendMessageUseCase sendMessage,
                          GetMessagesUseCase getMessages) {
        this.createRoom = createRoom;
        this.joinRoom = joinRoom;
        this.getRoom = getRoom;
        this.moveInRoom = moveInRoom;
        this.resign = resign;
        this.offerDraw = offerDraw;
        this.declineDraw = declineDraw;
        this.rematch = rematch;
        this.sendMessage = sendMessage;
        this.getMessages = getMessages;
    }

    public void register(RoutesConfig routes) {
        routes.post("/rooms", ctx -> {
            RoomRequest request = roomRequest(ctx);
            ctx.status(201).json(SessionResponse.from(createRoom.execute(request.name(), request.minutesOrZero())));
        });

        routes.post("/rooms/{code}/players", ctx ->
                ctx.status(201).json(SessionResponse.from(joinRoom.execute(code(ctx), roomRequest(ctx).name()))));

        routes.get("/rooms/{code}", ctx -> ctx.json(RoomResponse.from(getRoom.execute(code(ctx)), token(ctx))));

        routes.post("/rooms/{code}/moves", ctx -> {
            MoveRequest request = ctx.bodyAsClass(MoveRequest.class);
            RoomState state = moveInRoom.execute(code(ctx), token(ctx),
                    Square.at(request.from()), Square.at(request.to()), request.promotionType());
            ctx.json(RoomResponse.from(state, token(ctx)));
        });

        routes.post("/rooms/{code}/resign", ctx -> ctx.json(RoomResponse.from(resign.execute(code(ctx), token(ctx)), token(ctx))));
        routes.post("/rooms/{code}/draw-offer", ctx -> ctx.json(RoomResponse.from(offerDraw.execute(code(ctx), token(ctx)), token(ctx))));
        routes.delete("/rooms/{code}/draw-offer", ctx -> ctx.json(RoomResponse.from(declineDraw.execute(code(ctx), token(ctx)), token(ctx))));
        routes.post("/rooms/{code}/rematch", ctx -> ctx.json(RoomResponse.from(rematch.execute(code(ctx), token(ctx)), token(ctx))));

        routes.get("/rooms/{code}/messages", ctx ->
                ctx.json(getMessages.execute(code(ctx)).stream().map(ChatMessageResponse::from).toList()));
        routes.post("/rooms/{code}/messages", ctx -> {
            MessageRequest request = ctx.bodyAsClass(MessageRequest.class);
            ctx.status(201).json(ChatMessageResponse.from(sendMessage.execute(code(ctx), token(ctx), request.text())));
        });

        routes.exception(RoomNotFoundException.class, (e, ctx) -> ctx.status(404).json(new ErrorResponse(e.getMessage())));
        routes.exception(RoomFullException.class, (e, ctx) -> ctx.status(409).json(new ErrorResponse(e.getMessage())));
        routes.exception(WaitingForOpponentException.class, (e, ctx) -> ctx.status(409).json(new ErrorResponse(e.getMessage())));
        routes.exception(GameNotOverException.class, (e, ctx) -> ctx.status(409).json(new ErrorResponse(e.getMessage())));
        routes.exception(NotYourTurnException.class, (e, ctx) -> ctx.status(403).json(new ErrorResponse(e.getMessage())));
        routes.exception(NotAPlayerException.class, (e, ctx) -> ctx.status(403).json(new ErrorResponse(e.getMessage())));
    }

    private static RoomRequest roomRequest(Context ctx) {
        return ctx.body().isBlank() ? new RoomRequest(null, null) : ctx.bodyAsClass(RoomRequest.class);
    }

    private static String token(Context ctx) {
        return ctx.header(PLAYER_TOKEN_HEADER);
    }

    private static String code(Context ctx) {
        return ctx.pathParam("code").toUpperCase();
    }
}
