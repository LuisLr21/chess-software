package software.ulpgc.chess.server.infrastructure.adapters.http.endpoints;

import io.javalin.http.Context;
import software.ulpgc.chess.server.application.UseCase;
import software.ulpgc.chess.server.application.domain.Report;
import software.ulpgc.chess.server.application.usecases.NewReport;
import software.ulpgc.chess.server.infrastructure.adapters.http.EndPoint;
import software.ulpgc.chess.server.infrastructure.adapters.http.ReportRequest;
import software.ulpgc.chess.server.infrastructure.adapters.http.ReportResponse;
import software.ulpgc.chess.server.infrastructure.adapters.http.Requests;

public class ReportOpponentEndPoint implements EndPoint {
    private final UseCase<NewReport, Report> useCase;
    private final Context context;

    private ReportOpponentEndPoint(UseCase<NewReport, Report> useCase, Context context) {
        this.useCase = useCase;
        this.context = context;
    }

    public static EndPoint use(UseCase<NewReport, Report> useCase, Context context) {
        return new ReportOpponentEndPoint(useCase, context);
    }

    @Override
    public void execute() {
        ReportRequest request = Requests.bodyOr(context, ReportRequest.class, new ReportRequest(null));
        NewReport report = new NewReport(Requests.code(context), Requests.token(context), request.reason());
        context.status(201).json(ReportResponse.from(useCase.execute(report)));
    }
}
