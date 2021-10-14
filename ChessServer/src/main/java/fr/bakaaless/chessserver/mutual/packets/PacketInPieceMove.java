package fr.bakaaless.chessserver.mutual.packets;

public class PacketInPieceMove implements Packet {

    public int newX;
    public int newY;

    public PacketInPieceMove() {
    }

    public PacketInPieceMove(final int newX, final int newY) {
        this.newX = newX;
        this.newY = newY;
    }
}
