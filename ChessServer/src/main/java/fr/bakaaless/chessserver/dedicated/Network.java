package fr.bakaaless.chessserver.dedicated;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;
import fr.bakaaless.chessserver.mutual.packets.*;

import java.util.ArrayList;

public class Network {

    public static void register(final EndPoint endPoint) {
        final Kryo kryo = endPoint.getKryo();
        kryo.register(int.class);
        kryo.register(int[].class);
        kryo.register(int[][].class);
        kryo.register(long.class);
        kryo.register(long[].class);
        kryo.register(String.class);
        kryo.register(ArrayList.class);
        kryo.register(Packet.class);
        kryo.register(PacketInChat.class);
        kryo.register(PacketInJoin.class);
        kryo.register(PacketInPieceMove.class);
        kryo.register(PacketInSelectPiece.class);
        kryo.register(PacketOutChat.class);
        kryo.register(PacketOutChess.class);
        kryo.register(PacketOutColor.class);
        kryo.register(PacketOutConnected.class);
        kryo.register(PacketOutGameField.class);
        kryo.register(PacketOutGameStart.class);
        kryo.register(PacketOutGameStop.class);
        kryo.register(PacketOutPieceMoves.class);
        kryo.register(PacketOutPreviousMoves.class);
        kryo.register(PacketOutTime.class);
        kryo.register(PacketOutUnselectPiece.class);
    }

}
