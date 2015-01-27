package de.hochschuletrier.gdw.ss12.states;

import com.badlogic.gdx.graphics.Color;
import de.hochschuletrier.gdw.ss12.game.datagrams.ConnectDatagram;
import de.hochschuletrier.gdw.ss12.game.datagrams.DatagramFactory;
import de.hochschuletrier.gdw.ss12.game.datagrams.WorldSetupDatagram;
import de.hochschuletrier.gdw.commons.gdx.assets.AssetManagerX;
import de.hochschuletrier.gdw.commons.gdx.state.BaseGameState;
import de.hochschuletrier.gdw.commons.gdx.state.transition.FadeTransition;
import de.hochschuletrier.gdw.commons.netcode.core.NetConnection;
import de.hochschuletrier.gdw.commons.netcode.simple.NetClientSimple;
import de.hochschuletrier.gdw.commons.netcode.simple.NetDatagramHandler;
import de.hochschuletrier.gdw.ss12.Main;
import de.hochschuletrier.gdw.ss12.game.Game;
import java.io.IOException;

/**
 * Connecting to server state
 * 
 * @author Santo Pfingsten
 */
public class ConnectingState extends BaseGameState implements NetDatagramHandler {

    public static enum Status {

        CONNECTING,
        READY,
        DISCONNECTED,
        DONE
    }

    private final Main main;
    private final AssetManagerX assetManager;
    private Status status;
    private Game game;
    private final NetClientSimple netClient = new NetClientSimple(DatagramFactory.POOL);
    private NetConnection serverConnection;

    public ConnectingState(AssetManagerX assetManager, String ip, int port, String playerName) throws IOException {
        this.assetManager = assetManager;
        main = Main.getInstance();
        netClient.setHandler(this);
        if (netClient.connect(ip, port)) {
            serverConnection.sendReliable(ConnectDatagram.create(playerName));
            status = Status.CONNECTING;
        } else {
            //fixme
        }
    }

    public boolean checkForDisconnect() {
        if (!netClient.isRunning()) {
            serverConnection = null;
            status = Status.DISCONNECTED;
            return true;
        }
        return false;
    }

    @Override
    public void update(float delta) {
        netClient.update();
//        switch (status) {
//            case CONNECTING:
//                if(!checkForDisconnect()) {
//                    while (serverConnection.hasIncoming()) {
//                        NetDatagram datagram = serverConnection.receive();
//                        if (datagram != null && datagram.getType() == DatagramType.WORLD_SETUP) {
//                            game = new Game(assetManager); // fixme: set serverConnection
//                            serverConnection = null;
//                            game.handleWorldSetup((WorldSetupDatagram) datagram);
//                            status = Status.READY;
//                            break;
//                        }
//                    }
//                }
//                break;
//            case READY:
//                if(!checkForDisconnect()) {
//                    if (!main.isTransitioning()) {
//                        GameplayState state = new GameplayState(assetManager, game);
//                        game = null;
//                        main.changeState(state, new FadeTransition(Color.BLACK, 500), null);
//                        status = Status.DONE;
//                    }
//                }
//                break;
//            case DISCONNECTED:
//                if (!main.isTransitioning()) {
//                    if (game != null) {
//                        game.dispose();
//                        game = null;
//                    }
//                    BaseGameState state = main.getPersistentState(MainMenuState.class);
//                    main.changeState(state, new FadeTransition(Color.BLACK, 500), null);
//                }
//                break;
//
//        }
        //fixme: draw connection screen
    }

    @Override
    public void dispose() {
        if (game != null) {
            game.dispose();
            game = null;
        }
        netClient.disconnect();
        serverConnection = null;
    }
}
