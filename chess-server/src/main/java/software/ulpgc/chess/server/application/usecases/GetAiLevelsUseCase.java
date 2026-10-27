package software.ulpgc.chess.server.application.usecases;

import software.ulpgc.chess.server.application.UseCase;
import software.ulpgc.chess.server.application.ports.ChessEngine;

import java.util.List;

public class GetAiLevelsUseCase implements UseCase<Void, List<String>> {
    private final ChessEngine engine;

    public GetAiLevelsUseCase(ChessEngine engine) {
        this.engine = engine;
    }

    @Override
    public List<String> execute(Void nothing) {
        return engine.levels();
    }
}
