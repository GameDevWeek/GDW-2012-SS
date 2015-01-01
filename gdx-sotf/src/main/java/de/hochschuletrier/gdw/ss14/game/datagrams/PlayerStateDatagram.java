package de.hochschuletrier.gdw.ss14.game.datagrams;

import com.badlogic.gdx.math.Vector2;

import de.hochschuletrier.gdw.commons.netcode.core.NetDatagram;
import de.hochschuletrier.gdw.commons.netcode.core.NetMessageIn;
import de.hochschuletrier.gdw.commons.netcode.core.NetMessageOut;
import de.hochschuletrier.gdw.commons.netcode.core.NetMessageType;

public final class PlayerStateDatagram extends NetDatagram {

    private final Vector2 position = new Vector2();
    private float moveSpeed;
    private float viewingAngle;
    private float radius;
    private int renderEffectState;
//    private byte teamID; //why would this have to be send every frame ?
    private byte animState;
    private long id;

//    public void setup(IPlayer player) {
//        id = playerId; // fixme: netid ? and: write/read
//
//        if (player.isDead()) {
//            position.set(player.getDeathPosition());
//        } else {
//            position.set(player.getPosition());
//        }
//        moveSpeed = player.getMoveSpeed();
//        viewingAngle = player.getViewingAngle();
//        radius = player.getRadius();
//        teamID = (byte) player.getTeam().getID();
//        animState = (byte) player.getAnimState();
//
//        renderEffectState = 0;
//        for (IPlayerRenderEffect effect : player.getRenderEffects().values()) {
//            if (effect.isActive()) {
//                renderEffectState |= 1 << effect.getBit();
//            }
//        }
//    }

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

    public int getRenderEffectState() {
        return renderEffectState;
    }

//    public byte getTeamID() {
//        return teamID;
//    }

    public byte getAnimState() {
        return animState;
    }

    @Override
    public NetMessageType getMessageType() {
        return NetMessageType.NORMAL;
    }

    @Override
    public void writeToMessage(NetMessageOut message) {
        message.putFloat(position.x);
        message.putFloat(position.y);
        message.putFloat(moveSpeed);
        message.putFloat(radius);
        message.putFloat(viewingAngle);
        message.putInt(renderEffectState);
//        message.put(teamID);
        message.put(animState);
    }

    public @Override
    void readFromMessage(NetMessageIn message) {
        position.x = message.getFloat();
        position.y = message.getFloat();
        moveSpeed = message.getFloat();
        radius = message.getFloat();
        viewingAngle = message.getFloat();
        renderEffectState = message.getInt();
//        teamID = message.get();
        animState = message.get();
    }
}