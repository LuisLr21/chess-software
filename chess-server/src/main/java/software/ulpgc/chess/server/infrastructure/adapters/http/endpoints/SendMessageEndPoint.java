package software.ulpgc.chess.server.infrastructure.adapters.http.endpoints;

import io.javalin.http.Context;
import software.ulpgc.chess.server.application.UseCase;
import software.ulpgc.chess.server.application.domain.ChatMessage;
import software.ulpgc.chess.server.application.usecases.NewMessage;
import software.ulpgc.chess.server.infrastructure.adapters.http.ChatMessageResponse;
import software.ulpgc.chess.server.infrastructure.adapters.http.EndPoint;
import software.ulpgc.chess.server.infrastructure.adapters.http.MessageRequest;
import software.ulpgc.chess.server.infrastructure.adapters.http.Requests;

public class SendMessageEndPoint implements EndPoint {
    private final UseCase<NewMessage, ChatMessage> useCase;
    private final Context context;

    private SendMessageEndPoint(UseCase<NewMessage, ChatMessage> useCase, Context context) {
        this.useCase = useCase;
        this.context = context;
    }

    public static EndPoint use(UseCase<NewMessage, ChatMessage> useCase, Context context) {
        return new SendMessageEndPoint(useCase, context);
    }

    @Override
    public void execute() {
        MessageRequest request = context.bodyAsClass(MessageRequest.class);
        NewMessage message = new NewMessage(Requests.code(context), Requests.token(context), request.text());
        context.status(201).json(ChatMessageResponse.from(useCase.execute(message)));
    }
}
