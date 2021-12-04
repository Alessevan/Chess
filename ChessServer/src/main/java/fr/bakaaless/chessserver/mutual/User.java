package fr.bakaaless.chessserver.mutual;

import fr.bakaaless.chessserver.dedicated.KickReason;
import fr.bakaaless.chessserver.mutual.packets.*;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

public abstract class User {

    protected final ChessServer server;

    protected final String name;
    protected final int color;

    protected int[] selected;
    protected ArrayList<int[]> legalMoves;

    private boolean[] canRook;
    private final AtomicLong timeLeft;

    public User(final ChessServer server, final String name, final int color) {
        this.server = server;
        this.name = name;
        this.color = color;
        this.canRook = new boolean[] { true, true };
        this.selected = new int[0];
        this.timeLeft = new AtomicLong(server.userTime);
    }

    public String getName() {
        return this.name;
    }

    public int getColor() {
        return this.color;
    }

    public long getTimeLeft() {
        return this.timeLeft.get();
    }

    public void updateTimeLeft(final long time) {
        this.timeLeft.getAndAdd(-time);
    }

    public void resetTimeLeft() {
        this.timeLeft.set(this.server.userTime);
    }

    public abstract void sendPacket(final Packet packet);

    public void receivePacket(final Packet rawPacket) {
        ChessServer.logger.info("Received packet " + rawPacket.getClass().getSimpleName() + " from " + name);
        if (!this.server.start)
            return;
        if (rawPacket instanceof PacketInChat packet) {
            this.server.sendAllPacket(new PacketOutChat(this.name, packet.message));
        } else if (rawPacket instanceof PacketInSelectPiece packet) {
            this.selected = packet.coordinates;
            if (this.color != Pieces.getColor(this.server.getPieceAtPosition(this.selected)).value() || this.server.colorRound != this.color) {
                this.sendPacket(new PacketOutUnselectPiece(this.selected));
                return;
            }
            this.legalMoves = this.server.getMoves(this.server.field, this.selected, this.canRook, true);
            this.sendPacket(new PacketOutPieceMoves(this.legalMoves));
        } else if (rawPacket instanceof PacketInPieceMove packet) {
            if (this.selected.length == 0 || this.legalMoves == null || this.server.colorRound != this.color) {
                this.sendPacket(new PacketOutGameField(this.server.field, this.server.colorRound));
                return;
            }
            final int[] newPiece = new int[] { packet.newX, packet.newY };
            if (this.legalMoves.stream().noneMatch(position -> position[0] == newPiece[0] && position[1] == newPiece[1]))
                return;
            final int piece = this.server.field[this.selected[0]][this.selected[1]];
            if (Pieces.getPieceNColor(piece)[0] == Pieces.KING) {
                if (this.selected[0] - newPiece[0] == -2 && this.canRook[1]) {
                    this.server.field[newPiece[0] - 1][newPiece[1]] = this.server.field[7][newPiece[1]];
                    this.server.field[7][newPiece[1]] = 0;
                }
                else if (this.selected[0] - newPiece[0] == 2 && this.canRook[0]) {
                    this.server.field[newPiece[0] + 1][newPiece[1]] = this.server.field[0][newPiece[1]];
                    this.server.field[0][newPiece[1]] = 0;
                }
                this.canRook = new boolean[] { false, false };
            }
            if (Pieces.getPieceNColor(piece)[0] == Pieces.ROOK) {
                final int index = Math.min(this.selected[0], 1);
                this.canRook[index] = false;
            } else if (Pieces.getPieceNColor(piece)[0] == Pieces.PAWN) {
                this.server.enPassantList.removeIf(position -> position[0] == this.selected[0] && position[1] == this.selected[1]);
                if (Math.abs(this.selected[1] - newPiece[1]) == 2)
                    this.server.enPassantList.add(newPiece);
                else if (Math.abs(this.selected[0] - newPiece[0]) == 1 && Math.abs(this.selected[1] - newPiece[1]) == 1)
                    this.server.enPassantList.removeIf(position -> {
                        final boolean inRange = Math.abs(position[1] - newPiece[1]) == 1 && position[0] - newPiece[0] == 0;
                        if (inRange)
                            this.server.field[position[0]][position[1]] = 0;
                        return inRange;
                    });
            }
            this.server.colorRound = (this.color + 1) % 2;
            this.server.field[newPiece[0]][newPiece[1]] = this.server.field[this.selected[0]][this.selected[1]];
            this.server.field[this.selected[0]][this.selected[1]] = Pieces.NONE.value();
            this.server.previousMoves.add(new int[][] { this.selected, newPiece });
            final boolean hasChess = this.server.verifyCheck(this.server.colorRound, this.server.field);
            if (hasChess) {
                this.server.users.stream().filter(user -> user.color == this.server.colorRound).forEach(user -> user.canRook = new boolean[] { false, false });
                this.server.sendAllPacket(new PacketOutChess());
            }
            this.server.sendAllPacket(new PacketOutUnselectPiece(newPiece));
            this.server.sendAllPacket(new PacketOutUnselectPiece(this.selected));
            this.server.sendAllPacket(new PacketOutGameField(this.server.field, this.server.colorRound));
            this.server.sendAllPacket(new PacketOutPreviousMoves(this.server.previousMoves));
            this.server.sendAllPacket(new PacketOutPieceMoves(null));

            final User enemy = this.server.users.stream().filter(user -> user.getColor() != this.color && user.getColor() != Pieces.SPECTATOR.value()).findFirst().get();
            boolean canMove = false;

            searchInField:
            for (int x = 0; x < 8; x++) {
                for (int y = 0; y < 8; y++) {
                    if (this.server.field[x][y] == 0)
                        continue;
                    final Pieces[] pieceToLook = Pieces.getPieceNColor(this.server.field[x][y]);
                    if (pieceToLook[1].value() == this.color)
                        continue;
                    final ArrayList<int[]> moves = this.server.getMoves(this.server.field, new int[] { x, y }, enemy.canRook, true);
                    canMove = moves.size() != 0;
                    if (canMove)
                        break searchInField;
                }
            }

            if (!canMove) {
                Packet endPacket;
                if (hasChess)
                    endPacket = new PacketOutGameStop(this.color, PacketOutGameStop.PacketOutGameStopReason.MAT.ordinal());
                else
                    endPacket = new PacketOutGameStop(Pieces.SPECTATOR.value(), PacketOutGameStop.PacketOutGameStopReason.PAT.ordinal());
                this.server.sendAllPacket(endPacket);
                this.server.stop();
                return;
            }

            final long[] time = new long[2];
            time[0] = this.server.users.stream().filter(user -> user.color == Pieces.WHITE.value()).map(User::getTimeLeft).findFirst().get();
            time[1] = this.server.users.stream().filter(user -> user.color == Pieces.BLACK.value()).map(User::getTimeLeft).findFirst().get();
            final Packet timePacket = new PacketOutTime(time);
            this.server.sendAllPacket(timePacket);

            this.timeLeft.getAndAdd(this.server.userMoveTime);
            this.selected = new int[0];
            this.legalMoves = null;
        }
    }

    public void kick(final String user) {
        this.disconnect(KickReason.KICKED.getMessage().replace("{0}", user));
    }

    public void disconnect(final KickReason reason) {
        this.disconnect(reason.getMessage());
    }

    public void disconnect(final String reason) {
        this.disconnect();
        final Packet packet = new PacketOutChat("Server", reason);
        this.server.users.forEach(user -> user.sendPacket(packet));
    }

    public void disconnect() {
        this.server.users.remove(this);
    }

}
