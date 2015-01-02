package de.hochschuletrier.gdw.ss12.menu;

import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import de.hochschuletrier.gdw.commons.gdx.menu.MenuManager;
import de.hochschuletrier.gdw.commons.gdx.menu.widgets.DecoImage;

public class MenuPageHelpImageScroller extends MenuPage {

    public MenuPageHelpImageScroller(Skin skin, MenuManager menuManager, String image) {
        super(skin, "menu_bg_help_detail");
        ScrollPane scrollPane = new ScrollPane(new DecoImage(assetManager.getTexture(image)), skin, "transparent");
        scrollPane.setFadeScrollBars(false);
        scrollPane.setFlickScroll(true);
        scrollPane.setBounds(70, 90, 900, 520);

        addActor(scrollPane);
        addCenteredButton(menuManager.getWidth() - 100, 54, 100, 40, "Zurück", () -> menuManager.popPage());
    }
}
