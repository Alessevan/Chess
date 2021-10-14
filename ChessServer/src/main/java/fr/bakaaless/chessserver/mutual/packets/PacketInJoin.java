package fr.bakaaless.chessserver.mutual.packets;

public class PacketInJoin implements Packet {

    public String name;

    public PacketInJoin() {
    }

    public PacketInJoin(final String name) {
        this.name = name;
    }

}
