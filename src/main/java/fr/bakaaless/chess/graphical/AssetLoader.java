package fr.bakaaless.chess.graphical;

import de.codecentric.centerdevice.javafxsvg.SvgImageLoaderFactory;
import fr.bakaaless.chess.Chess;
import javafx.scene.image.Image;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class AssetLoader {

    private static Map<String, String> fonts = new HashMap<>();
    private static Map<String, Image> images = new HashMap<>();

    public static InputStream getFont(final String family) {
        return ClassLoader.getSystemResourceAsStream(fonts.get(family));
    }

    public static Image getImage(final String title) {
        return images.get(title);
    }

    public static void loadAssets() {
        Chess.getLogger().info("Loading fonts...");
        fonts.put("Letter", "assets/fonts/Kabrio-Abarth-Regular.ttf");
        fonts.put("Number", "assets/fonts/BalooTammudu2-Medium.ttf");
        fonts.put("Timer", "assets/fonts/QanelasRegular.otf");
        fonts.put("EndState", "assets/fonts/AloneInSpaceRegular.ttf");
        fonts.put("EndReason", "assets/fonts/QanelasRegular.otf");

        SvgImageLoaderFactory.install();

        Chess.getLogger().info("Loading images...");
        images.put("BISHOP_BLACK", new Image(ClassLoader.getSystemResourceAsStream("assets/images/pieces/bishop_black.svg")));
        images.put("BISHOP_WHITE", new Image(ClassLoader.getSystemResourceAsStream("assets/images/pieces/bishop_white.svg")));
        images.put("KING_BLACK", new Image(ClassLoader.getSystemResourceAsStream("assets/images/pieces/king_black.svg")));
        images.put("KING_WHITE", new Image(ClassLoader.getSystemResourceAsStream("assets/images/pieces/king_white.svg")));
        images.put("KNIGHT_BLACK", new Image(ClassLoader.getSystemResourceAsStream("assets/images/pieces/knight_black.svg")));
        images.put("KNIGHT_WHITE", new Image(ClassLoader.getSystemResourceAsStream("assets/images/pieces/knight_white.svg")));
        images.put("PAWN_BLACK", new Image(ClassLoader.getSystemResourceAsStream("assets/images/pieces/pawn_black.svg")));
        images.put("PAWN_WHITE", new Image(ClassLoader.getSystemResourceAsStream("assets/images/pieces/pawn_white.svg")));
        images.put("QUEEN_BLACK", new Image(ClassLoader.getSystemResourceAsStream("assets/images/pieces/queen_black.svg")));
        images.put("QUEEN_WHITE", new Image(ClassLoader.getSystemResourceAsStream("assets/images/pieces/queen_white.svg")));
        images.put("ROOK_BLACK", new Image(ClassLoader.getSystemResourceAsStream("assets/images/pieces/rook_black.svg")));
        images.put("ROOK_WHITE", new Image(ClassLoader.getSystemResourceAsStream("assets/images/pieces/rook_white.svg")));
    }
}
