package software.ulpgc.chess.server.infrastructure.adapters.http.endpoints;

import io.javalin.http.Context;
import software.ulpgc.chess.server.application.UseCase;
import software.ulpgc.chess.server.application.usecases.RoomState;
import software.ulpgc.chess.server.infrastructure.adapters.http.EndPoint;
import software.ulpgc.chess.server.infrastructure.adapters.http.Requests;
import software.ulpgc.chess.server.infrastructure.adapters.http.RoomResponse;

public class GetRoomEndPoint implements EndPoint {
    private final UseCase<String, RoomState> useCase;
    private final Context context;

    private GetRoomEndPoint(UseCase<String, RoomState> useCase, Context context) {
        this.useCase = useCase;
        this.context = context;
    }

    public static EndPoint use(UseCase<String, RoomState> useCase, Context context) {
        return new GetRoomEndPoint(useCase, context);
    }

    @Override
    public void execute() {
        context.json(RoomResponse.from(useCase.execute(Requests.code(context)), Requests.token(context)));
    }
}
