package software.ulpgc.chess;

public enum Color {
    White, Black;

    public Color opposite(){
        return this == White ? Black : White;
    }
}
