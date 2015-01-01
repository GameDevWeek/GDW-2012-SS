package de.hochschuletrier.gdw.ss12.game.systems.network;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import de.hochschuletrier.gdw.commons.gdx.assets.AssetManagerX;
import de.hochschuletrier.gdw.ss12.game.ComponentMappers;
import de.hochschuletrier.gdw.ss12.game.Game;
import de.hochschuletrier.gdw.ss12.game.components.InputComponent;
import de.hochschuletrier.gdw.ss12.game.interfaces.SystemGameInitializer;

public class NetClientSendControlSystem extends EntitySystem implements SystemGameInitializer {

    private Game game;

    public NetClientSendControlSystem() {
        super(0);
    }

    @Override
    public void initGame(Game game, AssetManagerX assetManager) {
        this.game = game;
    }

    @Override
    public void update(float deltaTime) {
        Entity localPlayer = game.getLocalPlayer();
        InputComponent input = ComponentMappers.input.get(localPlayer);
//            if (serverConnection.isAccepted()) {
//                serverConnection.send(new PlayerControlDatagram(GameWorld.getInstance().getLocalPlayer()));
//            }
    }
}
