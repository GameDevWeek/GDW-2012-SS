package de.hochschuletrier.gdw.ss12.game.systems.network;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.math.Vector2;
import de.hochschuletrier.gdw.commons.gdx.assets.AssetManagerX;
import de.hochschuletrier.gdw.commons.netcode.core.NetConnection;
import de.hochschuletrier.gdw.commons.netcode.simple.NetDatagramHandler;
import de.hochschuletrier.gdw.ss12.game.ComponentMappers;
import de.hochschuletrier.gdw.ss12.game.Game;
import de.hochschuletrier.gdw.ss12.game.components.PlayerComponent;
import de.hochschuletrier.gdw.ss12.game.datagrams.PlayerStateDatagram;
import de.hochschuletrier.gdw.ss12.game.interfaces.SystemGameInitializer;
import de.hochschuletrier.gdw.ss12.game.datagrams.WorldSoundDatagram;

public class NetClientUpdateSystem extends EntitySystem implements NetDatagramHandler, SystemGameInitializer {

    private Game game;

    public NetClientUpdateSystem() {
        super(0);
    }

    @Override
    public void initGame(Game game, AssetManagerX assetManager) {
        this.game = game;
    }

    @Override
    public void update(float deltaTime) {
        // fixme: call update on manager
    }

    public void handle(WorldSoundDatagram datagram) {
        Vector2 position = datagram.getPosition();
        game.playGlobalSound(datagram.getSound(), position.x, position.y, false);
    }

    // fixme: handle methods for all datagrams
//    public void handle(WorldSetupDatagram datagram) {
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
//    }
//
//    public void handle(CreateEntityDatagram datagram) {
//        ITeam team = datagram.getTeam() == -1 ? null : GameWorld.getInstance().getTeam(datagram.getTeam());
//        Entity entity = (Entity) EntityFactory.create(datagram.getID(), datagram.getEntityType(), datagram.getPosition(), team);
//        if (entity instanceof IEatable) {
//            GameWorld.getInstance().addItem((IEatable) entity);
//        } else if (entity instanceof ICandle) {
//            GameWorld.getInstance().addItem((ICandle) entity);
//        } else {
//            throw new UnsupportedOperationException("Unknown entity class: " + entity.getClass().toString());
//        }
//    }
//
//    public void handle(RemoveEntityDatagram datagram) {
//        Entity e = EntityFactory.debugHashMapForEntities.get(entityId);
//
//        engine.removeEntity(e);
//    }
//
//    public void handle(WorldStateDatagram datagram) {
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
//    }
//
//    public void handle(PlayerStateDatagram datagram) {
//        NetConnection connection = datagram.getConnection();
//        Entity playerEntity = (Entity)connection.getAttachment();
//        PlayerComponent player = ComponentMappers.player.get(playerEntity);
//
//        // only handle the latest datagrams
//        if(player.lastSequenceId > datagram.getSequenceId()) {
//            player.setAnimState(datagram.getAnimState());
//            player.setMoveSpeed(datagram.getMoveSpeed());
//            player.setRadius(datagram.getRadius());
//            player.applyRenderEffects(datagram.getRenderEffectState());
//            Entity localPlayerEntity = game.getLocalPlayer();
//            if (datagram.getID() != localPlayer.getID()) {
//                if (player.isDead()) {
//                    player.getDeathPosition().set(datagram.getPosition());
//                } else {
//                    player.getPosition().set(datagram.getPosition());
//                }
//                player.setViewingAngle(datagram.getViewingAngle());
//            }
//        }
//    }
//
//    public void handle(EntityEventDatagram datagram) {
//        int id = datagram.getID();
//        Entity e = EntityFactory.debugHashMapForEntities.get(id);
//        if (e == null) {
//            // see if it is a player
//            for (IPlayer p : GameWorld.getInstance().getPlayers()) {
//                if (p.getID() == id) {
//                    e = p;
//                    break;
//                }
//            }
//            if (e == null) {
//                throw new IllegalArgumentException("You Sir have failed: " + id + " does not exist");
//            }
//        }
//
//        e.onEvent(datagram.getParam1(), datagram.getParam2());
//    }
}
