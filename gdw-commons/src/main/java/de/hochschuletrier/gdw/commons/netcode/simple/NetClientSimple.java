package de.hochschuletrier.gdw.commons.netcode.simple;

import de.hochschuletrier.gdw.commons.netcode.core.NetConnection;
import de.hochschuletrier.gdw.commons.netcode.core.NetDatagram;
import de.hochschuletrier.gdw.commons.netcode.core.NetManagerClient;
import de.hochschuletrier.gdw.commons.netcode.core.NetDatagramPool;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A helper class to help avoid writing the same handler code for every game.
 *
 * @author Santo Pfingsten
 */
public class NetClientSimple {

    private static final Logger logger = LoggerFactory.getLogger(NetClientSimple.class);

    protected NetManagerClient manager;
    protected NetConnection connection;
    protected final NetDatagramDistributor distributor;
    protected final NetDatagramPool datagramPool;

    public NetClientSimple(NetDatagramHandler handler, NetDatagramPool datagramPool) {
        distributor = new NetDatagramDistributor(handler);
        this.datagramPool = datagramPool;
    }

    public boolean isRunning() {
        return connection != null && connection.isConnected();
    }

    public void update() {
        if (isRunning()) {
            while (connection.hasIncoming()) {
                NetDatagram datagram = connection.receive();
                if (datagram != null) {
                    try {
                        if (!distributor.handle(datagram)) {
                            connection.disconnect();
                        }
                    } catch (InvocationTargetException e) {
                        logger.error("Error calling handle() for datagram", e);
                    } finally {
                        datagramPool.free(datagram);
                    }
                }
            }
        }
    }

    public boolean connect(String ip, int port) {
        try {
            manager = new NetManagerClient(ip, port, datagramPool);
            NetThreadSimple thread = new NetThreadSimple(manager, 500);
            thread.start();
            connection = manager.getConnection();
            return true;
        } catch (IOException e) {
            connection = null;
            logger.error("Error creating a NetConnection", e);
        }
        return false;
    }

    public void disconnect() {
        if (connection != null) {
            connection.disconnect();
        }
    }

    public NetConnection getConnection() {
        return connection;
    }
}
