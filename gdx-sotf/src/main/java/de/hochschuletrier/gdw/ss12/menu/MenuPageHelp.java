package de.hochschuletrier.gdw.ss12.menu;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import de.hochschuletrier.gdw.commons.gdx.menu.MenuManager;

public class MenuPageHelp extends MenuPage {

    public MenuPageHelp(Skin skin, MenuManager menuManager) {
        super(skin, "menu_bg_help");

        int x = 100;
        int i = 0;
        int y = 370;
        int yStep = 55;
        addPageEntry(menuManager, x, y - yStep * (i++), "Spiel und Ziel", new MenuPageHelpDetail(skin, menuManager, "data/json/help_goal.json"));
        addPageEntry(menuManager, x, y - yStep * (i++), "Items und Erklärung", new MenuPageHelpDetail(skin, menuManager, "data/json/help_legend.json"));

        addCenteredButton(menuManager.getWidth() - 100, 54, 100, 40, "Zurück", () -> menuManager.popPage());
    }

    protected final void addPageEntry(MenuManager menuManager, int x, int y, String text, MenuPage page) {
        menuManager.addLayer(page);
        addLeftAlignedButton(x, y, 450, 40, text, () -> menuManager.pushPage(page));
    }
}
