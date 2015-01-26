package de.hochschuletrier.gdw.ss12.game.datagrams;

import com.badlogic.ashley.core.Entity;
import de.hochschuletrier.gdw.commons.netcode.core.NetDatagram;

/**
 * send from client only
 */
public final class DropItemDatagram extends NetDatagram {

    public static DropItemDatagram create(Entity playerEntity) {
        return DatagramFactory.create(DropItemDatagram.class);
    }
}
