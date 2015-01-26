package de.hochschuletrier.gdw.ss12;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.assets.loaders.BitmapFontLoader.BitmapFontParameter;
import com.badlogic.gdx.assets.loaders.ParticleEffectLoader.ParticleEffectParameter;
import com.badlogic.gdx.assets.loaders.TextureLoader.TextureParameter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import de.hochschuletrier.gdw.commons.devcon.DevConsole;
import de.hochschuletrier.gdw.commons.gdx.assets.AnimationExtended;
import de.hochschuletrier.gdw.commons.gdx.assets.AssetManagerX;
import de.hochschuletrier.gdw.commons.gdx.assets.loaders.AnimationExtendedLoader;
import de.hochschuletrier.gdw.commons.gdx.assets.loaders.TiledMapLoader;
import de.hochschuletrier.gdw.commons.gdx.devcon.DevConsoleView;
import de.hochschuletrier.gdw.commons.gdx.audio.MusicManager;
import de.hochschuletrier.gdw.commons.gdx.audio.SoundEmitter;
import de.hochschuletrier.gdw.commons.gdx.input.hotkey.HotkeyManager;
import de.hochschuletrier.gdw.commons.gdx.state.BaseGameState;
import de.hochschuletrier.gdw.commons.gdx.state.StateBasedGame;
import de.hochschuletrier.gdw.commons.gdx.state.transition.SplitHorizontalTransition;
import de.hochschuletrier.gdw.commons.gdx.utils.DrawUtil;
import de.hochschuletrier.gdw.commons.gdx.utils.GdxResourceLocator;
import de.hochschuletrier.gdw.commons.gdx.utils.KeyUtil;
import de.hochschuletrier.gdw.commons.jackson.JacksonReader;
import de.hochschuletrier.gdw.commons.resourcelocator.CurrentResourceLocator;
import de.hochschuletrier.gdw.commons.tiled.TiledMap;
import de.hochschuletrier.gdw.commons.utils.ClassUtils;
import de.hochschuletrier.gdw.ss12.game.Game;
import de.hochschuletrier.gdw.ss12.sandbox.SandboxCommand;
import de.hochschuletrier.gdw.ss12.states.GameplayState;
import de.hochschuletrier.gdw.ss12.states.LoadGameState;
import de.hochschuletrier.gdw.ss12.states.MainMenuState;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Santo Pfingsten
 */
public class Main extends StateBasedGame {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    public static final boolean IS_RELEASE = ClassUtils.getClassUrl(Main.class).getProtocol().equals("jar");

    public static final int WINDOW_WIDTH = 1024;
    public static final int WINDOW_HEIGHT = 768;

    private final AssetManagerX assetManager = new AssetManagerX();
    private static Main instance;

    public final DevConsole console = new DevConsole(16);
    private final DevConsoleView consoleView = new DevConsoleView(console);
    private Skin skin;
    public static final InputMultiplexer inputMultiplexer = new InputMultiplexer();
    private HashMap<String, String> maps;

    public Main() {
        super(new BaseGameState());
    }

    public static Main getInstance() {
        if (instance == null) {
            instance = new Main();
        }
        return instance;
    }

    public Skin getSkin() {
        return skin;
    }

    public HashMap<String, String> getMaps() {
        return maps;
    }

    private void setupDummyLoader() {
    }

    private void loadAssetLists() {
        TextureParameter param = new TextureParameter();
        param.minFilter = param.magFilter = Texture.TextureFilter.Linear;

        assetManager.loadAssetList("data/json/images.json", Texture.class, param);
        assetManager.loadAssetListByKey("data/json/maps.json", TiledMap.class, new TiledMapLoader.TiledMapParameter());
        assetManager.loadAssetList("data/json/sounds.json", Sound.class, null);
        assetManager.loadAssetList("data/json/music.json", Music.class, null);
        assetManager.loadAssetListWithParam("data/json/animations.json", AnimationExtended.class,
                AnimationExtendedLoader.AnimationExtendedParameter.class);
        BitmapFontParameter fontParam = new BitmapFontParameter();
        fontParam.flip = true;
        assetManager.loadAssetList("data/json/fonts.json", BitmapFont.class, fontParam);

        ParticleEffectParameter particleParam = new ParticleEffectParameter();
        particleParam.imagesDir = Gdx.files.internal("data/particles");

        assetManager.loadAssetList("data/json/particle_effects.json", ParticleEffect.class, particleParam);

        try {
            maps = JacksonReader.readMap("data/json/maps.json", String.class);
        } catch (Exception e) {
            logger.error("Error loading maplist", e);
        }
    }

