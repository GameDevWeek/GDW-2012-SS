package de.hochschuletrier.gdw.ss12.game.datagrams;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import de.hochschuletrier.gdw.commons.netcode.core.NetDatagram;
import de.hochschuletrier.gdw.commons.netcode.core.NetMessageIn;
import de.hochschuletrier.gdw.commons.netcode.core.NetMessageOut;
import de.hochschuletrier.gdw.commons.netcode.core.NetMessageType;
import de.hochschuletrier.gdw.ss12.game.ComponentMappers;
import de.hochschuletrier.gdw.ss12.game.components.InputComponent;

/**
 * send from client only
 */
public final class PlayerInputDatagram extends NetDatagram {

    private final Vector2 moveDirection = new Vector2();

    public static PlayerInputDatagram create(Entity playerEntity) {
        PlayerInputDatagram datagram = DatagramFactory.create(PlayerInputDatagram.class);
        InputComponent input = ComponentMappers.input.get(playerEntity);
        datagram.moveDirection.set(input.moveDirection);
        return datagram;
    }

    public Vector2 getMoveDirection() {
        return moveDirection;
    }

    @Override
    public void reset() {
        super.reset();

        moveDirection.setZero();
    }

    @Override
    public NetMessageType getMessageType() {
        return NetMessageType.NORMAL;
    }

    @Override
    public void writeToMessage(NetMessageOut message) {
        message.putFloat(moveDirection.x);
        message.putFloat(moveDirection.y);
    }

    public @Override
    void readFromMessage(NetMessageIn message) {
        moveDirection.x = message.getFloat();
        moveDirection.y = message.getFloat();
    }
}
