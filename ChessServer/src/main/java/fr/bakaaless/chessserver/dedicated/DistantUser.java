package fr.bakaaless.chessserver.dedicated;

import com.esotericsoftware.kryonet.Connection;
import fr.bakaaless.chessserver.mutual.ChessServer;
import fr.bakaaless.chessserver.mutual.User;
import fr.bakaaless.chessserver.mutual.packets.Packet;

public class DistantUser extends User {

    private Connection connection;

    public DistantUser(final ChessServer server, final Connection connection, final String name, final int color) {
        super(server, name, color);
        this.connection = connection;
    }

    public Connection getConnection() {
        return this.connection;
    }

    @Override
    public void sendPacket(final Packet packet) {
        this.server.getLogger().info("Sending packet " + packet.getClass().getSimpleName() + " to " + this.name + " @ " + this.connection.getRemoteAddressTCP().getAddress().getHostAddress());
        this.connection.sendTCP(packet);
    }

    @Override
    public void disconnect(final KickReason reason) {
        this.disconnect(reason.getMessage());
    }

    @Override
    public void disconnect(final String reason) {
        super.disconnect(reason);
        if (this.connection.isConnected())
            this.connection.close();
    }
}
