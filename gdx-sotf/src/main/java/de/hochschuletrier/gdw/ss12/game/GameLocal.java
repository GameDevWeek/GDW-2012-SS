package de.hochschuletrier.gdw.ss12.game;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Input;
import de.hochschuletrier.gdw.commons.devcon.cvar.CVarBool;
import de.hochschuletrier.gdw.commons.gdx.assets.AssetManagerX;
import de.hochschuletrier.gdw.commons.gdx.input.hotkey.Hotkey;
import de.hochschuletrier.gdw.commons.gdx.physix.PhysixComponentAwareContactListener;
import de.hochschuletrier.gdw.commons.gdx.physix.components.PhysixBodyComponent;
import de.hochschuletrier.gdw.commons.gdx.physix.components.PhysixModifierComponent;
import de.hochschuletrier.gdw.commons.gdx.physix.systems.PhysixSystem;
import de.hochschuletrier.gdw.ss12.Main;
import de.hochschuletrier.gdw.ss12.game.components.BotComponent;
import de.hochschuletrier.gdw.ss12.game.components.DropableComponent;
import de.hochschuletrier.gdw.ss12.game.components.InputComponent;
import de.hochschuletrier.gdw.ss12.game.components.PlayerComponent;
import de.hochschuletrier.gdw.ss12.game.components.TriggerComponent;
import de.hochschuletrier.gdw.ss12.game.contactlisteners.PlayerContactListener;
import de.hochschuletrier.gdw.ss12.game.contactlisteners.TriggerContactListener;
import de.hochschuletrier.gdw.ss12.game.data.NoticeType;
import de.hochschuletrier.gdw.ss12.game.data.PlayerState;
import de.hochschuletrier.gdw.ss12.game.data.Team;
import de.hochschuletrier.gdw.ss12.game.systems.BotSystem;
import de.hochschuletrier.gdw.ss12.game.systems.GameStateSystem;
import de.hochschuletrier.gdw.ss12.game.systems.PowerupSystem;
import de.hochschuletrier.gdw.ss12.game.systems.RemoveAnimatedItemSystem;
import de.hochschuletrier.gdw.ss12.game.systems.SpawnRandomEatableSystem;
import de.hochschuletrier.gdw.ss12.game.systems.UpdateLightSystem;
import de.hochschuletrier.gdw.ss12.game.systems.UpdatePlayerSystem;
import de.hochschuletrier.gdw.ss12.game.systems.input.InputSystem;
import java.util.Collections;
import java.util.LinkedList;

public class GameLocal extends Game {

    private final ImmutableArray<Entity> playerEntities = engine.getEntitiesFor(Family.all(PlayerComponent.class).get());
    private final ImmutableArray<Entity> entitiesToRemove = engine.getEntitiesFor(Family.exclude(PlayerComponent.class, TriggerComponent.class).get());
    private final CVarBool botsEnabled = new CVarBool("bots_enable", true, 0, "Enable bots");
    private final Hotkey toggleBotsEnabled = new Hotkey(this::toggleBotsEnabled, Input.Keys.F2);
    private final Hotkey resetGame = new Hotkey(this::reset, Input.Keys.F5);

    private final ImmutableArray<Entity> botPlayers = engine.getEntitiesFor(Family.all(PlayerComponent.class, BotComponent.class).get());

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
    }

    public void init(String mapName, String playerName) {
        super.init();

        Collections.addAll(freeBotNames, botNamesOrdered);
        Collections.shuffle(freeBotNames);
        
        Main.getInstance().console.register(botsEnabled);
        botsEnabled.addListener((CVar) -> engine.getSystem(BotSystem.class).setProcessing(botsEnabled.get()));

        addContactListeners();

        // If this is a build jar file, disable hotkeys
        if (!Main.IS_RELEASE) {
            toggleBotsEnabled.register();
            resetGame.register();
        }

        engine.getSystem(InputSystem.class).setProcessing(false);
        loadMap(mapName);
        setLocalPlayer(acquireBotPlayer());
        setPlayerName(localPlayer, playerName);
    }

    @Override
    public void dispose() {
        super.dispose();

        Main.getInstance().console.unregister(botsEnabled);
        toggleBotsEnabled.unregister();
        resetGame.unregister();
    }

    public void reset() {
        for (Entity entity : entitiesToRemove) {
            engine.removeEntity(entity);
        }
        for (Entity entity : playerEntities) {
            PlayerComponent player = ComponentMappers.player.get(entity);
            engine.getSystem(PowerupSystem.class).removePlayerPowerups(entity, player);
            player.radius = Constants.PLAYER_DEFAULT_SIZE;
            player.state = PlayerState.ALIVE;
            player.lastTeleport = 0;
            player.killer = null;
            ComponentMappers.light.get(entity).radius = player.radius;
            ComponentMappers.position.get(entity).ignorePhysix = false;
            entity.remove(DropableComponent.class);

            PhysixModifierComponent modifyComponent = engine.createComponent(PhysixModifierComponent.class);
            entity.add(modifyComponent);
            modifyComponent.schedule(() -> {
                PhysixBodyComponent bodyComponent = ComponentMappers.physixBody.get(entity);
                bodyComponent.setPosition(player.startPosition);
                bodyComponent.setLinearVelocity(0, 0);
                bodyComponent.setActive(true);
            });
        }
        startCountdown();
    }

    @Override
    public void startCountdown() {
        super.startCountdown();
        sendStartNotices();
    }

    @Override
    protected void addSystems() {
        super.addSystems();

        // Remember to set priorities in CustomPooledEngine when creating new system classes
        engine.addSystem(new BotSystem());
        engine.addSystem(new PowerupSystem());
        engine.addSystem(new SpawnRandomEatableSystem());
        engine.addSystem(new UpdatePlayerSystem());
        engine.addSystem(new UpdateLightSystem());
        engine.addSystem(new RemoveAnimatedItemSystem());
        engine.addSystem(new GameStateSystem());
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
        scheduleNoticeForAll(NoticeType.THREE, 0, -1);
        scheduleNoticeForAll(NoticeType.TWO, 1, -1);
        scheduleNoticeForAll(NoticeType.ONE, 2, -1);
        scheduleNoticeForAll(NoticeType.GO, 3, -1);
    }

    @Override
    public void onNoticeStart(NoticeType type) {
        super.onNoticeStart(type);
        switch (type) {
            case GO:
                engine.getSystem(GameStateSystem.class).setProcessing(true);
                break;
            case ROUND_WON:
            case ROUND_LOST:
            case TEAM_WON:
            case TEAM_LOST:
                engine.getSystem(GameStateSystem.class).setProcessing(false);
                break;
        }
    }

    @Override
    public void onNoticeEnd(NoticeType type) {
        switch (type) {
            case ROUND_WON:
            case ROUND_LOST:
            case TEAM_WON:
            case TEAM_LOST:
                reset();
                break;
        }
    }
}
