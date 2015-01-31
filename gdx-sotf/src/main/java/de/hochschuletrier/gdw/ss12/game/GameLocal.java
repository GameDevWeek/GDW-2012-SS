package de.hochschuletrier.gdw.ss12.game;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Input;
import de.hochschuletrier.gdw.commons.devcon.cvar.CVarBool;
import de.hochschuletrier.gdw.commons.gdx.assets.AssetManagerX;
import de.hochschuletrier.gdw.commons.gdx.input.hotkey.Hotkey;
import de.hochschuletrier.gdw.commons.gdx.physix.PhysixComponentAwareContactListener;
import de.hochschuletrier.gdw.commons.gdx.physix.systems.PhysixDebugRenderSystem;
import de.hochschuletrier.gdw.commons.gdx.physix.systems.PhysixSystem;
import de.hochschuletrier.gdw.ss12.Main;
import de.hochschuletrier.gdw.ss12.game.components.BotComponent;
import de.hochschuletrier.gdw.ss12.game.components.InputComponent;
import de.hochschuletrier.gdw.ss12.game.components.PlayerComponent;
import de.hochschuletrier.gdw.ss12.game.components.TriggerComponent;
import de.hochschuletrier.gdw.ss12.game.contactlisteners.PlayerContactListener;
import de.hochschuletrier.gdw.ss12.game.contactlisteners.TriggerContactListener;
import de.hochschuletrier.gdw.ss12.game.data.NoticeType;
import de.hochschuletrier.gdw.ss12.game.data.Team;
import de.hochschuletrier.gdw.ss12.game.systems.BotSystem;
import de.hochschuletrier.gdw.ss12.game.systems.GameStateSystem;
import de.hochschuletrier.gdw.ss12.game.systems.PowerupSystem;
import de.hochschuletrier.gdw.ss12.game.systems.RemoveAnimatedItemSystem;
import de.hochschuletrier.gdw.ss12.game.systems.SpawnRandomEatableSystem;
import de.hochschuletrier.gdw.ss12.game.systems.UpdateLightSystem;
import de.hochschuletrier.gdw.ss12.game.systems.UpdatePlayerSystem;
import de.hochschuletrier.gdw.ss12.game.systems.UpdatePositionSystem;
import java.util.Collections;
import java.util.LinkedList;

public class GameLocal extends Game {

    private final CVarBool physixDebug = new CVarBool("physix_debug", !Main.IS_RELEASE, 0, "Draw physix debug");
    private final CVarBool botsEnabled = new CVarBool("bots_enable", true, 0, "Enable bots");
    private final Hotkey togglePhysixDebug = new Hotkey(() -> physixDebug.toggle(false), Input.Keys.F1);
    private final Hotkey toggleBotsEnabled = new Hotkey(this::toggleBotsEnabled, Input.Keys.F2);
    private final Hotkey resetGame = new Hotkey(this::reset, Input.Keys.F5);

    private final ImmutableArray<Entity> botPlayers = engine.getEntitiesFor(Family.all(PlayerComponent.class, BotComponent.class).get());
    private final ImmutableArray<Entity> nonBotPlayers = engine.getEntitiesFor(Family.all(PlayerComponent.class).exclude(BotComponent.class).get());

    private final LinkedList<String> freeBotNames = new LinkedList();
    private static final String BOT_NAME_PREFIX = "[BOT] ";
    private final String[] botNamesOrdered = {
        "Stan", "Kyle", "Cartman", "Kenny",
        "Butters", "Timmy", "Jimmy", "Token",
        "Wendy", "Bebe", "Nichole", "Stacy",
        "Shelly", "Jessica", "Maria", "Henrietta",
        "Chef", "Garrison", "Prin. Victoria", "Ms. Choksondik",
        "Randy", "Sharon", "Gerald", "Sheila"
    };

