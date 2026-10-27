package software.ulpgc.chess.server.infrastructure.adapters.http.endpoints;

import io.javalin.http.Context;
import software.ulpgc.chess.core.Square;
import software.ulpgc.chess.server.application.UseCase;
import software.ulpgc.chess.server.application.usecases.RoomMove;
import software.ulpgc.chess.server.application.usecases.RoomState;
import software.ulpgc.chess.server.infrastructure.adapters.http.EndPoint;
import software.ulpgc.chess.server.infrastructure.adapters.http.MoveRequest;
import software.ulpgc.chess.server.infrastructure.adapters.http.Requests;
import software.ulpgc.chess.server.infrastructure.adapters.http.RoomResponse;

public class MoveInRoomEndPoint implements EndPoint {
    private final UseCase<RoomMove, RoomState> useCase;
    private final Context context;

    private MoveInRoomEndPoint(UseCase<RoomMove, RoomState> useCase, Context context) {
        this.useCase = useCase;
        this.context = context;
    }

    public static EndPoint use(UseCase<RoomMove, RoomState> useCase, Context context) {
        return new MoveInRoomEndPoint(useCase, context);
    }

    @Override
    public void execute() {
        MoveRequest request = context.bodyAsClass(MoveRequest.class);
        String player = Requests.token(context);
        RoomMove move = new RoomMove(Requests.code(context), player, Square.at(request.from()), Square.at(request.to()), request.promotionType());
        context.json(RoomResponse.from(useCase.execute(move), player));
    }
}
