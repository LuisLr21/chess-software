package software.ulpgc.chess.infrastructure.http;

import io.javalin.config.RoutesConfig;
import io.javalin.http.Context;
import software.ulpgc.chess.application.CreateRoomUseCase;
import software.ulpgc.chess.application.GetRoomUseCase;
import software.ulpgc.chess.application.JoinRoomUseCase;
import software.ulpgc.chess.application.MoveInRoomUseCase;
import software.ulpgc.chess.application.RoomNotFoundException;
import software.ulpgc.chess.application.RoomState;
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

    public RoomController(CreateRoomUseCase createRoom, JoinRoomUseCase joinRoom, GetRoomUseCase getRoom,
                          MoveInRoomUseCase moveInRoom) {
        this.createRoom = createRoom;
        this.joinRoom = joinRoom;
        this.getRoom = getRoom;
        this.moveInRoom = moveInRoom;
    }

    public void register(RoutesConfig routes) {
        routes.post("/rooms", ctx -> ctx.status(201).json(SessionResponse.from(createRoom.execute())));

        routes.post("/rooms/{code}/players", ctx -> ctx.status(201).json(SessionResponse.from(joinRoom.execute(code(ctx)))));

        routes.get("/rooms/{code}", ctx -> ctx.json(RoomResponse.from(getRoom.execute(code(ctx)))));

        routes.post("/rooms/{code}/moves", ctx -> {
            MoveRequest request = ctx.bodyAsClass(MoveRequest.class);
            RoomState state = moveInRoom.execute(code(ctx), ctx.header(PLAYER_TOKEN_HEADER),
                    Square.at(request.from()), Square.at(request.to()), request.promotionType());
            ctx.json(RoomResponse.from(state));
        });

        routes.exception(RoomNotFoundException.class, (e, ctx) -> ctx.status(404).json(new ErrorResponse(e.getMessage())));
        routes.exception(RoomFullException.class, (e, ctx) -> ctx.status(409).json(new ErrorResponse(e.getMessage())));
        routes.exception(WaitingForOpponentException.class, (e, ctx) -> ctx.status(409).json(new ErrorResponse(e.getMessage())));
        routes.exception(NotYourTurnException.class, (e, ctx) -> ctx.status(403).json(new ErrorResponse(e.getMessage())));
    }

    private static String code(Context ctx) {
        return ctx.pathParam("code").toUpperCase();
    }
}