    public GameLocal(AssetManagerX assetManager) {
        super(assetManager);

        Collections.addAll(freeBotNames, botNamesOrdered);
        Collections.shuffle(freeBotNames);

        Main.getInstance().console.register(physixDebug);
        physixDebug.addListener((CVar) -> engine.getSystem(PhysixDebugRenderSystem.class).setProcessing(physixDebug.get()));
        Main.getInstance().console.register(botsEnabled);
        botsEnabled.addListener((CVar) -> engine.getSystem(BotSystem.class).setProcessing(botsEnabled.get()));

        addContactListeners();

        // If this is a build jar file, disable hotkeys
        if (!Main.IS_RELEASE) {
            togglePhysixDebug.register();
            toggleBotsEnabled.register();
            resetGame.register();
        }
    }

    @Override
    public void dispose() {
        super.dispose();

        togglePhysixDebug.unregister();
        toggleBotsEnabled.unregister();
        resetGame.unregister();
    }

    @Override
    public void start() {
        super.start();
        sendStartNotices();
    }

    @Override
    protected void addSystems() {
        super.addSystems();

        // Remember to set priorities in CustomPooledEngine when creating new system classes
        engine.addSystem(new BotSystem());
        engine.addSystem(new PhysixSystem(
                Constants.BOX2D_SCALE, Constants.VELOCITY_ITERATIONS, Constants.POSITION_ITERATIONS
        ));
        engine.addSystem(new PowerupSystem());
        engine.addSystem(new UpdatePositionSystem());
        engine.addSystem(new SpawnRandomEatableSystem());
        engine.addSystem(new UpdatePlayerSystem());
        engine.addSystem(new UpdateLightSystem());
        engine.addSystem(new RemoveAnimatedItemSystem());
        engine.addSystem(new GameStateSystem());
        engine.addSystem(new PhysixDebugRenderSystem());
    }

    public void toggleBotsEnabled() {
        for (Entity botPlayer : botPlayers) {
            InputComponent input = ComponentMappers.input.get(botPlayer);
            input.moveDirection.setZero();
            input.dropItem = false;
        }
        botsEnabled.toggle(false);
    }

    protected void onPlayerNameChanged(Entity entity) {
    }

    public String acquireBotName() {
        return BOT_NAME_PREFIX + freeBotNames.pop();
    }

    public void freeBotName(String botName) {
        freeBotNames.add(botName.substring(BOT_NAME_PREFIX.length()));
        Collections.shuffle(freeBotNames);
    }

    public Entity acquireBotPlayer() {
        teams.sort(acquireTeamComparator);
        Team bestTeam = teams.get(0);

        Entity entity = acquireBotPlayer(bestTeam);
        if (entity != null) {
            PlayerComponent player = ComponentMappers.player.get(entity);
            player.team.numConnectedPlayers++;
            freeBotName(player.name);
            player.name = "[Connecting]";
            onPlayerNameChanged(entity);
            entity.remove(BotComponent.class);
        }
        return entity;
    }

    private Entity acquireBotPlayer(Team team) {
        // try alive players first
        for (Entity e : botPlayers) {
            PlayerComponent p = ComponentMappers.player.get(e);
            if (!p.isDead() && p.team == team) {
                return e;
            }
        }

        // now take whatever you can get
        return botPlayers.random();
    }

    public void freeBotPlayer(Entity entity) {
        PlayerComponent player = ComponentMappers.player.get(entity);
        player.team.numConnectedPlayers--;
        player.name = acquireBotName();
        onPlayerNameChanged(entity);
        entity.add(engine.createComponent(BotComponent.class));
    }

    private void addContactListeners() {
        PhysixSystem physixSystem = engine.getSystem(PhysixSystem.class);
        PhysixComponentAwareContactListener contactListener = new PhysixComponentAwareContactListener();
        physixSystem.getWorld().setContactListener(contactListener);
        contactListener.addListener(TriggerComponent.class, new TriggerContactListener());
        contactListener.addListener(PlayerComponent.class, new PlayerContactListener(engine, this));
    }

    public void sendStartNotices() {
        scheduleNoticeForAll(NoticeType.THREE, 0);
        scheduleNoticeForAll(NoticeType.TWO, 1);
        scheduleNoticeForAll(NoticeType.ONE, 2);
        scheduleNoticeForAll(NoticeType.GO, 3);
    }
}
