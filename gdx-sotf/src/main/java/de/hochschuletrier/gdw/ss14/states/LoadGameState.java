package de.hochschuletrier.gdw.ss14.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

import de.hochschuletrier.gdw.commons.gdx.assets.AssetManagerX;
import de.hochschuletrier.gdw.commons.gdx.utils.DrawUtil;
import de.hochschuletrier.gdw.ss14.Main;

public class LoadGameState extends MyBaseGameState {

    private boolean isDone;
    private Texture loadscreen;

    public LoadGameState() {
    }

    @Override
    public void init(AssetManagerX assetManager) {
        super.init(assetManager);
        
        loadscreen = new Texture(Gdx.files.internal("data/images/LoadScreen/titlescreen.png"));
    }

    public void render() {
        Main.getInstance().screenCamera.bind();
        DrawUtil.fillRect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), Color.BLACK);
        DrawUtil.fillRect(Gdx.graphics.getWidth() / 2 - 245, Gdx.graphics.getHeight() / 2 - 70, (int) (590f * assetManager.getProgress()), 50, Color.RED);

        float x = (Gdx.graphics.getWidth() - loadscreen.getWidth()) / 2;
        float y = (Gdx.graphics.getHeight() - loadscreen.getHeight()) / 2;
        DrawUtil.draw(loadscreen, x, y);
    }

    @Override
    public void update(float delta) {
        if (!isDone) {
            assetManager.update();

            if (assetManager.getProgress() == 1) {
                Main.getInstance().onLoadComplete();

                isDone = true;
            }
        }

        render();
    }

    @Override
    public void dispose() {
    }
}
