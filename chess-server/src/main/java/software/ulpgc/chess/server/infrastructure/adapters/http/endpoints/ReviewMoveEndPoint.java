package software.ulpgc.chess.server.infrastructure.adapters.http.endpoints;

import io.javalin.http.Context;
import software.ulpgc.chess.server.application.UseCase;
import software.ulpgc.chess.server.application.usecases.MoveReview;
import software.ulpgc.chess.server.application.usecases.MoveToReview;
import software.ulpgc.chess.server.infrastructure.adapters.http.EndPoint;
import software.ulpgc.chess.server.infrastructure.adapters.http.MoveReviewResponse;

public class ReviewMoveEndPoint implements EndPoint {
    private final UseCase<MoveToReview, MoveReview> useCase;
    private final Context context;

    private ReviewMoveEndPoint(UseCase<MoveToReview, MoveReview> useCase, Context context) {
        this.useCase = useCase;
        this.context = context;
    }

    public static EndPoint use(UseCase<MoveToReview, MoveReview> useCase, Context context) {
        return new ReviewMoveEndPoint(useCase, context);
    }

    @Override
    public void execute() {
        MoveToReview target = new MoveToReview(context.pathParam("id"), Integer.parseInt(context.pathParam("index")));
        context.json(MoveReviewResponse.from(useCase.execute(target)));
    }
}
