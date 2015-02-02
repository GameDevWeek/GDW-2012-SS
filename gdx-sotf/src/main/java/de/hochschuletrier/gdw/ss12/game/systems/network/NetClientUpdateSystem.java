package de.hochschuletrier.gdw.ss12.game.systems.network;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import de.hochschuletrier.gdw.commons.gdx.assets.AssetManagerX;
import de.hochschuletrier.gdw.commons.gdx.utils.DrawUtil;
import de.hochschuletrier.gdw.commons.netcode.simple.NetClientSimple;
import de.hochschuletrier.gdw.commons.netcode.simple.NetDatagramHandler;
import de.hochschuletrier.gdw.ss12.Main;
import de.hochschuletrier.gdw.ss12.game.ComponentMappers;
import de.hochschuletrier.gdw.ss12.game.Constants;
import de.hochschuletrier.gdw.ss12.game.Game;
import de.hochschuletrier.gdw.ss12.game.components.PlayerComponent;
import de.hochschuletrier.gdw.ss12.game.data.PlayerSetup;
import de.hochschuletrier.gdw.ss12.game.data.PlayerUpdate;
import de.hochschuletrier.gdw.ss12.game.data.Team;
import de.hochschuletrier.gdw.ss12.game.datagrams.CreateEntityDatagram;
import de.hochschuletrier.gdw.ss12.game.datagrams.NoticeDatagram;
import de.hochschuletrier.gdw.ss12.game.datagrams.PlayerNameDatagram;
import de.hochschuletrier.gdw.ss12.game.datagrams.PlayerUpdatesDatagram;
import de.hochschuletrier.gdw.ss12.game.datagrams.RemoveEntityDatagram;
import de.hochschuletrier.gdw.ss12.game.datagrams.WorldSetupDatagram;
import de.hochschuletrier.gdw.ss12.game.interfaces.SystemGameInitializer;
import de.hochschuletrier.gdw.ss12.game.datagrams.WorldSoundDatagram;
import de.hochschuletrier.gdw.ss12.game.datagrams.TeamStateDatagram;
import de.hochschuletrier.gdw.ss12.game.systems.EntitySpawnSystem;
import java.util.HashMap;

public class NetClientUpdateSystem extends EntitySystem implements NetDatagramHandler, SystemGameInitializer, NetClientSimple.Listener {

    private Game game;
    private final NetClientSimple netClient;
    private PooledEngine engine;
    private EntitySpawnSystem entitySpawnSystem;
    private final HashMap<Long, Entity> netEntityMap = new HashMap();

    public NetClientUpdateSystem(NetClientSimple netClient) {
        super(0);
        this.netClient = netClient;
    }

    @Override
    public void initGame(Game game, AssetManagerX assetManager, PooledEngine engine) {
        this.engine = engine;
        this.game = game;
        entitySpawnSystem = engine.getSystem(EntitySpawnSystem.class);
        netClient.setHandler(this);
        netClient.setListener(this);
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
        game.scheduleNoticeForPlayer(datagram.getNoticeType(), datagram.getDelay(), datagram.getTimeLeft(), game.getLocalPlayer());
    }

    public void handle(WorldSetupDatagram datagram) {
        DrawUtil.batch.end();
        game.loadMap(datagram.getMapName());
        DrawUtil.batch.begin();

        Array<Team> teams = game.getTeams();
        int numPlayers = datagram.getNumPlayers();
        PlayerSetup[] players = datagram.getPlayers();
        for (int i = 0; i < numPlayers; i++) {
            PlayerSetup player = players[i];
            Entity entity = entitySpawnSystem.createPlayer(player.start.x, player.start.y, teams.get(player.team), player.name);
            netEntityMap.put(player.netId, entity);
        }

        Entity entity = netEntityMap.get(datagram.getNetId());
        if (entity != null) {
            game.setLocalPlayer(entity);
        } else {
            //fixme: warn?
        }
    }

    public void handle(CreateEntityDatagram datagram) {
        final Vector2 position = datagram.getPosition();
        final byte teamId = datagram.getTeam();
        Team team = teamId == -1 ? null : game.getTeams().get(teamId);
        Entity entity = entitySpawnSystem.createStaticEntity(datagram.getEntityType(), position.x, position.y, Constants.ITEM_RADIUS, team);
        netEntityMap.put(datagram.getNetId(), entity);
    }

    public void handle(RemoveEntityDatagram datagram) {
        Entity entity = netEntityMap.get(datagram.getNetId());
        if (entity != null) {
            engine.removeEntity(entity);
        } else {
            //fixme: warn?
        }
    }

    public void handle(PlayerNameDatagram datagram) {
        Entity entity = netEntityMap.get(datagram.getNetId());
        if (entity != null) {
            ComponentMappers.player.get(entity).name = datagram.getName();
        }
    }

    public void handle(TeamStateDatagram datagram) {
        Team team = game.getTeams().get(datagram.getTeamId());
        team.setWins(datagram.getNumberTeamWins());
        team.setPizzaCount(datagram.getPizzaCount());
    }

    public void handle(PlayerUpdatesDatagram datagram) {
        int numPlayers = datagram.getNumPlayers();
        PlayerUpdate[] updates = datagram.getUpdates();
        for (int i = 0; i < numPlayers; i++) {
            PlayerUpdate update = updates[i];
            Entity playerEntity = netEntityMap.get(update.netId);
            if (playerEntity != null) {

                // only handle the latest datagrams
                PlayerComponent player = ComponentMappers.player.get(playerEntity);
                if (player.lastSequenceId < datagram.getSequenceId()) {
                    player.lastSequenceId = datagram.getSequenceId();
                    ComponentMappers.position.get(playerEntity).set(update.position);
                    ComponentMappers.input.get(playerEntity).speed = update.speed;
                    player.angle = update.angle;
                    player.radius = update.radius;
                    player.effectBits = update.effectBits;
                    player.state = update.state;
                    ComponentMappers.light.get(playerEntity).setFromPlayerRadius(player.radius);
                }
            }
        }
    }

    @Override
    public void onDisconnect() {
        Main.getInstance().disconnect();
    }
}
