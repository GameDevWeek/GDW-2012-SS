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
            CreateEntityDatagram.class,
            RemoveEntityDatagram.class,
            EntitySoundDatagram.class,
            PlayerInputDatagram.class,
            PlayerUpdatesDatagram.class,
            PlayerNameDatagram.class,
            DropItemDatagram.class,
            NoticeDatagram.class,
            TeamStateDatagram.class,
            WorldSetupDatagram.class,
            WorldSoundDatagram.class
    );

    static <T extends NetDatagram> T create(Class<T> clazz) {
        return POOL.obtain(clazz);
    }
}
