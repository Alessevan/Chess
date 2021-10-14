package fr.bakaaless.chess.graphical;

import fr.bakaaless.chess.Chess;
import fr.bakaaless.chess.connection.DistantServer;
import fr.bakaaless.chessserver.mutual.Pieces;
import fr.bakaaless.chessserver.mutual.packets.PacketInPieceMove;
import fr.bakaaless.chessserver.mutual.packets.PacketInSelectPiece;
import fr.bakaaless.chessserver.mutual.packets.PacketOutGameStop;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.util.Random;
import java.util.Timer;
import java.util.function.Function;

public class UserInterface extends Application {

    private static Chess game;

    public static void startInterface(final Chess chess, final String... args) {
        UserInterface.game = chess;
        launch(args);
    }

    private Scene scene;
    public Runnable changeScreen;

    private GraphicsContext context;
    private double width;
    private double height;
    private double cellSize;

    private Timer timer;
    private boolean updater;

    private Font letterCoordinateFont;
    private Font numberCoordinateFont;
    private Font timerFont;
    private Font endStateFont;
    private Font endReasonFont;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Chess.userInterface = this;

        Chess.getLogger().info("Loading assets...");
        AssetLoader.loadAssets();

        this.letterCoordinateFont = Font.loadFont(AssetLoader.getFont("Letter"), 25);
        this.numberCoordinateFont = Font.loadFont(AssetLoader.getFont("Number"), 25);
        this.timerFont = Font.loadFont(AssetLoader.getFont("Timer"), 32);
        this.endStateFont = Font.loadFont(AssetLoader.getFont("EndState"), 140);
        this.endReasonFont = Font.loadFont(AssetLoader.getFont("EndReason"), 45);


        Chess.getLogger().info("Creating panel...");
        this.width = Double.parseDouble(Chess.getProperties().getProperty("window-width"));
        this.height = Double.parseDouble(Chess.getProperties().getProperty("window-height"));
        this.cellSize = this.height / 10D;

        final StackPane pane = new StackPane();
        final StackPane paneConnection = new StackPane();
        final Canvas canvas = new Canvas(this.width, this.height);
        final GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(5);
        grid.setHgap(5);

        pane.getChildren().add(canvas);
        paneConnection.getChildren().add(grid);

        primaryStage.setScene(new Scene(paneConnection, 270, 75));
        primaryStage.setTitle("Chess - Client");
        primaryStage.setResizable(false);
        primaryStage.show();
        primaryStage.centerOnScreen();

        primaryStage.setOnCloseRequest(event ->
                game.stop()
        );

        final TextField name = new TextField();
        name.setPromptText("Name");
        name.setText(Chess.getProperties().getProperty("default-name"));
        name.setPrefColumnCount(10);
        GridPane.setRowIndex(name, 0);
        grid.getChildren().add(name);

        final Button random = new Button("Random");
        random.setOnAction(event -> {
            final int leftLimit = 48; // numeral '0'
            final int rightLimit = 122; // letter 'z'
            final int targetStringLength = new Random().nextInt(10) + 6;
            final Random randomObject = new Random();

            final String generatedString = randomObject.ints(leftLimit, rightLimit + 1)
                    .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                    .limit(targetStringLength)
                    .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                    .toString();
            name.setText(generatedString);
        });

        GridPane.setConstraints(random, 1, 0);
        grid.getChildren().add(random);

        final Button save = new Button("Save");
        save.setOnAction(event ->
            Chess.saveName(name.getText())
        );
        GridPane.setConstraints(save, 2, 0);
        grid.getChildren().add(save);

        final TextField address = new TextField();
        address.setPromptText(new Random().nextInt(256) + "." + new Random().nextInt(256) + "." + new Random().nextInt(256) + "." + new Random().nextInt(256) + "(:" + (new Random().nextInt(65536 - 1024) + 1024) + ")");
        address.setPrefColumnCount(10);
        address.getText();
        GridPane.setConstraints(address, 0, 1);
        grid.getChildren().add(address);

