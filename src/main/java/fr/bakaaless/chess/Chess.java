package fr.bakaaless.chess;

import fr.bakaaless.chess.connection.DistantServer;
import fr.bakaaless.chess.connection.Server;
import fr.bakaaless.chess.graphical.UserInterface;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.Properties;
import java.util.UUID;

public class Chess {

    private static long startTime;
    private static Logger logger;
    public static UserInterface userInterface;

    private static File propertiesFile;
    private static Properties clientProperties;

    public static Properties getProperties() {
        return clientProperties;
    }

    public static void main(final String... args) {
        startTime = System.currentTimeMillis();
        Thread.currentThread().setName("Chess - Client");
        logger = LogManager.getLogger("Chess - Client");
        logger.atLevel(Level.ALL);

        getLogger().info("Getting client properties...");
        propertiesFile = new File("client.properties");
        final boolean firstCreate = !propertiesFile.exists();
        if (!propertiesFile.exists()) {
            getLogger().warn(" - Creating file client.properties");
            try {
                FileUtils.copyInputStreamToFile(ClassLoader.getSystemResourceAsStream("config/client.properties"), propertiesFile);
            } catch (IOException e) {
                getLogger().fatal(" * Can't create file `client.properties`", e);
                System.exit(-1);
            }
        }

        clientProperties = new Properties();
        try {
            clientProperties.load(new BufferedInputStream(new FileInputStream(propertiesFile)));
            if (firstCreate) {
                clientProperties.setProperty("default-name", UUID.randomUUID().toString().replaceAll("-", ""));
                clientProperties.setProperty("default-server", "");
                clientProperties.setProperty("default-port", String.valueOf(35000));
                clientProperties.setProperty("window-width", String.valueOf(1200));
                clientProperties.setProperty("window-height", String.valueOf(800));
                try {
                    clientProperties.store(new FileWriter(propertiesFile), "");
                } catch (IOException e) {
                    getLogger().fatal(" * Can't write in file `client.properties`", e);
                    System.exit(-1);
                }
            }
        } catch (IOException e) {
            getLogger().fatal(" * Can't read file `client.properties`", e);
            System.exit(-1);
        }

        logger.info("Starting client interface...");
        UserInterface.startInterface(new Chess(), args);
    }

    public static void saveName(final String text) {
        try {
            clientProperties.setProperty("default-name", text);
            clientProperties.store(new FileWriter(propertiesFile), "");
        } catch (IOException e) {
            getLogger().fatal(" * Can't write in file `client.properties`", e);
            System.exit(-1);
        }
    }

    public static Logger getLogger() {
        return logger;
    }

    private Server server;
    private boolean start;
    private boolean stop;
    private int winner;
    private int endReason;
    private int[][] field;
    private ArrayList<int[][]> previousMoves;
    private ArrayList<int[]> moves;
    private int[] selected;
    private int color;
    private int roundColor;

    private long[] timeLeft;
    private long lastSynchronisation;

    public Chess() {
        this.field = new int[0][0];
        this.previousMoves = new ArrayList<>();
        this.selected = new int[0];
    }

    public void connectServer(final DistantServer distantServer) {
        this.server = distantServer;
        distantServer.connect();
    }

    public void stop() {
        logger.info("Shutdown...");
        System.exit(0);
    }

    public String formatCoordinates(final int x, final int z) {
        final String[] dataSet = { "A", "B", "C", "D", "E", "F", "G", "H" };
        return dataSet[x] + (z + 1);
    }

    public Server getServer() {
        return this.server;
    }

    public boolean hasStarted() {
        return this.start;
    }

    public void setStart(final boolean start) {
        this.start = start;
    }

    public boolean isStopped() {
        return this.stop;
    }

    public void setStop(final boolean stop) {
        this.stop = stop;
    }

    public int getWinner() {
        return this.winner;
    }

    public void setWinner(final int winner) {
        this.winner = winner;
    }

    public int getEndReason() {
        return this.endReason;
    }

    public void setEndReason(final int endReason) {
        this.endReason = endReason;
    }

    public int[][] getField() {
        return this.field;
    }

    public void setField(final int[][] field) {
        this.field = field;
    }

    public ArrayList<int[][]> getPreviousMoves() {
        return this.previousMoves;
    }

    public void setPreviousMoves(final ArrayList<int[][]> previousMoves) {
        this.previousMoves = previousMoves;
    }

    public int[] getSelected() {
        return this.selected;
    }

    public void setSelected(final int[] selected) {
        this.selected = selected;
    }

    public void setTimeLeft(long[] timeLeft) {
        this.timeLeft = timeLeft;
        this.lastSynchronisation = System.currentTimeMillis();
    }

    public long[] getTimeLeft() {
        return this.timeLeft;
    }

    public long getLastSynchronisation() {
        return this.lastSynchronisation;
    }

    public ArrayList<int[]> getMoves() {
        return this.moves;
    }

    public void setMoves(final ArrayList<int[]> moves) {
        this.moves = moves;
    }

    public int getColor() {
        return this.color;
    }

    public void setColor(final int color) {
        this.color = color;
    }

    public int getRoundColor() {
        return this.roundColor;
    }

    public void setRoundColor(final int roundColor) {
        this.roundColor = roundColor;
    }
}
