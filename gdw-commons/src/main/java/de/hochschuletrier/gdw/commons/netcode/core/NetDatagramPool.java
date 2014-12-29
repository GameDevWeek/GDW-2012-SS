package de.hochschuletrier.gdw.commons.netcode.core;

import de.hochschuletrier.gdw.commons.utils.pool.Pool;
import de.hochschuletrier.gdw.commons.utils.pool.ReflectionPool;
import java.util.HashMap;

/**
 * Pool datagrams for better performance and memory efficiency.
 *
 * @author Santo Pfingsten
 */
public class NetDatagramPool {

    private final HashMap<Short, Pool<NetDatagram>> pools = new HashMap<>();
    private final Mapper mapper;

    public NetDatagramPool(Mapper mapper) {
        this.mapper = mapper;
    }

    public synchronized NetDatagram get(short type) {
        Pool<NetDatagram> pool = pools.get(type);
        if (pool == null) {
            Class<? extends NetDatagram> clazz;
            if (type < NetDatagramType.FIRST_CUSTOM.ordinal()) {
                clazz = NetDatagram.class;
            } else {
                clazz = mapper.getClass(type);
            }
            pool = new ReflectionPool(clazz);
            pools.put(type, pool);
        }

        NetDatagram datagram = pool.obtain();
        datagram.init(type);
        return datagram;
    }

    public synchronized void free(NetDatagram datagram) {
        Pool<NetDatagram> pool = pools.get(datagram.getType());
        assert (pool != null);
        pool.free(datagram);
    }

    public static interface Mapper {

        /**
         * Get a datagram class for the specified type
         *
         * @param type the datagram type
         * @return the class for the type
         */
        Class<? extends NetDatagram> getClass(short type);
    }
}
