package software.ulpgc.chess.ai.application;

public interface UseCase<Input, Output> {
    Output execute(Input input);
}
