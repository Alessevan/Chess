package fr.bakaaless.chess.connection;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.rmi.TimeoutException;
import fr.bakaaless.chess.Chess;
import fr.bakaaless.chessserver.dedicated.Network;
import fr.bakaaless.chessserver.mutual.packets.Packet;
import fr.bakaaless.chessserver.mutual.packets.PacketInJoin;

import java.io.IOException;
import java.util.regex.Pattern;

public class DistantServer extends Server {

    public static Pattern IP_REGEX = Pattern.compile("(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)");

    private final String address;
    private final Integer port;

    private Client client;

    public DistantServer(final Chess chess, final String name, final String address, final int port) {
        super(chess, name);
        this.address = address;
        this.port = port;
    }

    @Override
    public void sendPacket(final Packet packet) {
        Chess.getLogger().info("Sending packet : " + packet.getClass().getSimpleName());
        this.client.sendTCP(packet);
    }

    @Override
    public void receivePacket(final Packet packet) {
        super.receivePacket(packet);
    }

    public void connect() {

        new Thread("ClientConnection") {
            @Override
            public void run() {
                try {
                    Chess.getLogger().info("Starting distant client...");
                    final Client client = new Client();
                    DistantServer.this.client = client;
                    client.start();

                    Chess.getLogger().info("Registering classes...");
                    Network.register(client);

                    Chess.getLogger().info("Registering listeners...");
                    client.addListener(new Listener() {

                        @Override
                        public void connected(final Connection connection) {
                            Chess.getLogger().info("Connected to " + connection.getRemoteAddressTCP().getAddress().getHostAddress());
                            client.sendTCP(new PacketInJoin(DistantServer.this.name));
                        }

                        @Override
                        public void received(final Connection connection, final Object object) {
                            Chess.getLogger().info("Receive raw packet : " + object.getClass().getSimpleName());
                            if (object instanceof Packet)
                                DistantServer.this.receivePacket((Packet) object);
                        }

                        @Override
                        public void disconnected(final Connection connection) {
                            Chess.getLogger().info("Disconnecting.");
                        }
                    });
                    Chess.getLogger().info("Trying to connect to : " + address + ":" + port);
                    client.connect(1000, address, port);
                    if (!client.isConnected())
                        throw new TimeoutException();
                } catch (TimeoutException | IOException e) {
                    Chess.getLogger().trace("Can't connect to server : " + address + ":" + port, e);
                }
            }
        }.start();
    }
}
