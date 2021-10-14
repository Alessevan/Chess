package fr.bakaaless.chessserver;

import fr.bakaaless.chessserver.dedicated.DedicatedServer;

import java.io.IOException;

public class Main {

    public static void main(final String... args) throws IOException {
        new DedicatedServer();
    }

}
