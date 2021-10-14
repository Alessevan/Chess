package fr.bakaaless.chessserver.mutual.packets;

public class PacketOutChat implements Packet {

    public String user;
    public String message;

    public PacketOutChat() {
    }

    public PacketOutChat(final String user, final String message) {
        this.user = user;
        this.message = message;
    }
}
