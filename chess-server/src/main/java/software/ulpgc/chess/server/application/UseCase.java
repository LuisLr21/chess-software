package software.ulpgc.chess.server.application;

public interface UseCase<Input, Output> {
    Output execute(Input input);
}
