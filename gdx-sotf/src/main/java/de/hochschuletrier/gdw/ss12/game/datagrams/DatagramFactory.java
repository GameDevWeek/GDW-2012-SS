package de.hochschuletrier.gdw.ss12.game.datagrams;

import de.hochschuletrier.gdw.commons.netcode.core.NetDatagram;
import de.hochschuletrier.gdw.commons.netcode.core.NetDatagramPool;

/**
 *
 * @author Santo Pfingsten
 */
public final class DatagramFactory {

    public static final NetDatagramPool POOL = new NetDatagramPool(
            ConnectDatagram.class,
            WorldSetupDatagram.class,
            CreateEntityDatagram.class,
//            RemoveEntityDatagram.class,
            PlayerStateDatagram.class,
            PlayerInputDatagram.class,
            WorldStateDatagram.class,
//            FireEventDatagram.class,
//            EntityEventDatagram.class,
//            UseItemDatagram.class,
            WorldSoundDatagram.class,
//            UnpauseDatagram.class,
            EntitySoundDatagram.class
    );

    static <T extends NetDatagram> T create(Class<T> clazz) {
        return POOL.obtain(clazz);
    }
}
