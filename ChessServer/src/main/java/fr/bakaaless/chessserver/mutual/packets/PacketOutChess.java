package fr.bakaaless.chessserver.mutual.packets;

public class PacketOutChess implements Packet {

    public int colour;

    public PacketOutChess(){
    }

    public PacketOutChess(final int colour) {
        this.colour = colour;
    }

}
