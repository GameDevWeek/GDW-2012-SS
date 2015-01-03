package de.hochschuletrier.gdw.ss12.menu;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import de.hochschuletrier.gdw.commons.gdx.menu.MenuManager;
import de.hochschuletrier.gdw.commons.gdx.sceneanimator.SceneAnimator;
import de.hochschuletrier.gdw.commons.gdx.sceneanimator.SceneAnimatorActor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MenuPageCredits extends MenuPage {

    private static final Logger logger = LoggerFactory.getLogger(MenuPageCredits.class);

    private SceneAnimator sceneAnimator;

    public MenuPageCredits(Skin skin, MenuManager menuManager) {
        super(skin, "menu_bg_credits");

        try {
            sceneAnimator = new SceneAnimator(skin::getFont, "data/json/credits.json");
            addActor(new SceneAnimatorActor(sceneAnimator));
        } catch (Exception ex) {
            logger.error("Error loading credits", ex);
        }

        addCenteredButton(menuManager.getWidth() - 100, 54, 100, 40, "Zurück", () -> menuManager.popPage());
    }

}
