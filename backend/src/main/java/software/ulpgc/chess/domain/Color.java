package software.ulpgc.chess.domain;

public enum Color {
    White, Black;

    public Color opposite(){
        return this == White ? Black : White;
    }
}
