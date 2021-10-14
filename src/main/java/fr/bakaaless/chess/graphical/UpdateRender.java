package fr.bakaaless.chess.graphical;

import fr.bakaaless.chess.Chess;
import javafx.application.Platform;

import java.util.TimerTask;

public class UpdateRender extends TimerTask {

    @Override
    public void run() {
        Platform.runLater(Chess.userInterface::render);
    }

}
