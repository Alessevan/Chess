package fr.bakaaless.chessserver.dedicated;

public enum KickReason {

    KICKED      ("Kicked by {0}."),
    RESET_PEER  ("Connection reset by peer."),
    SHUTDOWN    ("Server shutdown..."),
    ;

    private String message;

    KickReason(final String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }
}
