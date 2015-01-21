package de.hochschuletrier.gdw.ss12.game.datagrams;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import de.hochschuletrier.gdw.commons.netcode.core.NetDatagram;
import de.hochschuletrier.gdw.commons.netcode.core.NetMessageIn;
import de.hochschuletrier.gdw.commons.netcode.core.NetMessageOut;
import de.hochschuletrier.gdw.commons.netcode.core.NetMessageType;
import de.hochschuletrier.gdw.ss12.game.ComponentMappers;
import de.hochschuletrier.gdw.ss12.game.components.InputComponent;
import de.hochschuletrier.gdw.ss12.game.components.NetPlayerComponent;

/**
 * send from client only
 */
public final class PlayerInputDatagram extends NetDatagram {

    private long netId;
    private final Vector2 moveDirection = new Vector2();
    private int dropItemCount;
    private float speed;

    public static PlayerInputDatagram create(Entity playerEntity) {
        PlayerInputDatagram datagram = DatagramFactory.create(PlayerInputDatagram.class);
        InputComponent input = ComponentMappers.input.get(playerEntity);
        NetPlayerComponent netPlayer = ComponentMappers.netPlayer.get(playerEntity);
        
        datagram.netId =  netPlayer.id;
        datagram.moveDirection.set(input.moveDirection);
        datagram.dropItemCount = netPlayer.dropItemCount;
        datagram.speed = input.speed;
        return datagram;
    }

    public long getNetId() {
        return netId;
    }

    public Vector2 getMoveDirection() {
        return moveDirection;
    }

    public int getDropItemCount() {
        return dropItemCount;
    }

    public float getSpeed() {
        return speed;
    }

    @Override
    public void reset() {
        super.reset();
        
        netId = -1;
        moveDirection.setZero();
        dropItemCount = 0;
        speed = 0;
    }

    @Override
    public NetMessageType getMessageType() {
        return NetMessageType.NORMAL;
    }

    @Override
    public void writeToMessage(NetMessageOut message) {
        message.putLong(netId);
        message.putFloat(moveDirection.x);
        message.putFloat(moveDirection.y);
        message.putInt(dropItemCount);
        message.putFloat(speed);
    }

    public @Override
    void readFromMessage(NetMessageIn message) {
        netId = message.getLong();
        moveDirection.x = message.getFloat();
        moveDirection.y = message.getFloat();
        dropItemCount = message.getInt();
        speed = message.getFloat();
    }
}
