package fr.bakaaless.chessserver.mutual.packets;

public class PacketOutTime implements Packet {

    public long[] time;

    public PacketOutTime() {
    }

    public PacketOutTime(final long[] time) {
        this.time = time;
    }

}
