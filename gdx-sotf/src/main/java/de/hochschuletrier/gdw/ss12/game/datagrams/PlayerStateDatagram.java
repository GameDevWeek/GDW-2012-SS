package de.hochschuletrier.gdw.ss12.game.datagrams;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;

import de.hochschuletrier.gdw.commons.netcode.core.NetDatagram;
import de.hochschuletrier.gdw.commons.netcode.core.NetMessageIn;
import de.hochschuletrier.gdw.commons.netcode.core.NetMessageOut;
import de.hochschuletrier.gdw.commons.netcode.core.NetMessageType;
import de.hochschuletrier.gdw.ss12.game.ComponentMappers;
import de.hochschuletrier.gdw.ss12.game.components.InputComponent;
import de.hochschuletrier.gdw.ss12.game.components.PlayerComponent;
import de.hochschuletrier.gdw.ss12.game.components.PositionComponent;
import de.hochschuletrier.gdw.ss12.game.data.PlayerState;

/**
 * send from server only
 */
public final class PlayerStateDatagram extends NetDatagram {

    private long netId;
    private final Vector2 position = new Vector2();
    private float moveSpeed;
    private float viewingAngle;
    private float radius;
    private int effectBits;
    public PlayerState state;
//todo:    public final PlayerStatistic statistic = new PlayerStatistic();

    public static PlayerStateDatagram create(Entity entity) {
        PlayerStateDatagram datagram = DatagramFactory.create(PlayerStateDatagram.class);
        datagram.netId = entity.getId();

        PlayerComponent player = ComponentMappers.player.get(entity);
        PositionComponent position = ComponentMappers.position.get(entity);
        InputComponent input = ComponentMappers.input.get(entity);
        datagram.position.set(position.x, position.y);
        datagram.moveSpeed = input.speed;
        datagram.viewingAngle = player.angle;
        datagram.radius = player.radius;

        datagram.effectBits = player.effectBits;
        datagram.state = player.state;
        return datagram;
    }

    public long getNetId() {
        return netId;
    }

    public Vector2 getPosition() {
        return position;
    }

    public float getMoveSpeed() {
        return moveSpeed;
    }

    public float getViewingAngle() {
        return viewingAngle;
    }

    public float getRadius() {
        return radius;
    }

    public int getEffectBits() {
        return effectBits;
    }

    public PlayerState getState() {
        return state;
    }

    @Override
    public NetMessageType getMessageType() {
        return NetMessageType.NORMAL;
    }

    @Override
    public void writeToMessage(NetMessageOut message) {
        message.putLong(netId);
        message.putFloat(position.x);
        message.putFloat(position.y);
        message.putFloat(moveSpeed);
        message.putFloat(viewingAngle);
        message.putFloat(radius);
        message.putInt(effectBits);
        message.putEnum(state);
    }

    public @Override
    void readFromMessage(NetMessageIn message) {
        netId = message.getLong();
        position.x = message.getFloat();
        position.y = message.getFloat();
        moveSpeed = message.getFloat();
        viewingAngle = message.getFloat();
        radius = message.getFloat();
        effectBits = message.getInt();
        state = message.getEnum(PlayerState.class);
    }
}
