package software.ulpgc.chess.server.infrastructure.adapters.http.endpoints;

import io.javalin.http.Context;
import software.ulpgc.chess.server.application.UseCase;
import software.ulpgc.chess.server.application.usecases.NewRoom;
import software.ulpgc.chess.server.application.usecases.PlayerSession;
import software.ulpgc.chess.server.infrastructure.adapters.http.EndPoint;
import software.ulpgc.chess.server.infrastructure.adapters.http.Requests;
import software.ulpgc.chess.server.infrastructure.adapters.http.RoomRequest;
import software.ulpgc.chess.server.infrastructure.adapters.http.SessionResponse;

public class CreateRoomEndPoint implements EndPoint {
    private final UseCase<NewRoom, PlayerSession> useCase;
    private final Context context;

    private CreateRoomEndPoint(UseCase<NewRoom, PlayerSession> useCase, Context context) {
        this.useCase = useCase;
        this.context = context;
    }

    public static EndPoint use(UseCase<NewRoom, PlayerSession> useCase, Context context) {
        return new CreateRoomEndPoint(useCase, context);
    }

    @Override
    public void execute() {
        RoomRequest request = Requests.bodyOr(context, RoomRequest.class, new RoomRequest(null, null));
        NewRoom room = new NewRoom(request.name(), request.minutesOrZero());
        context.status(201).json(SessionResponse.from(useCase.execute(room)));
    }
}