        this.scene = new Scene(pane, this.width, this.height);
        this.changeScreen = () -> {
            primaryStage.setScene(this.scene);
            primaryStage.centerOnScreen();
            UserInterface.this.context = canvas.getGraphicsContext2D();
            UserInterface.this.connected(pane);
        };

        final Function<?, Boolean> localHandler = event -> false;

        final Function<?, Boolean> distantHandler = event -> {
            final String server = address.getText();
            if (server.isBlank() || name.getText().isBlank())
                return false;
            try {
                final String[] serverAddress = server.split(":");
                if (serverAddress.length == 1) {
                    final DistantServer distantServer = new DistantServer(game, name.getText(), server, Integer.parseInt(Chess.getProperties().getProperty("default-port")));
                    game.connectServer(distantServer);
                    return distantServer.isConnected();
                } else if (serverAddress.length == 2) {
                    final DistantServer distantServer = new DistantServer(game, name.getText(), serverAddress[0], Integer.parseInt(serverAddress[1]));
                    game.connectServer(distantServer);
                    return distantServer.isConnected();
                } else {
                    Chess.getLogger().error("Can't understand the following address : " + server);
                }
            } catch (Exception e) {
                Chess.getLogger().error("Can't understand the following address : " + server);
            }
            return false;
        };

        final EventHandler<KeyEvent> keyPressed = event -> {
            if (event.getCode() == KeyCode.ENTER) {
                if (name.getText().isBlank())
                    return;
                boolean succeed;
                if (address.getText().isBlank() && Chess.getProperties().getProperty("default-server").isBlank())
                    succeed = localHandler.apply(null);
                else
                    succeed = distantHandler.apply(null);
                if (succeed)
                    changeScreen.run();
            }
        };

        paneConnection.setOnKeyPressed(keyPressed);

        final Button connection = new Button("Connect");
        connection.setOnAction(event -> {
            if (distantHandler.apply(null))
                changeScreen.run();
        });

        GridPane.setConstraints(connection, 1, 1);
        grid.getChildren().add(connection);

