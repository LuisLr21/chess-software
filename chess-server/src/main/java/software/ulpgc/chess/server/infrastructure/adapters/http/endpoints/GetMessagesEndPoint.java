package software.ulpgc.chess.server.infrastructure.adapters.http.endpoints;

import io.javalin.http.Context;
import software.ulpgc.chess.server.application.UseCase;
import software.ulpgc.chess.server.application.domain.ChatMessage;
import software.ulpgc.chess.server.infrastructure.adapters.http.ChatMessageResponse;
import software.ulpgc.chess.server.infrastructure.adapters.http.EndPoint;
import software.ulpgc.chess.server.infrastructure.adapters.http.Requests;

import java.util.List;

public class GetMessagesEndPoint implements EndPoint {
    private final UseCase<String, List<ChatMessage>> useCase;
    private final Context context;

    private GetMessagesEndPoint(UseCase<String, List<ChatMessage>> useCase, Context context) {
        this.useCase = useCase;
        this.context = context;
    }

    public static EndPoint use(UseCase<String, List<ChatMessage>> useCase, Context context) {
        return new GetMessagesEndPoint(useCase, context);
    }

    @Override
    public void execute() {
        context.json(useCase.execute(Requests.code(context)).stream().map(ChatMessageResponse::from).toList());
    }
}
