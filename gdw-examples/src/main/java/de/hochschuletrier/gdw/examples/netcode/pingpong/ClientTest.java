package de.hochschuletrier.gdw.examples.netcode.pingpong;

import de.hochschuletrier.gdw.commons.netcode.core.NetConnection;
import de.hochschuletrier.gdw.commons.netcode.simple.NetDatagramHandler;
import de.hochschuletrier.gdw.commons.netcode.simple.NetClientSimple;
import de.hochschuletrier.gdw.commons.netcode.core.NetDatagramPool;
import de.hochschuletrier.gdw.commons.utils.QuietUtils;
import de.hochschuletrier.gdw.examples.netcode.pingpong.datagrams.ChatDatagram;
import de.hochschuletrier.gdw.examples.netcode.pingpong.datagrams.DatagramType;

/**
 *
 * @author Santo Pfingsten
 */
public class ClientTest implements NetDatagramHandler {

    private final NetDatagramPool datagramPool = new NetDatagramPool(DatagramType.MAPPER);
    private final NetClientSimple netClient = new NetClientSimple(this, datagramPool);
    private NetConnection connection;

    public void sendPing() {
        ChatDatagram ping = (ChatDatagram) DatagramType.CHAT.create();
        ping.setText("Ping");
        ping.setTimestamp(System.currentTimeMillis());
        connection.sendUnreliable(ping);
    }
    
    ClientTest() {
        DatagramType.setPool(datagramPool);
    }

    private void run() {
        if (netClient.connect("localhost", 9090)) {
            connection = netClient.getConnection();
            System.out.println("Server connection established");
            sendPing();

            while (netClient.isRunning()) {
                netClient.update();
                QuietUtils.sleep(16);
            }

            System.out.println("Server disconnected");
        }
    }

    public void handle(ChatDatagram datagram) {
        long time = System.currentTimeMillis() - datagram.getTimestamp();
        System.out.printf("%s (%d ms)\n", datagram.getText(), time);
        if (connection.getTcpStatistic().getNumDatagramsSent() < 1000) {
            sendPing();
        } else {
            netClient.disconnect();
        }
    }

    public static void main(String[] args) {
        ClientTest test = new ClientTest();
        test.run();
    }
}
