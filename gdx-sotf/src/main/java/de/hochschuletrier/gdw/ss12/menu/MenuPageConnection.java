package de.hochschuletrier.gdw.ss12.menu;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import de.hochschuletrier.gdw.commons.gdx.menu.MenuManager;
import de.hochschuletrier.gdw.commons.gdx.menu.widgets.DecoImage;
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

    private String ip;
    private int port;
    private boolean valid = true;
    private final Type type;
    private final Label titleLabel;
    private final TextField username;
    private final TextField serverIp;
    private final TextField serverPort;
    private final Label errorLabel;
    private final SelectBox<MapInfo> mapSelect;
    private final DecoImage previewImage;

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

        public MapInfo(String mapFile, String name) {
            this.name = name;
            this.mapFile = mapFile;
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
        titleLabel = new Label(type.title, skin, "connectionTitle");
        titleLabel.setBounds(LABEL_X, y, 600, 30);
        addActor(titleLabel);
        y -= 70;

        // Error label
        if (type != Type.SINGLEPLAYER) {
            errorLabel = new Label(type.title, skin, "connectionError");
            errorLabel.setBounds(50, 50, 600, 20);
            errorLabel.setVisible(false);
            addActor(errorLabel);
        } else {
            errorLabel = null;
        }

        // Username
        username = createTextField(y, "Username", "Spieler");
        y -= yStep;

        if (type == Type.JOIN_SERVER) {
            serverIp = createTextField(y, "Server IP", "");
            y -= yStep;
        } else {
            serverIp = null;
        }

        if (type != Type.SINGLEPLAYER) {
            serverPort = createTextField(y, "Server Port", "");
            y -= yStep;
        } else {
            serverPort = null;
        }

        // Map selection
        if (type != Type.JOIN_SERVER) {
            createInputLabel(y, "Karte");
            Array<MapInfo> maps = new Array();
            for (Map.Entry<String, String> map : main.getMaps().entrySet()) {
                maps.add(new MapInfo(map.getKey(), map.getValue()));
            }
            maps.sort((MapInfo a, MapInfo b) -> a.name.compareToIgnoreCase(b.name));
            mapSelect = new SelectBox(skin);
            mapSelect.setItems(maps);
            mapSelect.setBounds(INPUT_X, y, INPUT_WIDTH, 30);
            mapSelect.setMaxListCount(10);
            mapSelect.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    previewImage.setTexture(assetManager.getTexture(mapSelect.getSelected().mapFile));
                }
            });
            addActor(mapSelect);
            y -= yStep;
        } else {
            mapSelect = null;
        }

        // Accept button
        TextButton button = addButton(INPUT_X, y, INPUT_WIDTH, 30, type.acceptText, this::onAccept, "default");
        button.getLabel().setAlignment(Align.left);

        addCenteredButton(menuManager.getWidth() - 100, 54, 100, 40, "Zurück", () -> menuManager.popPage());

        // Map preview
        if (mapSelect != null) {
            previewImage = new DecoImage(assetManager.getTexture(mapSelect.getSelected().mapFile));
            previewImage.setBounds(600, 470 - 256, 256, 256);
            addActor(previewImage);
        } else {
            previewImage = null;
        }
    }

    private Label createInputLabel(int y, String text) {
        Label label = new Label(text, skin, "inputLabel");
        label.setBounds(LABEL_X, y, LABEL_WIDTH, 30);
        addActor(label);
        return label;
    }

    private TextField createTextField(int y, String label, String text) {
        createInputLabel(y, label);
        TextField textField = new TextField(text, skin);
        textField.setBounds(INPUT_X, y, INPUT_WIDTH, 30);
        addActor(textField);
        return textField;
    }

    @Override
    public void setVisible(boolean visible) {
        if (titleLabel != null && isVisible() != visible) {
            if (visible) {
                if (errorLabel != null) {
                    errorLabel.setVisible(false);
                }
                restoreSettings();
            }
        }
        super.setVisible(visible);
    }

    private void restoreSettings() {
        if (mapSelect != null) {
            String mapFile = Settings.MAP_FILE.get();
            Array<MapInfo> maps = mapSelect.getItems();
            for (MapInfo map : maps) {
                if (map.mapFile.equals(mapFile)) {
                    mapSelect.setSelected(map);
                    break;
                }
            }
        }
        username.setText(Settings.PLAYER_NAME.get());
        if (serverIp != null) {
            serverIp.setText(Settings.LAST_HOST.get());
        }
        if (serverPort != null) {
            serverPort.setText("" + Settings.LAST_PORT.get());
        }
    }

    private void storeSettings() {
        if (mapSelect != null) {
            Settings.MAP_FILE.set(mapSelect.getSelected().mapFile);
        }
        Settings.PLAYER_NAME.set(username.getText());
        if (serverIp != null) {
            Settings.LAST_HOST.set(ip);
        }
        if (serverPort != null) {
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

        parseServerString();
        if (valid) {
            storeSettings();

            try {
                if (type == Type.CREATE_SERVER) {
                    main.createServer(port);
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

    private void parseServerString() {
        try {
            if (serverIp != null) {
                ip = serverIp.getText();
            }
            port = Integer.parseInt(serverPort.getText());
            valid = true;
            serverIp.setStyle(skin.get(TextField.TextFieldStyle.class));
        } catch (NumberFormatException e) {
            valid = false;
            serverPort.setStyle(skin.get("error", TextField.TextFieldStyle.class));
        }
    }
}
