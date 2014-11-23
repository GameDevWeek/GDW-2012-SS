package de.hochschuletrier.gdw.ss14.game.systems.rendering;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.graphics.Texture;
import de.hochschuletrier.gdw.commons.gdx.tiled.TiledMapRendererGdx;
import de.hochschuletrier.gdw.commons.resourcelocator.CurrentResourceLocator;
import de.hochschuletrier.gdw.commons.tiled.Layer;
import de.hochschuletrier.gdw.commons.tiled.TileSet;
import de.hochschuletrier.gdw.commons.tiled.TiledMap;
import de.hochschuletrier.gdw.commons.tiled.tmx.TmxImage;
import de.hochschuletrier.gdw.ss14.game.componentdata.Team;
import de.hochschuletrier.gdw.ss14.game.interfaces.SystemMapInitializer;
import java.util.HashMap;

public class RenderMapSystem extends EntitySystem implements SystemMapInitializer {
    private TiledMapRendererGdx mapRenderer;
    private TiledMap map;

    public RenderMapSystem() {
        super(0);
    }

    public RenderMapSystem(int priority) {
        super(priority);
    }
    
    @Override
	public void addedToEngine(Engine engine) {
    }
    
    @Override
	public void removedFromEngine(Engine engine) {
    }

    @Override
    public void initMap(TiledMap map, Team[] teams) {
        this.map = map;
        HashMap<TileSet, Texture> tilesetImages = new HashMap();
        for (TileSet tileset : map.getTileSets()) {
            TmxImage img = tileset.getImage();
            String filename = CurrentResourceLocator.combinePaths(tileset.getFilename(), img.getSource());
            tilesetImages.put(tileset, new Texture(filename));
        }
        mapRenderer = new TiledMapRendererGdx(map, tilesetImages);
    }

    @Override
    public void update(float deltaTime) {
        mapRenderer.update(deltaTime);
        for (Layer layer : map.getLayers()) {
            mapRenderer.render(0, 0, layer);
        }
    }
}
