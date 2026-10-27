package software.ulpgc.chess.server.infrastructure.adapters.http.endpoints;

import io.javalin.http.Context;
import software.ulpgc.chess.server.application.UseCase;
import software.ulpgc.chess.server.application.usecases.PlayerSession;
import software.ulpgc.chess.server.application.usecases.RoomEntry;
import software.ulpgc.chess.server.infrastructure.adapters.http.EndPoint;
import software.ulpgc.chess.server.infrastructure.adapters.http.Requests;
import software.ulpgc.chess.server.infrastructure.adapters.http.RoomRequest;
import software.ulpgc.chess.server.infrastructure.adapters.http.SessionResponse;

public class JoinRoomEndPoint implements EndPoint {
    private final UseCase<RoomEntry, PlayerSession> useCase;
    private final Context context;

    private JoinRoomEndPoint(UseCase<RoomEntry, PlayerSession> useCase, Context context) {
        this.useCase = useCase;
        this.context = context;
    }

    public static EndPoint use(UseCase<RoomEntry, PlayerSession> useCase, Context context) {
        return new JoinRoomEndPoint(useCase, context);
    }

    @Override
    public void execute() {
        RoomRequest request = Requests.bodyOr(context, RoomRequest.class, new RoomRequest(null, null));
        RoomEntry entry = new RoomEntry(Requests.code(context), request.name());
        context.status(201).json(SessionResponse.from(useCase.execute(entry)));
    }
}
