package fr.bakaaless.chess.connection;

import fr.bakaaless.chess.Chess;
import fr.bakaaless.chessserver.mutual.packets.*;
import javafx.application.Platform;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public abstract class Server {

    protected final Chess game;
    protected final String name;

    private boolean connected;

    public Server(final Chess game, final String name) {
        this.game = game;
        this.name = name;
        this.connected = false;
    }

    public abstract void sendPacket(final Packet packet);

    public void receivePacket(final Packet packet) {
        Chess.getLogger().info("Received packet " + packet.getClass().getSimpleName());
        if (packet instanceof PacketOutConnected) {
            this.connected = true;
            Platform.runLater(Chess.userInterface.changeScreen);
            return;
        } else if (packet instanceof PacketOutGameStart) {
            Chess.getLogger().info("Receiving game start information.");
            this.game.setStart(true);
        } else if (packet instanceof PacketOutGameField gameField) {
            Chess.getLogger().info("Receiving game field information.");
            this.game.setField(gameField.field);
            this.game.setRoundColor(gameField.colorRound);
        } else if (packet instanceof PacketOutColor gameColor){
            Chess.getLogger().info("Receiving game color information.");
            this.game.setColor(gameColor.color);
        } else if (packet instanceof PacketOutPieceMoves pieceMoves) {
            Chess.getLogger().info("Receiving piece moves information.");
            this.game.setMoves(pieceMoves.moves);
        } else if (packet instanceof PacketOutUnselectPiece unselectPiece) {
            Chess.getLogger().info("Receiving unselect piece order.");
            if (game.getSelected().length != 0)
                if (game.getSelected()[0] == unselectPiece.coordinates[0] && game.getSelected()[1] == unselectPiece.coordinates[1])
                    game.setSelected(new int[0]);
        } else if (packet instanceof PacketOutTime time) {
            Chess.getLogger().info("Receiving time information.");
            game.setTimeLeft(time.time);
        } else if (packet instanceof PacketOutPreviousMoves previousMoves)  {
            Chess.getLogger().info("Receiving previous moves information.");
            game.setPreviousMoves(previousMoves.moves);
        } else if (packet instanceof PacketOutGameStop gameStop) {
            Chess.getLogger().info("Receiving stop information.");
            game.setWinner(gameStop.winner);
            game.setEndReason(gameStop.reason);
            game.setStop(true);
        }
        Platform.runLater(() -> Chess.userInterface.render());
    }

    public boolean isConnected() {
        if (this.connected)
            return true;
        final CompletableFuture<Boolean> connected = new CompletableFuture<>();
        new Thread(() -> {
            try {
                for (int i = 0; i < 500; i++) {
                    if (Server.this.connected)
                        connected.complete(true);
                    Thread.sleep(1);
                }
                connected.complete(Server.this.connected);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        try {
            return connected.get(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }
        return false;
    }
}

