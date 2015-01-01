package de.hochschuletrier.gdw.ss12.game.datagrams;

import de.hochschuletrier.gdw.commons.netcode.core.NetDatagram;
import de.hochschuletrier.gdw.commons.netcode.core.NetDatagramPool;
import de.hochschuletrier.gdw.commons.netcode.core.NetDatagramType;

/**
 *
 * @author Santo Pfingsten
 */
public enum DatagramType {
    CONNECT(ConnectDatagram.class),
    WORLD_SETUP(WorldSetupDatagram.class),
    CREATE_ENTITY(CreateEntityDatagram.class),
//    REMOVE_ENTITY(RemoveEntityDatagram.class),
    PLAYER_STATE(PlayerStateDatagram.class),
    PLAYER_CONTROL(PlayerControlDatagram.class),
    WORLD_STATE(WorldStateDatagram.class),
//    FIRE_EVENT(FireEventDatagram.class),
//    ENTITY_EVENT(EntityEventDatagram.class),
//    USE_ITEM(UseItemDatagram.class),
    WORLD_SOUND(WorldSoundDatagram.class),
//    UNPAUSE(UnpauseDatagram.class),
    PLAYER_SOUND(PlayerSoundDatagram.class);

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
