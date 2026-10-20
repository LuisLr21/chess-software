package software.ulpgc.chess.domain;

public enum PieceType {
    Pawn('p'), Knight('n'), Bishop('b'), Rook('r'), Queen('q'), King('k');

    private final char symbol;

    PieceType(char symbol) {
        this.symbol = symbol;
    }

    public char symbol(){
        return symbol;
    }
    public static PieceType fromSymbol(char symbol){
        return switch (Character.toLowerCase(symbol)){
            case 'p' -> Pawn;
            case 'n' -> Knight;
            case 'b' -> Bishop;
            case 'r' -> Rook;
            case 'q' -> Queen;
            case 'k' -> King;
            default -> throw new IllegalArgumentException("Invalid piece type: " + symbol);
        };
    }
}
