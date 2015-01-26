package de.hochschuletrier.gdw.ss12.game.systems.network;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.math.Vector2;
import de.hochschuletrier.gdw.commons.gdx.assets.AssetManagerX;
import de.hochschuletrier.gdw.commons.netcode.core.NetConnection;
import de.hochschuletrier.gdw.commons.netcode.simple.NetClientSimple;
import de.hochschuletrier.gdw.commons.netcode.simple.NetDatagramHandler;
import de.hochschuletrier.gdw.ss12.game.ComponentMappers;
import de.hochschuletrier.gdw.ss12.game.Game;
import de.hochschuletrier.gdw.ss12.game.components.NetPlayerComponent;
import de.hochschuletrier.gdw.ss12.game.components.PlayerComponent;
import de.hochschuletrier.gdw.ss12.game.datagrams.CreateEntityDatagram;
import de.hochschuletrier.gdw.ss12.game.datagrams.NoticeDatagram;
import de.hochschuletrier.gdw.ss12.game.datagrams.PlayerStateDatagram;
import de.hochschuletrier.gdw.ss12.game.datagrams.WorldSetupDatagram;
import de.hochschuletrier.gdw.ss12.game.interfaces.SystemGameInitializer;
import de.hochschuletrier.gdw.ss12.game.datagrams.WorldSoundDatagram;
import de.hochschuletrier.gdw.ss12.game.datagrams.WorldStateDatagram;

public class NetClientUpdateSystem extends EntitySystem implements NetDatagramHandler, SystemGameInitializer {

    private Game game;
    private NetClientSimple netClient;

    public NetClientUpdateSystem() {
        super(0);
    }

    @Override
    public void initGame(Game game, AssetManagerX assetManager) {
        this.game = game;
    }

    @Override
    public void update(float deltaTime) {
        netClient.update();
    }

    public void handle(WorldSoundDatagram datagram) {
        Vector2 position = datagram.getPosition();
        game.playGlobalSound(datagram.getSound(), position.x, position.y, false);
    }

    public void handle(NoticeDatagram datagram) {
        game.scheduleNoticeForPlayer(datagram.getNoticeType(), datagram.getDelay(), game.getLocalPlayer());
    }

    // fixme: handle methods for all datagrams
    public void handle(WorldSetupDatagram datagram) {
//        serverConnection.setAccepted(true);
//        GameWorld world = GameWorld.getInstance();
//        String newMapName = datagram.getMapname();
//
//        if (!newMapName.equals(world.getMapName())) {
//            world.loadMap(newMapName);
//        } else {
//            world.reset();
//        }
//        world.setLocalPlayer(datagram.getID(), "client " + datagram.getID());
//
//        world.paused = datagram.isPaused();
//        world.getLocalPlayer().setPosition(datagram.getStartPosition());
//
//        GameStates.CLANARENA.fadeActivate(700);
    }

    public void handle(CreateEntityDatagram datagram) {
//        ITeam team = datagram.getTeam() == -1 ? null : GameWorld.getInstance().getTeam(datagram.getTeam());
//        Entity entity = (Entity) EntityFactory.create(datagram.getID(), datagram.getEntityType(), datagram.getPosition(), team);
//        if (entity instanceof IEatable) {
//            GameWorld.getInstance().addItem((IEatable) entity);
//        } else if (entity instanceof ICandle) {
//            GameWorld.getInstance().addItem((ICandle) entity);
//        } else {
//            throw new UnsupportedOperationException("Unknown entity class: " + entity.getClass().toString());
//        }
    }
//
//    public void handle(RemoveEntityDatagram datagram) {
//        Entity e = EntityFactory.debugHashMapForEntities.get(entityId);
//
//        engine.removeEntity(e);
//    }
//

    public void handle(WorldStateDatagram datagram) {
//        GameWorld world = GameWorld.getInstance();
//
//        String[] playerNames = datagram.getPlayerNames();
//        for (int playerId = 0; playerId < playerNames.length; ++playerId) {
//            world.getPlayerByID(playerId).setName(playerNames[playerId]);
//        }
//
//        int[] numberTeamWins = datagram.getNumberTeamWins();
//        for (int team = 0; team < numberTeamWins.length; ++team) {
//            world.getTeam(team).setWins(numberTeamWins[team]);
//        }
//
//        byte[] pizzaCount = datagram.getPizzaCount();
//        for (int team = 0; team < pizzaCount.length; ++team) {
//            world.getTeam(team).setPizzaCount(pizzaCount[team]);
//        }
    }

    public Entity getPlayerByNetId(long netId) {
        return null; //fixme
    }

    public void handle(PlayerStateDatagram datagram) {
        Entity playerEntity = getPlayerByNetId(datagram.getNetId());
        if (playerEntity != null) {
            NetPlayerComponent netPlayer = ComponentMappers.netPlayer.get(playerEntity);

            // only handle the latest datagrams
            if (netPlayer.lastSequenceId > datagram.getSequenceId()) {
                netPlayer.lastSequenceId = datagram.getSequenceId();
                ComponentMappers.position.get(playerEntity).set(datagram.getPosition());
                ComponentMappers.input.get(playerEntity).speed = datagram.getMoveSpeed();
                PlayerComponent player = ComponentMappers.player.get(playerEntity);
                player.angle = datagram.getViewingAngle();
                player.radius = datagram.getRadius();
                player.effectBits = datagram.getEffectBits();
                player.state = datagram.getState();
                ComponentMappers.light.get(playerEntity).setFromPlayerRadius(player.radius);
            }
        }
    }
}
