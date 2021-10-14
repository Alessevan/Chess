package fr.bakaaless.chessserver.mutual.packets;

public class PacketOutUnselectPiece implements Packet {

    public int[] coordinates;

    public PacketOutUnselectPiece() {
    }

    public PacketOutUnselectPiece(final int[] coordinates) {
        this.coordinates = coordinates;
    }

}
