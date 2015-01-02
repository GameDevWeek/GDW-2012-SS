package de.hochschuletrier.gdw.ss12.menu;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import de.hochschuletrier.gdw.commons.gdx.menu.MenuManager;
import de.hochschuletrier.gdw.commons.gdx.menu.widgets.DecoImage;
import de.hochschuletrier.gdw.commons.gdx.menu.widgets.RotatingDecoImage;

public class MenuPageRoot extends MenuPage {

    public enum Type {

        MAINMENU,
        INGAME
    }

    public MenuPageRoot(Skin skin, MenuManager menuManager, Type type) {
        super(skin, "menu_bg_root_top");

        addGear("menu_gear1", 824, 608, 7);
        addGear("menu_gear2", 637, 750, -7);
        addGear("menu_gear3", 308, 539, 5);
        addGear("menu_gear4", 173, 563, -2);
        addGear("menu_gear5", 20, 546, 10);
        addActor(new DecoImage(assetManager.getTexture("menu_bg_root_bottom")));

        int x = 100;
        int i = 0;
        int y = 370;
        int yStep = 55;
        if (type == Type.MAINMENU) {
            addPageEntry(menuManager, x, y - yStep * (i++), "Singleplayer", new MenuPageConnection(skin, menuManager, MenuPageConnection.Type.SINGLEPLAYER));
            addPageEntry(menuManager, x, y - yStep * (i++), "Spiel erstellen", new MenuPageConnection(skin, menuManager, MenuPageConnection.Type.CREATE_SERVER));
            addPageEntry(menuManager, x, y - yStep * (i++), "Spiel beitreten", new MenuPageConnection(skin, menuManager, MenuPageConnection.Type.JOIN_SERVER));
            addPageEntry(menuManager, x, y - yStep * (i++), "Einstellungen", new MenuPageOptions(skin, menuManager));
            addPageEntry(menuManager, x, y - yStep * (i++), "Hilfe", new MenuPageHelp(skin, menuManager));
            addPageEntry(menuManager, x, y - yStep * (i++), "Credits", new MenuPageCredits(skin, menuManager));
        } else {
            addLeftAlignedButton(x, y - yStep * (i++), 400, 50, "Fortsetzen", () -> menuManager.popPage());
            addLeftAlignedButton(x, y - yStep * (i++), 400, 50, "Spiel verlassen", () -> main.disconnect());
            addPageEntry(menuManager, x, y - yStep * (i++), "Einstellungen", new MenuPageOptions(skin, menuManager));
            addPageEntry(menuManager, x, y - yStep * (i++), "Hilfe", new MenuPageHelp(skin, menuManager));
            addPageEntry(menuManager, x, y - yStep * (i++), "Credits", new MenuPageCredits(skin, menuManager));
        }
        addCenteredButton(menuManager.getWidth() - 80, 54, 100, 40, "Exit", () -> System.exit(-1));
    }

    private void addGear(String name, int x, int y, int rotate) {
        final Texture texture = assetManager.getTexture(name);
        final RotatingDecoImage decoImage = new RotatingDecoImage(texture, rotate);
        decoImage.setPosition(x - texture.getWidth() / 2, y - texture.getHeight() / 2);
        addActor(decoImage);
    }

    protected final void addPageEntry(MenuManager menuManager, int x, int y, String text, MenuPage page) {
        menuManager.addLayer(page);
        addLeftAlignedButton(x, y, 300, 40, text, () -> menuManager.pushPage(page));
    }
}
