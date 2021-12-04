package fr.bakaaless.chessserver.mutual;

public enum Pieces {

    NONE(0b0),
    PAWN(0b1),
    ROOK(0b10),
    KNIGHT(0b11),
    BISHOP(0b100),
    QUEEN(0b101),
    KING(0b110),

    WHITE(0b0),
    BLACK(0b1),
    SPECTATOR(0b11);

    public static final int[][] BISHOP_DIRECTIONS = new int[][] { { 1, 1 }, { -1, 1 }, { -1, -1 }, { 1, -1 } };
    public static final int[][] KING_DIRECTIONS = new int[][] { { 1, 0 }, { 0, 1 }, { -1, 0 }, { 0, -1 }, { 1, 1 }, { -1, 1 }, { -1, -1 }, { 1, -1 } };
    public static final int[][] KNIGHT_DIRECTIONS = new int[][] { { 1, 2 }, { -1, 2 }, { 2, 1 }, { 2, -1 }, { 1, -2 }, { -1, -2 }, { -2, 1 }, { -2, -1 } };
    public static final int[][] ROOK_DIRECTIONS = new int[][] { { 1, 0 }, { 0, 1 }, { -1, 0 }, { 0, -1 } };
    public static final int[][] QUEEN_DIRECTIONS = new int[][] { { 1, 0 }, { 0, 1 }, { -1, 0 }, { 0, -1 }, { 1, 1 }, { -1, 1 }, { -1, -1 }, { 1, -1 } };

    private final int value;

    Pieces(final int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }

    public static Pieces[] getPieceNColor(final int piece) {
        final Pieces[] result = new Pieces[2];
        result[1] = getColor(piece);
        result[0] = values()[piece >> 2];
        return result;
    }

    public static Pieces getColor(final int piece) {
        final int lastToBits = piece & 3;
        if (NONE.value == piece - lastToBits)
            return NONE;
        if (WHITE.value == lastToBits)
            return WHITE;
        if (BLACK.value == lastToBits)
            return BLACK;
        return SPECTATOR;
    }

}