        final Button local = new Button("Local");
        local.setOnAction(event -> {
            if (localHandler.apply(null))
                changeScreen.run();
        });
        GridPane.setConstraints(local, 2, 1);
        grid.getChildren().add(local);
    }


    private void connected(final Pane pane) {
        this.context.setImageSmoothing(true);
        this.context.setFontSmoothingType(FontSmoothingType.LCD);
        this.context.setFill(Color.rgb(45, 49, 51));
        this.context.fillRect(0, 0, this.width, this.height);

        pane.setOnKeyPressed(this::onKeyPressed);
        pane.setOnMouseClicked(this::onMouseClicked);

        this.render();
        this.updater = true;
        this.timer = new Timer("Rendering Updater");
        this.timer.schedule(new UpdateRender(), 0L, 100L);
    }

    public void onKeyPressed(final KeyEvent event) {

    }

    public void onMouseClicked(final MouseEvent event) {
        if (!game.hasStarted())
            return;
        if (event.getButton() == MouseButton.PRIMARY) {
            final int[] position = new int[] { (int) (Math.floor(event.getX() / this.cellSize) - 1), (int) (Math.floor(event.getY() / this.cellSize) - 1)};
            if (position[0] >= game.getField().length || position[1] >= game.getField()[position[0]].length)
                return;
            final int piece = game.getField()[position[0]][position[1]];
            if ((game.getSelected() != null && game.getSelected().length != 0) && (piece == 0 || Pieces.getColor(piece).value() != game.getColor())) {
                if (game.getMoves().stream().anyMatch(pieces -> pieces[0] == position[0] && pieces[1] == position[1])) {
                    game.setSelected(new int[0]);
                    game.getServer().sendPacket(new PacketInPieceMove(position[0], position[1]));
                }
                return;
            }
            game.setSelected(position);
            game.getServer().sendPacket(new PacketInSelectPiece(position));
        }
    }

    public void render() {
        if (game.isStopped()) {
        if (this.updater) {
            this.timer.cancel();
            this.updater = false;
        }

        this.context.setFill(Color.rgb(100, 100, 100, 0.15));
        this.context.fillRect(0, this.height * 0.25, this.width, this.height * 0.5);
        this.context.setFont(this.endStateFont);
        this.context.setTextAlign(TextAlignment.CENTER);
        this.context.setTextBaseline(VPos.CENTER);
        this.context.setFill(Color.rgb(238, 238, 210));
        if (game.getColor() == Pieces.SPECTATOR.value()) {
            switch (game.getColor()) {
                case 0b0 -> {
                    this.context.fillText("WHITE WINS", this.width * 0.5, this.height * 0.5, this.width);
                }
                case 0b1 -> {
                    this.context.fillText("BLACK WINS", this.width * 0.5, this.height * 0.5, this.width);
                }
                case 0b11 -> {
                    this.context.fillText("PAT", this.width * 0.5, this.height * 0.5, this.width);
                }
            }
        } else if (game.getWinner() == game.getColor()) {
            this.context.setFill(Color.rgb(212, 200, 10));
            this.context.fillText("VICTORY", this.width * 0.5, this.height * 0.5, this.width);
        } else if (game.getWinner() == Pieces.SPECTATOR.value()) {
            this.context.fillText("PAT", this.width * 0.5, this.height * 0.5, this.width);
        } else {
            this.context.setFill(Color.rgb(140, 0, 0));
            this.context.fillText("DEFEAT", this.width * 0.5, this.height * 0.5, this.width);
        }
        this.context.setFill(Color.rgb(150, 150, 150));
        this.context.setFont(this.endReasonFont);
        switch (PacketOutGameStop.PacketOutGameStopReason.values()[game.getEndReason()]) {
            case MAT -> {
                this.context.fillText("The king is in chess and there is no longer move allowed", this.width * 0.5, this.height * 0.66);
            }
            case PAT -> {
                this.context.fillText("The king isn't in chess and there is no longer move allowed", this.width * 0.5, this.height * 0.66);
            }
            case TIMES_UP -> {
                this.context.fillText("The allowed time is up", this.width * 0.5, this.height * 0.66);
            } case DISCONNECT -> {
                this.context.fillText("An opponent disconnected", this.width * 0.5, this.height * 0.66);
            }
        }

        return;
        }

        for (int x = 0; x < this.height / this.cellSize - 2; x++) {
            for (int y = 0; y < this.height / this.cellSize - 2; y++) {
                if ((x + y) % 2 == 0)
                    this.context.setFill(Color.rgb(238, 238, 210));
                else
                    this.context.setFill(Color.rgb(118, 150, 86));
                if (game.getSelected().length > 0)
                    if (x == game.getSelected()[0] && y == game.getSelected()[1])
                        this.context.setFill(Color.rgb(245, 189, 62, 0.6));
                this.context.fillRect((x + 1) * this.cellSize, (y + 1) * this.cellSize, this.cellSize, this.cellSize);
            }
        }
        if (game.getPreviousMoves().size() > 0) {
            this.context.setFill(Color.rgb(246, 246, 103, 0.6));
            final int[][] previousMove = game.getPreviousMoves().get(game.getPreviousMoves().size() - 1);
            this.context.fillRect((previousMove[0][0] + 1) * this.cellSize, (previousMove[0][1] + 1) *  this.cellSize, this.cellSize, this.cellSize);
            this.context.fillRect((previousMove[1][0] + 1) * this.cellSize, (previousMove[1][1] + 1) *  this.cellSize, this.cellSize, this.cellSize);
        }
        if (game.getMoves() != null) {
            this.context.setFill(Color.rgb(100, 100, 100));
            for (int[] position : game.getMoves()) {
                if (game.getField()[position[0]][position[1]] == 0)
                    this.context.fillArc((position[0] + 1.375) * this.cellSize, (position[1] + 1.375) * this.cellSize, 0.25 * this.cellSize, 0.25 * this.cellSize, 0, 360, ArcType.CHORD);
                else
                    this.context.fillRect((position[0] + 1) * this.cellSize, (position[1] + 1) *  this.cellSize, this.cellSize, this.cellSize);
            }
        }
        final String[] dataSet = { "A", "B", "C", "D", "E", "F", "G", "H" };
        this.context.setFill(Color.rgb(238, 238, 210));
        this.context.setTextAlign(TextAlignment.CENTER);
        this.context.setFont(this.letterCoordinateFont);
        this.context.setTextBaseline(VPos.CENTER);
        for (int index = 0; index < dataSet.length; index++)
            this.context.fillText(dataSet[index], (index + 1 + 0.5) * this.cellSize, this.cellSize / 1.25, this.cellSize);
        this.context.setFont(this.numberCoordinateFont);
        for (int index = 1; index < 9; index++)
            this.context.fillText(String.valueOf(index), this.cellSize / 1.60, (index + 0.5) * this.cellSize, this.cellSize);
        for (int x = 0; x < game.getField().length; x++) {
            for (int y = 0; y < game.getField()[x].length; y++) {
                if (game.getField()[x][y] == 0)
                    continue;
                final Pieces[] piece = Pieces.getPieceNColor(game.getField()[x][y]);
                final Image image = AssetLoader.getImage(piece[0].name().toUpperCase() + "_" + piece[1].name().toUpperCase());
                this.context.drawImage(image, (x + 1) * this.cellSize, (y + 1) * this.cellSize, this.cellSize, this.cellSize);
            }
        }
        if (game.hasStarted() && game.getRoundColor() == Pieces.BLACK.value())
            this.context.setFill(Color.rgb(150, 150, 150));
        else
            this.context.setFill(Color.rgb(75, 75, 75));
        this.context.fillRoundRect(9.5 * this.cellSize, 1.25 * this.cellSize, 1.3 * this.cellSize, 0.5 * this.cellSize, 20, 10);
        if (game.hasStarted() && game.getRoundColor() == Pieces.WHITE.value())
            this.context.setFill(Color.rgb(150, 150, 150));
        else
            this.context.setFill(Color.rgb(75, 75, 75));
        this.context.fillRoundRect(9.5 * this.cellSize, 8.25 * this.cellSize, 1.3 * this.cellSize, 0.5 * this.cellSize, 20, 10);
        this.context.setFill(Color.rgb(238, 238, 210));
        this.context.setFont(this.timerFont);
        this.context.setTextAlign(TextAlignment.CENTER);
        this.context.setTextBaseline(VPos.CENTER);
        if (!game.hasStarted()) {
            this.context.fillText("00 : 00", 10.15 * this.cellSize, 1.5 * this.cellSize, 1.3 * this.cellSize);
            this.context.fillText("00 : 00", 10.15 * this.cellSize, 8.5 * this.cellSize, 1.3 * this.cellSize);
        } else {
            final long now = System.currentTimeMillis();
            this.context.fillText(this.format(game.getTimeLeft()[1] - (game.getRoundColor() == Pieces.BLACK.value() ? now - game.getLastSynchronisation() : 0)), 10.15 * this.cellSize, 1.5 * this.cellSize, 1.3 * this.cellSize);
            this.context.fillText(this.format(game.getTimeLeft()[0] - (game.getRoundColor() == Pieces.WHITE.value() ? now - game.getLastSynchronisation() : 0)), 10.15 * this.cellSize, 8.5 * this.cellSize, 1.3 * this.cellSize);
        }
    }

    private String format(final long millis) {
        final int seconds = Math.toIntExact((millis / 1000) % 60);
        final int minutes = Math.toIntExact((millis / 60000));
        return (minutes < 10 ? "0" + minutes : minutes) + " : " + (seconds < 10 ? "0" + seconds : seconds);
    }
}
