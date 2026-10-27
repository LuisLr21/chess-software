package software.ulpgc.chess.ai.infrastructure.adapters.endpoints;

import io.javalin.http.Context;
import software.ulpgc.chess.ai.application.UseCase;
import software.ulpgc.chess.ai.application.usecases.BestMoveQuery;
import software.ulpgc.chess.ai.infrastructure.adapters.EndPoint;
import software.ulpgc.chess.core.Move;

import java.util.List;

public class BestMoveEndPoint implements EndPoint {
    private final UseCase<BestMoveQuery, Move> useCase;
    private final Context context;

    private BestMoveEndPoint(UseCase<BestMoveQuery, Move> useCase, Context context) {
        this.useCase = useCase;
        this.context = context;
    }

    public static EndPoint use(UseCase<BestMoveQuery, Move> useCase, Context context) {
        return new BestMoveEndPoint(useCase, context);
    }

    @Override
    public void execute() {
        BestMoveRequest request = context.bodyAsClass(BestMoveRequest.class);
        List<Move> moves = request.moves() == null ? List.of() : request.moves().stream().map(Move::fromUci).toList();
        Move move = useCase.execute(new BestMoveQuery(request.level(), moves));
        context.json(new BestMoveResponse(move.toString()));
    }

    public record BestMoveRequest(String level, List<String> moves) {
    }

    public record BestMoveResponse(String move) {
    }
}
