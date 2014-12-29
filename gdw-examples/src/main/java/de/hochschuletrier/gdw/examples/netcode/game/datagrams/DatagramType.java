package de.hochschuletrier.gdw.examples.netcode.game.datagrams;

import de.hochschuletrier.gdw.commons.netcode.core.NetDatagram;
import de.hochschuletrier.gdw.commons.netcode.core.NetDatagramPool;
import de.hochschuletrier.gdw.commons.netcode.core.NetDatagramType;

public enum DatagramType {

    PLAYER(PlayerDatagram.class),
    MOVE_INTENT(MoveIntentDatagram.class),
    DESTROY_ENTITY(DestroyEntityDatagram.class);

    // Everything below this line is the same for every game.
    private final Class<? extends NetDatagram> clazz;

    DatagramType(Class<? extends NetDatagram> clazz) {
        this.clazz = clazz;
    }

    public short toID() {
        return (short) (NetDatagramType.FIRST_CUSTOM.toID() + ordinal());
    }

    public static DatagramType fromID(short id) {
        return values()[id - NetDatagramType.FIRST_CUSTOM.toID()];
    }
    
    
    public NetDatagram create() {
        return POOL.get(toID());
    }

    public static final NetDatagramPool.Mapper MAPPER = (short type) -> fromID(type).clazz;
    public static NetDatagramPool POOL;

    public static void setPool(NetDatagramPool pool) {
        POOL = pool;
    }
}
