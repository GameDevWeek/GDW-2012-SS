package de.hochschuletrier.gdw.ss12.game.datagrams;

import de.hochschuletrier.gdw.ss12.game.data.PlayerUpdate;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.utils.ImmutableArray;

import de.hochschuletrier.gdw.commons.netcode.core.NetDatagram;
import de.hochschuletrier.gdw.commons.netcode.core.NetMessageIn;
import de.hochschuletrier.gdw.commons.netcode.core.NetMessageOut;
import de.hochschuletrier.gdw.commons.netcode.core.NetMessageType;
import de.hochschuletrier.gdw.commons.utils.Assert;
import de.hochschuletrier.gdw.ss12.game.ComponentMappers;
import de.hochschuletrier.gdw.ss12.game.Constants;
import de.hochschuletrier.gdw.ss12.game.components.InputComponent;
import de.hochschuletrier.gdw.ss12.game.components.PlayerComponent;
import de.hochschuletrier.gdw.ss12.game.components.PositionComponent;
import de.hochschuletrier.gdw.ss12.game.data.PlayerState;

/**
 * send from server only
 */
public final class PlayerUpdatesDatagram extends NetDatagram {

    private final PlayerUpdate[] updates = new PlayerUpdate[Constants.MAX_PLAYERS];
    private byte numPlayers;

    public PlayerUpdatesDatagram() {
        for (int i = 0; i < updates.length; i++) {
            updates[i] = new PlayerUpdate();
        }
    }

    public static PlayerUpdatesDatagram create(ImmutableArray<Entity> entities) {
        Assert.that(entities.size() <= Constants.MAX_PLAYERS, "Number of players exceeds MAX_PLAYERS!");

        PlayerUpdatesDatagram datagram = DatagramFactory.create(PlayerUpdatesDatagram.class);
        datagram.numPlayers = 0;
        for (Entity entity : entities) {
            PlayerUpdate update = datagram.updates[datagram.numPlayers++];
            update.netId = entity.getId();

            PlayerComponent player = ComponentMappers.player.get(entity);
            PositionComponent position = ComponentMappers.position.get(entity);
            InputComponent input = ComponentMappers.input.get(entity);
            update.position.set(position.x, position.y);
            update.moveDirection.set(input.moveDirection);
            update.speed = input.speed;
            update.angle = player.angle;
            update.radius = player.radius;

            update.effectBits = player.effectBits;
            update.state = player.state;
        }
        return datagram;
    }

    public PlayerUpdate[] getUpdates() {
        return updates;
    }

    public byte getNumPlayers() {
        return numPlayers;
    }

    @Override
    public NetMessageType getMessageType() {
        return NetMessageType.NORMAL;
    }

    @Override
    public void writeToMessage(NetMessageOut message) {
        message.put(numPlayers);
        for (int i = 0; i < numPlayers; i++) {
            PlayerUpdate update = updates[i];
            message.putLong(update.netId);
            message.putFloat(update.position.x);
            message.putFloat(update.position.y);
            message.putFloat(update.moveDirection.x);
            message.putFloat(update.moveDirection.y);
            message.putFloat(update.speed);
            message.putFloat(update.angle);
            message.putFloat(update.radius);
            message.putInt(update.effectBits);
            message.putEnum(update.state);
        }
    }

    public @Override
    void readFromMessage(NetMessageIn message) {
        numPlayers = message.get();
        for (int i = 0; i < numPlayers; i++) {
            PlayerUpdate update = updates[i];
            update.netId = message.getLong();
            update.position.x = message.getFloat();
            update.position.y = message.getFloat();
            update.moveDirection.x = message.getFloat();
            update.moveDirection.y = message.getFloat();
            update.speed = message.getFloat();
            update.angle = message.getFloat();
            update.radius = message.getFloat();
            update.effectBits = message.getInt();
            update.state = message.getEnum(PlayerState.class);
        }
    }
}
