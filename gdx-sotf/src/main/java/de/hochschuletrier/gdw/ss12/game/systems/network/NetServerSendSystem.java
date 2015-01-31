package de.hochschuletrier.gdw.ss12.game.systems.network;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.utils.Array;
import de.hochschuletrier.gdw.commons.netcode.simple.NetServerSimple;
import de.hochschuletrier.gdw.commons.tiled.TiledMap;
import de.hochschuletrier.gdw.ss12.game.components.PlayerComponent;
import de.hochschuletrier.gdw.ss12.game.components.SetupComponent;
import de.hochschuletrier.gdw.ss12.game.data.Team;
import de.hochschuletrier.gdw.ss12.game.datagrams.CreateEntityDatagram;
import de.hochschuletrier.gdw.ss12.game.datagrams.PlayerUpdatesDatagram;
import de.hochschuletrier.gdw.ss12.game.datagrams.RemoveEntityDatagram;
import de.hochschuletrier.gdw.ss12.game.datagrams.TeamStateDatagram;
import de.hochschuletrier.gdw.ss12.game.interfaces.SystemMapInitializer;

public class NetServerSendSystem extends EntitySystem implements EntityListener, SystemMapInitializer {

    private final NetServerSimple netServer;
    private ImmutableArray<Entity> players;
    private Array<Team> teams;

    public NetServerSendSystem(NetServerSimple netServer) {
        super(0);

        this.netServer = netServer;
    }

    @Override
    public void addedToEngine(Engine engine) {
        engine.addEntityListener(Family.all(SetupComponent.class).get(), this);
        players = engine.getEntitiesFor(Family.all(PlayerComponent.class).get());
    }

    @Override
    public void removedFromEngine(Engine engine) {
        engine.removeEntityListener(this);
        players = null;
    }

    @Override
    public void update(float deltaTime) {
        netServer.broadcastUnreliable(PlayerUpdatesDatagram.create(players));

        for (Team team : teams) {
            if (team.changed) {
                team.changed = false;
                netServer.broadcastReliable(TeamStateDatagram.create(team));
            }
        }
    }

    @Override
    public void entityAdded(Entity entity) {
        netServer.broadcastReliable(CreateEntityDatagram.create(entity));
    }

    @Override
    public void entityRemoved(Entity entity) {
        netServer.broadcastReliable(RemoveEntityDatagram.create(entity));
    }

    @Override
    public void initMap(TiledMap map, Array<Team> teams) {
        this.teams = teams;
    }
}
