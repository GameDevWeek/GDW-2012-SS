package de.hochschuletrier.gdw.ss12.game.systems.rendering;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.utils.Array;
import de.hochschuletrier.gdw.commons.tiled.ITiledMapRenderer;
import de.hochschuletrier.gdw.commons.tiled.Layer;
import de.hochschuletrier.gdw.commons.tiled.TiledMap;
import de.hochschuletrier.gdw.ss12.game.data.Team;
import de.hochschuletrier.gdw.ss12.game.interfaces.SystemMapInitializer;

public class RenderMapSystem extends EntitySystem implements SystemMapInitializer {

    private TiledMap map;
    private ITiledMapRenderer mapRenderer;

    public RenderMapSystem() {
        super(0);
    }

    @Override
    public void initMap(TiledMap map, Array<Team> teams) {
        this.map = map;
        mapRenderer = map.getRenderer();
    }

    @Override
    public void update(float deltaTime) {
        mapRenderer.update(deltaTime);
        render();
    }

    void render() {
        for (Layer layer : map.getLayers()) {
            mapRenderer.render(0, 0, layer);
        }
    }
}
