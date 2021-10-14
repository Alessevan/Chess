package fr.bakaaless.chessserver.dedicated;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import fr.bakaaless.chessserver.mutual.ChessServer;
import fr.bakaaless.chessserver.mutual.Pieces;
import fr.bakaaless.chessserver.mutual.User;
import fr.bakaaless.chessserver.mutual.packets.*;
import jline.TerminalFactory;
import jline.console.ConsoleReader;
import org.fusesource.jansi.AnsiConsole;

import java.io.IOException;
import java.util.List;

public class DedicatedServer extends ChessServer {

    final Server server;
    final Thread consoleThread;

    public DedicatedServer() throws IOException {
        getLogger().info("Starting network dedicated server...");
        this.server = new Server();

        Network.register(this.server);
        server.addListener(new Listener() {

            @Override
            public void connected(Connection connection) {
                super.connected(connection);
                getLogger().info("Receive connection from " + connection.getRemoteAddressTCP().getAddress().getHostAddress() + " : waiting authentification...");
                new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (DedicatedServer.this.users.stream()
                            .filter(user -> user instanceof DistantUser)
                            .noneMatch(user -> ((DistantUser) user).getConnection() == connection)
                            && connection.isConnected()) {
                        getLogger().info("Close connection : not authentificated");
                        connection.close();
                    }
                }).start();
            }

            @Override
            public void disconnected(final Connection connection) {
                DedicatedServer.this.users.stream()
                        .filter(user -> {
                            if (user instanceof DistantUser distantUser)
                                return distantUser.getConnection() == connection;
                            return false;
                        })
                        .findFirst()
                        .ifPresent(user -> user.disconnect(KickReason.RESET_PEER));
            }

            @Override
            public void received(final Connection connection, final Object object) {
                if (object instanceof PacketInJoin packet) {

                    getLogger().info("Receiving authentification packet from " + connection.getRemoteAddressTCP().getAddress().getHostAddress() + " AKA " + packet.name);

                    if (DedicatedServer.this.users.stream().anyMatch(user -> user.getName().equalsIgnoreCase(packet.name))) {
                        getLogger().warn("Someone with this name is already authentified, this connection will be disconnected");
                        connection.close();
                        return;
                    }

                    final int color =
                            DedicatedServer.this.users.stream().filter(user -> user.getColor() == Pieces.WHITE.value() || user.getColor() == Pieces.BLACK.value()).count() == 2 ?
                                    Pieces.SPECTATOR.value() : DedicatedServer.this.users.stream().anyMatch(user -> user.getColor() == Pieces.WHITE.value()) ? Pieces.BLACK.value() : Pieces.WHITE.value();
                    final DistantUser user = new DistantUser(DedicatedServer.this, connection, packet.name, color);
                    DedicatedServer.this.users.add(user);

                    getLogger().info("Sending game field & color packets to " + packet.name);

                    user.sendPacket(new PacketOutConnected());
                    user.sendPacket(new PacketOutColor(color));
                    user.sendPacket(new PacketOutGameField(DedicatedServer.this.field, DedicatedServer.this.colorRound));
                    if (DedicatedServer.this.start)
                        user.sendPacket(new PacketOutPreviousMoves(DedicatedServer.this.previousMoves));

                    if (DedicatedServer.this.users.size() == 2 && !DedicatedServer.this.start) {
                        DedicatedServer.this.start = true;

                        final long[] time = new long[2];
                        time[0] = DedicatedServer.this.users.stream().filter(users -> users.getColor() == Pieces.WHITE.value()).map(User::getTimeLeft).findFirst().get();
                        time[1] = DedicatedServer.this.users.stream().filter(users -> users.getColor() == Pieces.BLACK.value()).map(User::getTimeLeft).findFirst().get();
                        final Packet timePacket = new PacketOutTime(time);
                        DedicatedServer.this.sendAllPacket(timePacket);

                        DedicatedServer.this.sendAllPacket(new PacketOutGameStart());
                        DedicatedServer.this.users.forEach(users -> {
                            users.resetTimeLeft();
                            users.sendPacket(new PacketOutGameStart());
                        });
                        DedicatedServer.this.timeUpdater.start();
                        DedicatedServer.this.timeUpdaterTimer.schedule(DedicatedServer.this.timeUpdater, 0L, 100L);
                    }
                } else if (object instanceof Packet packet) {
                    DedicatedServer.this.users.stream()
                            .filter(user -> {
                                if (user instanceof DistantUser distantUser)
                                    return distantUser.getConnection() == connection;
                                return false;
                            })
                            .findFirst()
                            .ifPresent(user -> user.receivePacket(packet));
                }
            }
        });

        this.server.bind(this.serverPort);
        this.server.start();

        consoleThread = new Thread(() -> {
            try {

                boolean useJline;

                String jline_UnsupportedTerminal = new String(new char[]{'j', 'l', 'i', 'n', 'e', '.', 'U', 'n', 's', 'u', 'p', 'p', 'o', 'r', 't', 'e', 'd', 'T', 'e', 'r', 'm', 'i', 'n', 'a', 'l'});
                String jline_terminal = new String(new char[]{'j', 'l', 'i', 'n', 'e', '.', 't', 'e', 'r', 'm', 'i', 'n', 'a', 'l'});

                useJline = !(jline_UnsupportedTerminal).equals(System.getProperty(jline_terminal));

                jline.TerminalFactory.registerFlavor(TerminalFactory.Flavor.WINDOWS, jline.UnsupportedTerminal.class);
                if (useJline)
                    AnsiConsole.systemInstall();
                else
                    System.setProperty(jline.TerminalFactory.JLINE_TERMINAL, jline.UnsupportedTerminal.class.getName());

                final ConsoleReader reader = new ConsoleReader("Chess - Server", System.in, System.out, null);
                String line;
                while (true) {
                    line = reader.readLine("\33[31m> \33[0m");
                    if (line == null)
                        line = "stop";

                    if (line.length() == 0)
                        continue;

                    switch (line.toLowerCase()) {
                        case "quit", "stop" -> {
                            logger.info("Shutdown...");
                            List.copyOf(DedicatedServer.this.users).forEach(user -> user.disconnect(KickReason.SHUTDOWN));
                            logger.info("Goodbye !");
                            System.exit(0);
                        }
                        default -> {
                            logger.info("Unknown command.");
                        }
                    }
                }
            } catch (IOException exception) {
                logger.fatal("Error while retrieve console command", exception);
            }
        });

        consoleThread.setName("ConsoleThread");
        consoleThread.setDaemon(true);
        consoleThread.start();

        getLogger().info("Chess server is ready ! (elapsed : " + (System.currentTimeMillis() - this.startTime) + "ms)");
    }
}
