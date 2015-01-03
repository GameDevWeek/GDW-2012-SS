package de.hochschuletrier.gdw.ss12.menu;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.utils.Array;
import de.hochschuletrier.gdw.commons.gdx.menu.MenuManager;
import de.hochschuletrier.gdw.ss12.Settings;
import java.util.Map;
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
    private final SelectBox<MapInfo> mapSelect;

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
    
    private class MapInfo {
        public final String name;
        public final String mapFile;
        public final String screenshotFile;

        public MapInfo(String mapFile, String name) {
            this.name = name;
            this.mapFile = mapFile + ".tmx";
            this.screenshotFile = mapFile + ".png";
        }

        @Override
        public String toString() {
            return name;
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
            Array<MapInfo> maps = new Array();
            for (Map.Entry<String, String> map : main.getMaps().entrySet()) {
                maps.add(new MapInfo(map.getKey(), map.getValue()));
            }
            maps.sort((MapInfo a, MapInfo b)->a.name.compareToIgnoreCase(b.name));
            mapSelect = new SelectBox(skin);
            mapSelect.setItems(maps);
            mapSelect.setBounds(INPUT_X, y, INPUT_WIDTH, 30);
            mapSelect.setMaxListCount(10);
            addActor(mapSelect);
            y -= yStep;
        } else {
            mapSelect = null;
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
        errorLabel.setVisible(false);

        restoreSettings();
    }

    private void restoreSettings() {
        if(mapSelect != null) {
//            Settings.MAP_FILE.get()
//            selectBox.setSelected();
        }
        username.setText(Settings.PLAYER_NAME.get());
        if(server != null) {
            server.setText(Settings.LAST_HOST.get() + ":" + Settings.LAST_PORT.get());
        }
    }

    private void storeSettings() {
        if(mapSelect != null) {
            Settings.MAP_FILE.set(mapSelect.getSelected().mapFile);
        }
        Settings.PLAYER_NAME.set(username.getText());
        if(server != null) {
            Settings.LAST_HOST.set(ip);
            Settings.LAST_PORT.set(port);
        }
        Settings.flush();
    }

    private void onAccept() {
        if (type == Type.SINGLEPLAYER) {
            storeSettings();
            main.startSingleplayer();
            return;
        }

        parseServerString(server.getText());
        if (valid) {
            storeSettings();

            try {
                if (type == Type.CREATE_SERVER) {
                    main.createServer(ip, port);
                } else {
                    main.joinServer(ip, port);
                }
            } catch (Throwable e) {
                logger.error("Error creating game", e);
                if (errorLabel != null) {
                    errorLabel.setVisible(true);
                    errorLabel.setText("Error: " + e.getMessage());
                }
            }
        }
    }

    private void parseServerString(String connection) {
        try {
            String[] parts = connection.split(":");
            ip = parts[0];
            port = Integer.parseInt(parts[1]);
            valid = true;
            server.setStyle(skin.get(TextField.TextFieldStyle.class));
        } catch (NumberFormatException e) {
            valid = false;
            server.setStyle(skin.get("error", TextField.TextFieldStyle.class));
        }
    }
}