    private void setupGdx() {
        KeyUtil.init();
        Gdx.graphics.setContinuousRendering(true);

        Gdx.input.setCatchMenuKey(true);
        Gdx.input.setCatchBackKey(true);
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    @Override
    public void create() {
        CurrentResourceLocator.set(new GdxResourceLocator(Files.FileType.Local));
        DrawUtil.init();
        setupDummyLoader();
        loadAssetLists();
        setupGdx();
        skin = new Skin(Gdx.files.internal("data/skins/sotf.json"));
        consoleView.init(skin);
        addScreenListener(consoleView);
        inputMultiplexer.addProcessor(consoleView.getInputProcessor());
        inputMultiplexer.addProcessor(HotkeyManager.getInputProcessor());

        changeState(new LoadGameState(assetManager, this::onLoadComplete), null, null);

        SoundEmitter.setGlobalVolume(Settings.SOUND_VOLUME.get());
        SoundEmitter.setMuted(Settings.SOUND_MUTE.get());
        MusicManager.setGlobalVolume(Settings.MUSIC_VOLUME.get());
        MusicManager.setMuted(Settings.MUSIC_MUTE.get());
    }

    private void onLoadComplete() {
        final MainMenuState mainMenuState = new MainMenuState(assetManager);
        addPersistentState(mainMenuState);
        changeState(mainMenuState, null, null);

        SandboxCommand.init(assetManager);
    }

    public void disconnect() {
        //fixme: disconnect netgame
        changeState(getPersistentState(MainMenuState.class), null, null);
    }

    @Override
    public void dispose() {
        super.dispose();
        DrawUtil.batch.dispose();
        consoleView.dispose();
        skin.dispose();
    }

    protected void preRender() {
        DrawUtil.clearColor(Color.BLACK);
        DrawUtil.clear();
        DrawUtil.resetColor();

        DrawUtil.batch.begin();
    }

    protected void postRender() {
        DrawUtil.batch.end();
        if (consoleView.isVisible()) {
            consoleView.render();
        }
    }

    @Override
    protected void preUpdate(float delta) {
        if (consoleView.isVisible()) {
            consoleView.update(delta);
        }
        console.executeCmdQueue();

        preRender();
    }

    @Override
    protected void postUpdate(float delta) {
        MusicManager.update(delta);
        postRender();
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    public static void main(String[] args) {
        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.title = "Survival Of The Fattest";
        cfg.width = WINDOW_WIDTH;
        cfg.height = WINDOW_HEIGHT;
        cfg.useGL30 = false;
        cfg.vSyncEnabled = true;
        cfg.foregroundFPS = 60;
        cfg.backgroundFPS = 60;

        new LwjglApplication(getInstance(), cfg);
    }

    public AssetManagerX getAssetManager() {
        return assetManager;
    }

    private boolean beforeConnect() {
        if (isTransitioning()) {
            return false;
        }
//        if(SotfGame.isClient() || SotfGame.isServer()) {
//            errorLabel.visible(true);
//            errorLabel.text("Error: Already connected");
//            return false;
//        }
        return true;
    }

    public void startSingleplayer() {
        if (beforeConnect()) {
            Game game = new Game(assetManager);
            game.loadMap(Settings.MAP_FILE.get());
            game.setLocalPlayer(game.acquireBotPlayer(), Settings.PLAYER_NAME.get());
            GameplayState gameplayState = new GameplayState(assetManager, game);
            changeState(gameplayState, new SplitHorizontalTransition(500), null);
        }
    }

    public void createServer(String ip, int port) {
        if (beforeConnect()) {
            //Fixme: create netServer, init it and pass to game
            Game game = new Game(assetManager);
            game.loadMap(Settings.MAP_FILE.get());
            game.setLocalPlayer(game.acquireBotPlayer(), Settings.PLAYER_NAME.get());
            GameplayState gameplayState = new GameplayState(assetManager, game);
            changeState(gameplayState, new SplitHorizontalTransition(500), null);
        }
    }

    public void joinServer(String ip, int port) {
        if (beforeConnect()) {

        }
    }
}
