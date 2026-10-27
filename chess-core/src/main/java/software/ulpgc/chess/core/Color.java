package software.ulpgc.chess.core;

public enum Color {
    White, Black;

    public Color opposite(){
        return this == White ? Black : White;
    }
}
