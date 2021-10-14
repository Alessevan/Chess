package fr.bakaaless.chessserver.mutual.packets;

public class PacketInChat implements Packet {

    public String message;

    public PacketInChat() {
    }

    public PacketInChat(final String message) {
        this.message = message;
    }
}
