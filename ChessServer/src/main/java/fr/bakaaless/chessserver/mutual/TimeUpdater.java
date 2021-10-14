package fr.bakaaless.chessserver.mutual;

import fr.bakaaless.chessserver.mutual.packets.Packet;
import fr.bakaaless.chessserver.mutual.packets.PacketOutGameStop;

import java.util.TimerTask;
import java.util.stream.Collectors;

public class TimeUpdater extends TimerTask {

    private final ChessServer server;
    private long lastUpdate;

    public TimeUpdater(final ChessServer server) {
        this.server = server;
    }

    public void start() {
        this.lastUpdate = System.currentTimeMillis();
    }

    @Override
    public void run() {
        final long now = System.currentTimeMillis();
        if (this.server.start && this.server.users.stream().filter(user -> user.color == Pieces.WHITE.value() || user.color == Pieces.BLACK.value()).count() > 1) {
            this.server.users.stream().filter(user -> user.color == this.server.colorRound).forEach(user -> user.updateTimeLeft(now - this.lastUpdate));
            for (final User user : this.server.users.stream().filter(user -> user.color == this.server.colorRound).filter(user -> user.getTimeLeft() <= 0L).collect(Collectors.toList())) {
                final boolean isWhite = user.color == 0;
                final Packet packet = new PacketOutGameStop(isWhite ? Pieces.BLACK.value() : Pieces.WHITE.value(), PacketOutGameStop.PacketOutGameStopReason.TIMES_UP.ordinal());
                this.server.sendAllPacket(packet);
                this.server.stop();
                return;
            }
        } else if (this.server.start) {
            final boolean isWhite = this.server.users.stream().filter(user -> user.color != Pieces.SPECTATOR.value() && user.color == Pieces.WHITE.value()).count() == 1;
            final Packet packet = new PacketOutGameStop(isWhite ? Pieces.WHITE.value() : Pieces.BLACK.value(), PacketOutGameStop.PacketOutGameStopReason.DISCONNECT.ordinal());
            this.server.sendAllPacket(packet);
            this.server.stop();
        }
        this.lastUpdate = now;
    }

}
