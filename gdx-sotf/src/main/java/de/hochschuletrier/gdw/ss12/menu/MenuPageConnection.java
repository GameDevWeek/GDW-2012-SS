package de.hochschuletrier.gdw.ss12.menu;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import de.hochschuletrier.gdw.commons.gdx.menu.MenuManager;
import de.hochschuletrier.gdw.commons.gdx.state.transition.SplitHorizontalTransition;
import de.hochschuletrier.gdw.ss12.game.Game;
import de.hochschuletrier.gdw.ss12.states.GameplayState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MenuPageConnection extends MenuPage {

    private static final Logger logger = LoggerFactory.getLogger(MenuPageConnection.class);

    private static final int LABEL_X = 50;
    private static final int LABEL_WIDTH = 200;
    private static final int INPUT_X = 250;
    private static final int INPUT_WIDTH = 300;

    private String ip = "localhost";
    private int port = 49999;
    private boolean valid = true;
    private final Type type;
    private final Label titelLabel;
    private final TextField username;
    private final TextField server;
    private final Label errorLabel;

    public enum Type {

        SINGLEPLAYER("menu_bg_singleplayer", "Starten", "Spiel starten"),
        CREATE_SERVER("menu_bg_create_server", "Erstellen", "Netzwerkspiel erstellen"),
        JOIN_SERVER("menu_bg_join_server", "Verbinden", "Netzwerkspiel beitreten");

        public final String image;
        public final String acceptText;
        public final String title;

        Type(String image, String acceptText, String title) {
            this.image = image;
            this.acceptText = acceptText;
            this.title = title;
        }
    }

    public MenuPageConnection(Skin skin, MenuManager menuManager, Type type) {
        super(skin, type.image);
        this.type = type;

        
        int y = 470;
        int yStep = 50;
        
        // Title
        titelLabel = new Label(type.title, skin, "connectionTitle"); // gray, verdana 46
        titelLabel.setBounds(LABEL_X, y, 600, 30);
        addActor(titelLabel);
        y -= 70;

        if (type != Type.SINGLEPLAYER) {
            // errorLabel
            errorLabel = new Label(type.title, skin, "connectionError"); // red, verdana 32
            errorLabel.setBounds(50, 50, 600, 20);
            errorLabel.setVisible(false);
            addActor(errorLabel);
        } else {
            errorLabel = null;
        }

        // Username
        username = createTextField(y, "Username", "Spieler");
        y -= yStep;

        if (type != Type.SINGLEPLAYER) {
            server = createTextField(y, "Server:Port", ip + ":" + port);
            y -= yStep;
        } else {
            server = null;
        }

        if (type != Type.JOIN_SERVER) {
            createInputLabel(y, "Karte");
            //fixme: mapnames from main
            String[] maps = new String[100];
            for (int i = 0; i < maps.length; i++) {
                maps[i] = "HumpNRun " + i;
            }
            SelectBox<String> selectBox = new SelectBox(skin);
            selectBox.setItems(maps);
            selectBox.setBounds(INPUT_X, y, INPUT_WIDTH, 30);
            selectBox.setMaxListCount(10);
            addActor(selectBox);
            y -= yStep;
        }

        TextButton button = addButton(INPUT_X, y, INPUT_WIDTH, 30, type.acceptText, this::onAccept, "default");
        button.getLabel().setAlignment(Align.left);

        addCenteredButton(menuManager.getWidth() - 100, 54, 100, 40, "Zurück", () -> menuManager.popPage());
    }

    private Label createInputLabel(int y, String text) {
        Label label = new Label(text, skin, "inputLabel");
        label.setBounds(LABEL_X, y, LABEL_WIDTH, 30);
        addActor(label);
        return label;
    }


    private TextField createTextField(int y, String label, String text) {
        createInputLabel(y, label);
        TextField textField = new TextField(text, skin); //font: verdana 32
        textField.setBounds(INPUT_X, y, INPUT_WIDTH, 30);
        addActor(textField);
        return textField;
    }

    public void activate() {
//        errorLabel.visible(false);

        // Restore values
//        SafeProperties properties = SotfGame.getSettings("settings");
//        name.text(properties.getString("player_name", ""));
//        server.text(properties.getString("last_host", "") + ":" + properties.getString("last_port", ""));
//
//        String lastMap = properties.getString("last_map", "");
//        mapToggle.state(0);
//        for (int i = 0; i < mapNames.length; i++) {
//            if (mapNames[i].equalsIgnoreCase(lastMap)) {
//                mapToggle.state(i);
//                break;
//            }
//        }
    }

    private void onAccept() {
        if(type == Type.SINGLEPLAYER) {
            //fixme: move to Main, set username
            if(!main.isTransitioning()) {
                Game game = new Game(assetManager);
                game.loadMap("data/maps/HumpNRun.tmx");
                GameplayState gameplayState = new GameplayState(assetManager, game);
                main.changeState(gameplayState, new SplitHorizontalTransition(500), null);
            }
            return;
        }
//        if(SotfGame.isClient() || SotfGame.isServer()) {
//            errorLabel.visible(true);
//            errorLabel.text("Error: Already connected");
//            return;
//        }
        parseServerString(server.getText());
        if (valid) {
//            // Store values
//            SafeProperties settings = SotfGame.getSettings("settings");
//            settings.setString("player_name", name.getText());
//            settings.setString("last_host", ip);
//            settings.setString("last_port", "" + port);
//            settings.setString("last_map", mapNames[mapToggle.getState()]);
//            SotfGame.storeSettings();

            if (type != Type.JOIN_SERVER) {
//                GameWorld world = GameWorld.getInstance();
//                world.loadMap(mapNames[mapToggle.getState()]);
//                world.setLocalPlayer(0, name.getText());
            }

            server.setStyle(skin.get(TextField.TextFieldStyle.class));
            try {
                //fixme:
                switch (type) {
                    case SINGLEPLAYER:
                        break;
                    case CREATE_SERVER:
                        break;
                    case JOIN_SERVER:
                        break;
                }
            } catch (Throwable e) {
                logger.error("Error creating game", e);
                if (errorLabel != null) {
                    errorLabel.setVisible(true);
                    errorLabel.setText("Error: " + e.getMessage());
                }
            }
        } else {
            server.setStyle(skin.get("error", TextField.TextFieldStyle.class));
        }
    }

    private void parseServerString(String connection) {
        try {
            String[] parts = connection.split(":");
            ip = parts[0];
            port = Integer.parseInt(parts[1]);
            valid = true;
        } catch (NumberFormatException e) {
            valid = false;
        }
    }
}
