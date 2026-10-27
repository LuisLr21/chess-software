package software.ulpgc.chess.server.infrastructure.adapters.http.endpoints;

import io.javalin.http.Context;
import software.ulpgc.chess.server.application.UseCase;
import software.ulpgc.chess.server.application.usecases.PlayerInRoom;
import software.ulpgc.chess.server.application.usecases.RoomState;
import software.ulpgc.chess.server.infrastructure.adapters.http.EndPoint;
import software.ulpgc.chess.server.infrastructure.adapters.http.Requests;
import software.ulpgc.chess.server.infrastructure.adapters.http.RoomResponse;

public class RoomActionEndPoint implements EndPoint {
    private final UseCase<PlayerInRoom, RoomState> useCase;
    private final Context context;

    private RoomActionEndPoint(UseCase<PlayerInRoom, RoomState> useCase, Context context) {
        this.useCase = useCase;
        this.context = context;
    }

    public static EndPoint use(UseCase<PlayerInRoom, RoomState> useCase, Context context) {
        return new RoomActionEndPoint(useCase, context);
    }

    @Override
    public void execute() {
        String player = Requests.token(context);
        context.json(RoomResponse.from(useCase.execute(new PlayerInRoom(Requests.code(context), player)), player));
    }
}
