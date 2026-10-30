package software.ulpgc.chess.server.infrastructure.adapters.http;

import software.ulpgc.chess.server.application.usecases.MoveReview;

public record MoveReviewResponse(String played, String suggested, boolean best) {
    public static MoveReviewResponse from(MoveReview review) {
        return new MoveReviewResponse(review.played(), review.suggested(), review.isBest());
    }
}
