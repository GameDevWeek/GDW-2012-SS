package de.hochschuletrier.gdw.ss12.menu;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import de.hochschuletrier.gdw.commons.gdx.menu.MenuManager;
import de.hochschuletrier.gdw.commons.gdx.menu.widgets.DecoImage;
import de.hochschuletrier.gdw.commons.jackson.JacksonReader;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MenuPageHelpDetail extends MenuPage {

    private static final Logger logger = LoggerFactory.getLogger(MenuPageHelpDetail.class);

    public MenuPageHelpDetail(Skin skin, MenuManager menuManager, String jsonFile) {
        super(skin, "menu_bg_help_detail");

        try {
            List<String> list = JacksonReader.readList(jsonFile, String.class);

            Table table = new Table();
            table.setWidth(850);
            for (String text : list) {
                String[] split = text.split(":", 2);
                if (split.length == 2) {
                    switch (split[0]) {
                        case "images":
                            table.row();
                            table.add(new DecoImage(assetManager.getTexture(split[1]))).align(Align.left);
                            break;
                        case "title":
                            table.row().fillX().expandX();
                            Label title = new Label(split[1], skin, "helpTitle");
                            title.setWrap(true);
                            title.setAlignment(Align.left);
                            table.add(title);
                            break;
                        default:
                            table.row().fillX().expandX();
                            Label label = new Label(split[1], skin, "helpText");
                            label.setWrap(true);
                            label.setAlignment(Align.left);
                            table.add(label);
                            break;
                    }
                }
            }

            ScrollPane scrollPane = new ScrollPane(table, skin, "transparent");
            scrollPane.setFadeScrollBars(false);
            scrollPane.setFlickScroll(true);
            scrollPane.setBounds(70, 90, 900, 520);

            addActor(scrollPane);
        } catch (Exception e) {
            logger.error("Error reading help json", e);
        }

        addCenteredButton(menuManager.getWidth() - 100, 54, 100, 40, "Zurück", () -> menuManager.popPage());
    }
}
