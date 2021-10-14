package fr.bakaaless.chessserver.mutual.packets;

public class PacketInSelectPiece implements Packet {

    public int[] coordinates;

    public PacketInSelectPiece() {
    }

    public PacketInSelectPiece(final int[] coordinates) {
        this.coordinates = coordinates;
    }
}
