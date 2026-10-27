package software.ulpgc.chess.server.infrastructure.adapters.http;

import io.javalin.config.RoutesConfig;
import io.javalin.http.Context;
import software.ulpgc.chess.core.GameOverException;
import software.ulpgc.chess.core.IllegalMoveException;
import software.ulpgc.chess.server.application.domain.NotAPlayerException;
import software.ulpgc.chess.server.application.domain.NotYourTurnException;
import software.ulpgc.chess.server.application.domain.RoomFullException;
import software.ulpgc.chess.server.application.domain.WaitingForOpponentException;
import software.ulpgc.chess.server.application.ports.ChessEngineUnavailableException;
import software.ulpgc.chess.server.application.usecases.AlreadyReportedException;
import software.ulpgc.chess.server.application.usecases.GameNotFoundException;
import software.ulpgc.chess.server.application.usecases.GameNotOverException;
import software.ulpgc.chess.server.application.usecases.RoomNotFoundException;

public final class ErrorHandlers {
    private ErrorHandlers() {
    }

    public static void register(RoutesConfig routes) {
        routes.exception(IllegalArgumentException.class, (e, context) -> error(context, 400, e));
        routes.exception(NotYourTurnException.class, (e, context) -> error(context, 403, e));
        routes.exception(NotAPlayerException.class, (e, context) -> error(context, 403, e));
        routes.exception(GameNotFoundException.class, (e, context) -> error(context, 404, e));
        routes.exception(RoomNotFoundException.class, (e, context) -> error(context, 404, e));
        routes.exception(GameOverException.class, (e, context) -> error(context, 409, e));
        routes.exception(GameNotOverException.class, (e, context) -> error(context, 409, e));
        routes.exception(RoomFullException.class, (e, context) -> error(context, 409, e));
        routes.exception(WaitingForOpponentException.class, (e, context) -> error(context, 409, e));
        routes.exception(AlreadyReportedException.class, (e, context) -> error(context, 409, e));
        routes.exception(IllegalMoveException.class, (e, context) -> error(context, 422, e));
        routes.exception(ChessEngineUnavailableException.class, (e, context) -> error(context, 503, e));
    }

    private static void error(Context context, int status, Exception e) {
        context.status(status).json(new ErrorResponse(e.getMessage()));
    }
}
