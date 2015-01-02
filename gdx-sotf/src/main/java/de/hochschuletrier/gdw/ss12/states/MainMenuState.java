package de.hochschuletrier.gdw.ss12.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import de.hochschuletrier.gdw.commons.gdx.menu.MenuManager;
import de.hochschuletrier.gdw.commons.gdx.assets.AssetManagerX;
import de.hochschuletrier.gdw.commons.gdx.input.InputInterceptor;
import de.hochschuletrier.gdw.commons.gdx.state.BaseGameState;
import de.hochschuletrier.gdw.commons.gdx.utils.DrawUtil;
import de.hochschuletrier.gdw.ss12.Main;
import de.hochschuletrier.gdw.commons.gdx.menu.widgets.DecoImage;
import de.hochschuletrier.gdw.ss12.menu.MenuPageRoot;

/**
 * Menu state
 *
 * @author Santo Pfingsten
 */
public class MainMenuState extends BaseGameState {

    private final Music music;

    InputInterceptor inputProcessor;
    private final MenuManager menuManager;

    public MainMenuState(AssetManagerX assetManager) {
        menuManager = new MenuManager(Main.WINDOW_WIDTH, Main.WINDOW_HEIGHT);
        music = assetManager.getMusic("menu");

        music.setLooping(true);
//        music.play();

        Main.inputMultiplexer.addProcessor(menuManager.getInputProcessor());
        
        Skin skin = Main.getInstance().getSkin();
        final MenuPageRoot menuPageRoot = new MenuPageRoot(skin, menuManager, MenuPageRoot.Type.MAINMENU);
        menuManager.addLayer(menuPageRoot);
        
        menuManager.addLayer(new DecoImage(assetManager.getTexture("menu_fg_border")));
        menuManager.pushPage(menuPageRoot);
//        menuManager.getStage().setDebugAll(true);
        
        Main.getInstance().addScreenListener(menuManager);
    }

    public void render() {
        Main.getInstance().screenCamera.bind();
        DrawUtil.fillRect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), Color.GRAY);
        menuManager.render();
    }

    @Override
    public void update(float delta) {
        menuManager.update(delta);
        render();
    }

    @Override
    public void onEnterComplete() {
        menuManager.enableInput(true);
    }

    @Override
    public void onLeave(BaseGameState nextState) {
        menuManager.enableInput(false);
    }

    @Override
    public void dispose() {
        menuManager.dispose();
    }
}
