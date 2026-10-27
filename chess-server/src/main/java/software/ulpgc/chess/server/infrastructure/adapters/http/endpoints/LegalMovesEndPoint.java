package software.ulpgc.chess.server.infrastructure.adapters.http.endpoints;

import io.javalin.http.Context;
import software.ulpgc.chess.core.Square;
import software.ulpgc.chess.server.application.UseCase;
import software.ulpgc.chess.server.application.usecases.SelectedPiece;
import software.ulpgc.chess.server.infrastructure.adapters.http.EndPoint;

import java.util.List;

public class LegalMovesEndPoint implements EndPoint {
    private final UseCase<SelectedPiece, List<Square>> useCase;
    private final Context context;

    private LegalMovesEndPoint(UseCase<SelectedPiece, List<Square>> useCase, Context context) {
        this.useCase = useCase;
        this.context = context;
    }

    public static EndPoint use(UseCase<SelectedPiece, List<Square>> useCase, Context context) {
        return new LegalMovesEndPoint(useCase, context);
    }

    @Override
    public void execute() {
        SelectedPiece selected = new SelectedPiece(context.pathParam("id"), Square.at(context.queryParam("from")));
        context.json(useCase.execute(selected).stream().map(Square::toString).toList());
    }
}
