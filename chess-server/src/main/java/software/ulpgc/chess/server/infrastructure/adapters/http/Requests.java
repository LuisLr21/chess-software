package software.ulpgc.chess.server.infrastructure.adapters.http;

import io.javalin.http.Context;

public final class Requests {
    private static final String PLAYER_TOKEN_HEADER = "X-Player-Token";

    private Requests() {
    }

    public static String token(Context context) {
        return context.header(PLAYER_TOKEN_HEADER);
    }

    public static String code(Context context) {
        return context.pathParam("code").toUpperCase();
    }

    public static <T> T bodyOr(Context context, Class<T> type, T empty) {
        return context.body().isBlank() ? empty : context.bodyAsClass(type);
    }
}
