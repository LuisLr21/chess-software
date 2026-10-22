package software.ulpgc.chess.application;

import software.ulpgc.chess.domain.ai.Engine;

import java.util.List;
import java.util.Map;

public class GetAiLevelsUseCase {
    private final Map<String, Engine> engines;

    public GetAiLevelsUseCase(Map<String, Engine> engines) {
        this.engines = engines;
    }

    public List<String> execute() {
        return List.copyOf(engines.keySet());
    }
}
