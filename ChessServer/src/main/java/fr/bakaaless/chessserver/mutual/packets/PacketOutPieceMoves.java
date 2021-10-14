package fr.bakaaless.chessserver.mutual.packets;

import java.util.ArrayList;

public class PacketOutPieceMoves implements Packet {

    public ArrayList<int[]> moves;

    public PacketOutPieceMoves() {
    }

    public PacketOutPieceMoves(final ArrayList<int[]> moves) {
        this.moves = moves;
    }

}
