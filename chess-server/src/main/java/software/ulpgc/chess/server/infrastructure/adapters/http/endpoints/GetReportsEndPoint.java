package software.ulpgc.chess.server.infrastructure.adapters.http.endpoints;

import io.javalin.http.Context;
import software.ulpgc.chess.server.application.UseCase;
import software.ulpgc.chess.server.application.domain.Report;
import software.ulpgc.chess.server.infrastructure.adapters.http.EndPoint;
import software.ulpgc.chess.server.infrastructure.adapters.http.ReportResponse;
import software.ulpgc.chess.server.infrastructure.adapters.http.Requests;

import java.util.List;

public class GetReportsEndPoint implements EndPoint {
    private final UseCase<String, List<Report>> useCase;
    private final Context context;

    private GetReportsEndPoint(UseCase<String, List<Report>> useCase, Context context) {
        this.useCase = useCase;
        this.context = context;
    }

    public static EndPoint use(UseCase<String, List<Report>> useCase, Context context) {
        return new GetReportsEndPoint(useCase, context);
    }

    @Override
    public void execute() {
        context.json(useCase.execute(Requests.code(context)).stream().map(ReportResponse::from).toList());
    }
}
