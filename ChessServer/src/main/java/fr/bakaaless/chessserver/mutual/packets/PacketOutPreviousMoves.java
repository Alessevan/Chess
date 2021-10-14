package fr.bakaaless.chessserver.mutual.packets;

import java.util.ArrayList;

public class PacketOutPreviousMoves implements Packet {

    public ArrayList<int[][]> moves;

    public PacketOutPreviousMoves() {
    }

    public PacketOutPreviousMoves(final ArrayList<int[][]> moves) {
        this.moves = moves;
    }

}
