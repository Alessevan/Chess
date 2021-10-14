package fr.bakaaless.chessserver.mutual.packets;

public class PacketOutColor implements Packet {

    public int color;

    public PacketOutColor() {
    }

    public PacketOutColor(final int color) {
        this.color = color;
    }

}
