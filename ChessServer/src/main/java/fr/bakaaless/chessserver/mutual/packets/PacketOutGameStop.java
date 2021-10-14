package fr.bakaaless.chessserver.mutual.packets;

public class PacketOutGameStop implements Packet {

    public int winner;
    public int reason;

    public PacketOutGameStop() {
    }

    public PacketOutGameStop(final int winner, final int reason) {
        this.winner = winner;
        this.reason = reason;
    }

    public static enum PacketOutGameStopReason {

        MAT,
        PAT,
        TIMES_UP,
        DISCONNECT,

    }
}
