package software.ulpgc.chess.core;

public enum File {
    A('a'), B('b'), C('c'), D('d'), E('e'), F('f'), G('g'), H('h');

    private final char symbol;

    File(char symbol) {
        this.symbol = symbol;
    }

    public char symbol(){
        return symbol;
    }
    public static File fromSymbol(char symbol){
        return switch (symbol) {
            case 'a' -> A;
            case 'b' -> B;
            case 'c' -> C;
            case 'd' -> D;
            case 'e' -> E;
            case 'f' -> F;
            case 'g' -> G;
            case 'h' -> H;
            default -> throw new IllegalArgumentException("Invalid file: " + symbol);
        };
    }

}
