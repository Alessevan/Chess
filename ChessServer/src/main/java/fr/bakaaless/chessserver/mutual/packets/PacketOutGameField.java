package fr.bakaaless.chessserver.mutual.packets;

public class PacketOutGameField implements Packet {

    public int[][] field;
    public int colorRound;

    public PacketOutGameField() {
    }

    public PacketOutGameField(final int[][] field, final int colorRound) {
        this.field = field;
        this.colorRound = colorRound;
    }

}
