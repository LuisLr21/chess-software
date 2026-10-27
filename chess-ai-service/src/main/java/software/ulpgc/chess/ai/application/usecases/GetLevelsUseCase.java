package software.ulpgc.chess.ai.application.usecases;

import software.ulpgc.chess.ai.application.UseCase;
import software.ulpgc.chess.ai.application.domain.Engine;

import java.util.List;
import java.util.Map;

public class GetLevelsUseCase implements UseCase<Void, List<String>> {
    private final Map<String, Engine> engines;

    public GetLevelsUseCase(Map<String, Engine> engines) {
        this.engines = engines;
    }

    @Override
    public List<String> execute(Void nothing) {
        return List.copyOf(engines.keySet());
    }
}
