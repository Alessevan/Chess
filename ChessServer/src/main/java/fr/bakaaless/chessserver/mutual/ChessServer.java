package fr.bakaaless.chessserver.mutual;

import fr.bakaaless.chessserver.dedicated.KickReason;
import fr.bakaaless.chessserver.mutual.packets.Packet;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public abstract class ChessServer {

    protected static Logger logger;

    protected Timer timeUpdaterTimer;
    protected TimeUpdater timeUpdater;

    protected final long startTime;
    protected final List<User> users;
    protected final int[][] field;

    protected boolean start;
    protected int colorRound;
    protected ArrayList<int[][]> previousMoves;
    protected ArrayList<int[]> enPassantList;

    protected long userTime;
    protected long userMoveTime;
    protected int serverPort;

    public ChessServer() {
        this.startTime = System.currentTimeMillis();
        Thread.currentThread().setName("Chess - Server");
        logger = LogManager.getLogger("Chess - Server");
        logger.atLevel(Level.INFO);

        getLogger().info("Starting server...");
        this.users = new ArrayList<>();
        this.field = new int[8][8];
        this.load();
    }

    private void load() {
        getLogger().info("Setting up variables...");

        getLogger().info("Getting server properties...");
        final File properties = new File("server.properties");
        if (!properties.exists()) {
            getLogger().warn(" - Creating file server.properties");
            try {
                FileUtils.copyInputStreamToFile(ClassLoader.getSystemResourceAsStream("server.properties"), properties);
            } catch (IOException e) {
                getLogger().fatal(" * Can't create file `server.properties`", e);
                System.exit(-1);
            }
        }

        final Properties serverProperties = new Properties();
        try {
            serverProperties.load(new BufferedInputStream(new FileInputStream(properties)));
            this.userTime = Long.parseLong(serverProperties.getProperty("time")) * 1000L;
            this.userMoveTime = Long.parseLong(serverProperties.getProperty("time-move")) * 1000L;
            this.serverPort = Integer.parseInt(serverProperties.getProperty("server-port"));
        } catch (IOException e) {
            getLogger().fatal(" * Can't read file `server.properties`", e);
            System.exit(-1);
        }

        this.users.clear();
        this.start = false;
        this.colorRound = Pieces.WHITE.value();
        this.previousMoves = new ArrayList<>();
        this.enPassantList = new ArrayList<>();

        getLogger().info("Filling grid...");
        for (int index = 0; index < 8; index++) {
            Arrays.fill(this.field[index], 0);
            this.field[index][1] = (Pieces.PAWN.value() << 2) | Pieces.BLACK.value();
            this.field[index][6] = (Pieces.PAWN.value() << 2) | Pieces.WHITE.value();
        }

        this.field[0][0] = this.field[7][0] = (Pieces.ROOK.value() << 2) | Pieces.BLACK.value();
        this.field[0][7] = this.field[7][7] = (Pieces.ROOK.value() << 2) | Pieces.WHITE.value();

        this.field[1][0] = this.field[6][0] = (Pieces.KNIGHT.value() << 2) | Pieces.BLACK.value();
        this.field[1][7] = this.field[6][7] = (Pieces.KNIGHT.value() << 2) | Pieces.WHITE.value();

        this.field[2][0] = this.field[5][0] = (Pieces.BISHOP.value() << 2) | Pieces.BLACK.value();
        this.field[2][7] = this.field[5][7] = (Pieces.BISHOP.value() << 2) | Pieces.WHITE.value();

        this.field[3][0] = (Pieces.QUEEN.value() << 2) | Pieces.BLACK.value();
        this.field[4][0] = (Pieces.KING.value() << 2) | Pieces.BLACK.value();

        this.field[3][7] = (Pieces.QUEEN.value() << 2) | Pieces.WHITE.value();
        this.field[4][7] = (Pieces.KING.value() << 2) | Pieces.WHITE.value();

        this.timeUpdaterTimer = new Timer();
        this.timeUpdater = new TimeUpdater(this);
    }

    public ArrayList<int[]> getMoves(final int[][] field, final int[] position, final boolean[] canRook, final boolean checkChess) {
        final ArrayList<int[]> moves = new ArrayList<>();
        final Pieces[] piece = Pieces.getPieceNColor(field[position[0]][position[1]]);
        switch (piece[0]) {
            case BISHOP -> {
                for (final int[] direction : Pieces.BISHOP_DIRECTIONS)
                    linearMoves(field, position, moves, piece, direction[0], direction[1], checkChess);
            }

            case KING -> {
                int pieceToLook;
                int[] positionToLook;
                Vector vector;
                for (final int[] direction : Pieces.KING_DIRECTIONS) {
                    vector = new Vector(direction[0], direction[1]);
                    positionToLook = getPosition(position, vector);
                    if (!inBound(positionToLook))
                        continue;
                    pieceToLook = field[positionToLook[0]][positionToLook[1]];
                    if (pieceToLook == 0) {
                        if (!(checkChess && illegalMove(field, piece[1].value(), position, positionToLook)))
                            moves.add(positionToLook);
                    } else if (Pieces.getColor(pieceToLook) != piece[1]) {
                        if (!(checkChess && illegalMove(field, piece[1].value(), position, positionToLook)))
                            moves.add(positionToLook);
                    }
                }
                if (canRook[0]) {
                    positionToLook = getPosition(position, new Vector(-2, 0));
                    if (field[positionToLook[0] + 1][positionToLook[1]] == 0 && field[positionToLook[0]][positionToLook[1]] == 0 && field[positionToLook[0] - 1][positionToLook[1]] == 0)
                        if (!(checkChess && illegalMove(field, piece[1].value(), position, positionToLook, new int[][] { { 7, positionToLook[1] }, { positionToLook[0] - 1, positionToLook[1]} })))
                            moves.add(positionToLook);
                }
                if (canRook[1]) {
                    positionToLook = getPosition(position, new Vector(2, 0));
                    if (field[positionToLook[0] - 1][positionToLook[1]] == 0 && field[positionToLook[0]][positionToLook[1]] == 0)
                        if (!(checkChess && illegalMove(field, piece[1].value(), position, positionToLook, new int[][] { { 7, positionToLook[1] }, { positionToLook[0] - 1, positionToLook[1]} })))
                            moves.add(positionToLook);
                }
            }

            case KNIGHT -> {
                int pieceToLook;
                int[] positionToLook;
                Vector vector;
                for (final int[] direction : Pieces.KNIGHT_DIRECTIONS) {
                    vector = new Vector(direction[0], direction[1]);
                    positionToLook = getPosition(position, vector);
                    if (!inBound(positionToLook))
                        continue;
                    pieceToLook = field[positionToLook[0]][positionToLook[1]];
                    if (pieceToLook == 0) {
                        if (checkChess && illegalMove(field, piece[1].value(), position, positionToLook))
                            continue;
                        moves.add(positionToLook);
                    }
                    else if (Pieces.getColor(pieceToLook) != piece[1]) {
                        if (checkChess && illegalMove(field, piece[1].value(), position, positionToLook))
                            continue;
                        moves.add(positionToLook);
                    }
                }
            }

            case PAWN -> {
                Vector vector = new Vector(0, 0, piece[1] == Pieces.WHITE ? -1 : 1);
                vector.add(0, 1, true);

                int[] positionToLook = getPosition(position, vector);
                int pieceToLook;
                if (inBound(positionToLook)) {
                    pieceToLook = field[positionToLook[0]][positionToLook[1]];
                    if (pieceToLook == 0) {
                        if (!(checkChess && illegalMove(field, piece[1].value(), position, positionToLook)))
                            moves.add(positionToLook);
                    }
                }
                if (position[1] == 1 && piece[1] == Pieces.BLACK || position[1] == 6 && piece[1] == Pieces.WHITE) {
                    vector.add(0, 1, true);
                    positionToLook = getPosition(position, vector);
                    pieceToLook = field[positionToLook[0]][positionToLook[1]];
                    if (pieceToLook == 0) {
                        if (!(checkChess && illegalMove(field, piece[1].value(), position, positionToLook)))
                            moves.add(positionToLook);
                    }
                    vector.add(0, -1, true);
                }
                vector.add(1, 0, true);
                checkPawnMoves(field, position, checkChess, moves, piece, vector);
                vector.add(-2, 0, true);
                checkPawnMoves(field, position, checkChess, moves, piece, vector);

                vector = new Vector(0, 0, piece[1] == Pieces.WHITE ? -1 : 1);
                vector.add(1, 0);
                positionToLook = getPosition(position, vector);
                final int[] finalPositionToLook = positionToLook;
                if (this.enPassantList.stream().anyMatch(pawnPosition -> pawnPosition[0] == finalPositionToLook[0] && pawnPosition[1] == finalPositionToLook[1])) {
                    vector.add(0, 1, true);
                    final int[] targetPosition = getPosition(position, vector);
                    if (field[targetPosition[0]][targetPosition[1]] == 0 && !(checkChess && illegalMove(field, piece[1].value(), position, getPosition(position, vector), new int[][] { { positionToLook[0], positionToLook[1] }, {} })))
                        moves.add(getPosition(position, vector));
                }
                vector.add(-2, 0);
                positionToLook = getPosition(position, vector);
                final int[] finalPositionToLook2 = positionToLook;
                if (this.enPassantList.stream().anyMatch(pawnPosition -> pawnPosition[0] == finalPositionToLook2[0] && pawnPosition[1] == finalPositionToLook[1])) {
                    vector.add(0, 1, true);
                    final int[] targetPosition = getPosition(position, vector);
                    if (field[targetPosition[0]][targetPosition[1]] == 0 && !(checkChess && illegalMove(field, piece[1].value(), position, getPosition(position, vector), new int[][] { { positionToLook[0], positionToLook[1] }, {} })))
                        moves.add(getPosition(position, vector));
                }
            }

            case ROOK -> {
                for (final int[] direction : Pieces.ROOK_DIRECTIONS)
                    linearMoves(field, position, moves, piece, direction[0], direction[1], checkChess);
            }

            case QUEEN -> {
                for (final int[] direction : Pieces.QUEEN_DIRECTIONS) {
                    linearMoves(field, position, moves, piece, direction[0], direction[1], checkChess);
                }
            }
        }
        return moves;
    }

    private void checkPawnMoves(final int[][] field, final int[] position, final boolean checkChess, final ArrayList<int[]> moves, final Pieces[] piece, final Vector vector) {
        int[] positionToLook;
        int pieceToLook;
        positionToLook = getPosition(position, vector);
        if (inBound(positionToLook)) {
            pieceToLook = field[positionToLook[0]][positionToLook[1]];
            if (pieceToLook != 0 && Pieces.getColor(pieceToLook) != piece[1]) {
                if (!(checkChess && illegalMove(field, piece[1].value(), position, positionToLook)))
                    moves.add(positionToLook);
            }
        }
    }

    private void linearMoves(final int[][] field, final int[] position, final ArrayList<int[]> moves, final Pieces[] piece, final int x, final int y, final boolean checkChess) {
        Vector vector;
        int[] positionToLook;
        int pieceToLook;
        vector = new Vector(0, 0);
        for (int index = 1; index < 8; index++) {
            vector.add(x, y);
            positionToLook = this.getPosition(position, vector);
            if (!inBound(positionToLook)) {
                break;
            }
            pieceToLook = field[positionToLook[0]][positionToLook[1]];
            if (pieceToLook == Pieces.NONE.value()) {
                if (checkChess && illegalMove(field, piece[1].value(), position, positionToLook))
                    continue;
                moves.add(positionToLook);
            }
            else if (Pieces.getColor(pieceToLook) != piece[1]) {
                if (checkChess && illegalMove(field, piece[1].value(), position, positionToLook))
                    continue;
                moves.add(positionToLook);
                break;
            } else {
                break;
            }
        }
    }

    public boolean illegalMove(final int[][] field, final int color, final int[] origin, final int[] destination) {
        return this.illegalMove(field, color, origin, destination, new int[0][0]);
    }

    public boolean illegalMove(final int[][] field, final int color, final int[] origin, final int[] destination, int[][]... replacedPieces) {
        final int[][] nextField = new int[8][8];
        for (int x = 0; x < 8; x++)
            System.arraycopy(field[x], 0, nextField[x], 0, 8);
        nextField[destination[0]][destination[1]] = nextField[origin[0]][origin[1]];
        nextField[origin[0]][origin[1]] = Pieces.NONE.value();
        if (replacedPieces != null) {
            for (final int[][] replacedPiece : replacedPieces) {
                if (replacedPiece.length == 0 || replacedPiece[0].length == 0)
                    continue;
                if (replacedPiece[1].length == 0) {
                    nextField[replacedPiece[0][0]][replacedPiece[0][1]] = Pieces.NONE.value();
                    continue;
                }
                nextField[replacedPiece[1][0]][replacedPiece[1][1]] = nextField[replacedPiece[0][0]][replacedPiece[0][1]];
                nextField[replacedPiece[0][0]][replacedPiece[0][1]] = Pieces.NONE.value();
            }
        }
        return this.verifyCheck(color, nextField);
    }

    public boolean verifyCheck(final int color, final int[][] field) {
        for (int x = 0; x < field.length; x++) {
            for (int y = 0; y < field[x].length; y++) {
                if (field[x][y] == 0)
                    continue;
                final Pieces[] piece = Pieces.getPieceNColor(field[x][y]);
                if (piece[0] != Pieces.KING)
                    continue;
                if (piece[1].value() != color)
                    continue;

                final int[] coordinates = new int[] { x, y };
                for (int xi = 0; xi < field.length; xi++) {
                    for (int yi = 0; yi < field.length; yi++) {
                        if (field[xi][yi] == 0)
                            continue;
                        final Pieces[] enemy = Pieces.getPieceNColor(field[xi][yi]);
                        if (piece[1] == enemy[1])
                            continue;

                        final ArrayList<int[]> moves = this.getMoves(field, new int[] { xi, yi }, new boolean[] { false, false }, false);
                        if (moves.stream().anyMatch(move -> move[0] == coordinates[0] && move[1] == coordinates[1]))
                            return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean inBound(final int[] position) {
        if (position[0] < 0 || position[0] >= this.field.length)
            return false;
        return position[1] >= 0 && position[1] < this.field[0].length;
    }

    public int getPieceAtPosition(final int[] position) {
        return this.field[position[0]][position[1]];
    }

    public int[] getPosition(final int[] initialPosition, final Vector vector) {
        return new int[] { initialPosition[0] + vector.x, initialPosition[1] + vector.y };
    }

    public void sendAllPacket(final Packet packet) {
        this.users.forEach(user -> user.sendPacket(packet));
    }

    public Logger getLogger() {
        return ChessServer.logger;
    }

    public int getColorRound() {
        return this.colorRound;
    }

    public void stop() {
        new ArrayList<>(this.users).forEach(user -> user.disconnect(KickReason.SHUTDOWN));
        this.timeUpdaterTimer.cancel();
        this.load();
    }
}
