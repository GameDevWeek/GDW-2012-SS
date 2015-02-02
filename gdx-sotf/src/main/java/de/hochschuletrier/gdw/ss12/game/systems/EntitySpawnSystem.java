package de.hochschuletrier.gdw.ss12.game.systems;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Array;
import de.hochschuletrier.gdw.commons.gdx.assets.AssetManagerX;
import de.hochschuletrier.gdw.commons.gdx.physix.PhysixBodyDef;
import de.hochschuletrier.gdw.commons.gdx.physix.PhysixFixtureDef;
import de.hochschuletrier.gdw.commons.gdx.physix.components.PhysixBodyComponent;
import de.hochschuletrier.gdw.commons.gdx.physix.components.PhysixModifierComponent;
import de.hochschuletrier.gdw.commons.gdx.physix.systems.PhysixSystem;
import de.hochschuletrier.gdw.commons.jackson.JacksonReader;
import de.hochschuletrier.gdw.commons.tiled.Layer;
import de.hochschuletrier.gdw.commons.tiled.LayerObject;
import de.hochschuletrier.gdw.commons.tiled.TiledMap;
import de.hochschuletrier.gdw.ss12.game.ComponentMappers;
import de.hochschuletrier.gdw.ss12.game.Constants;
import de.hochschuletrier.gdw.ss12.game.Game;
import de.hochschuletrier.gdw.ss12.game.GameLocal;
import de.hochschuletrier.gdw.ss12.game.GameServer;
import de.hochschuletrier.gdw.ss12.game.data.PlayerState;
import de.hochschuletrier.gdw.ss12.game.data.Team;
import de.hochschuletrier.gdw.ss12.game.components.BotComponent;
import de.hochschuletrier.gdw.ss12.game.components.EatableComponent;
import de.hochschuletrier.gdw.ss12.game.components.InputComponent;
import de.hochschuletrier.gdw.ss12.game.components.ItemTrapComponent;
import de.hochschuletrier.gdw.ss12.game.components.LightComponent;
import de.hochschuletrier.gdw.ss12.game.components.PizzaSliceComponent;
import de.hochschuletrier.gdw.ss12.game.components.PlayerComponent;
import de.hochschuletrier.gdw.ss12.game.components.PositionComponent;
import de.hochschuletrier.gdw.ss12.game.components.RenderTextureComponent;
import de.hochschuletrier.gdw.ss12.game.components.DropableComponent;
import de.hochschuletrier.gdw.ss12.game.components.ParticleEffectComponent;
import de.hochschuletrier.gdw.ss12.game.components.RenderAnimationComponent;
import de.hochschuletrier.gdw.ss12.game.components.SoundEmitterComponent;
import de.hochschuletrier.gdw.ss12.game.components.SetupComponent;
import de.hochschuletrier.gdw.ss12.game.interfaces.SystemGameInitializer;
import de.hochschuletrier.gdw.ss12.game.interfaces.SystemMapInitializer;
import de.hochschuletrier.gdw.ss12.game.data.EntityJson;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntitySpawnSystem extends EntitySystem implements SystemGameInitializer, SystemMapInitializer {

    private static final Logger logger = LoggerFactory.getLogger(EntitySystem.class);

    private PooledEngine engine;
    private PhysixSystem physixSystem;
    private PowerupSystem powerupSystem;
    private HashMap<String, EntityJson> entityJsonMap;
    private AssetManagerX assetManager;
    private Game game;

    @Override
    public void initGame(Game game, AssetManagerX assetManager, PooledEngine engine) {
        this.game = game;
        this.assetManager = assetManager;
        this.engine = engine;

        physixSystem = engine.getSystem(PhysixSystem.class);
        if (game instanceof GameLocal) {
            powerupSystem = engine.getSystem(PowerupSystem.class);
        }

        try {
            entityJsonMap = JacksonReader.readMap("data/json/entities.json", EntityJson.class);
        } catch (Exception e) {
            logger.error("Error reading entities.json", e);
        }
    }

    @Override
    public void initMap(TiledMap map, Array<Team> teams) {
        if (game instanceof GameLocal) {
            GameLocal gameLocal = (GameLocal) game;
            int highestTeamID = -1;
            for (Layer layer : map.getLayers()) {
                if (layer.isObjectLayer()) {
                    for (LayerObject obj : layer.getObjects()) {
                        int id = obj.getIntProperty("team", -1);
                        if (id >= 0) {
                            if (id > highestTeamID) {
                                highestTeamID = id;
                            }
                            if (id < 0 || id >= teams.size) {
                                throw new RuntimeException("Map contains bad Team Id: " + id);
                            }

                            createPlayer(obj.getX() + obj.getWidth() / 2, obj.getY() + obj.getHeight() / 2, teams.get(id), gameLocal.acquireBotName());
                        }
                    }
                }
            }
        }
    }

    public Entity createPlayer(float x, float y, Team team, String name) {
        team.numPlayers++;

        Entity entity = engine.createEntity();
        PositionComponent position = engine.createComponent(PositionComponent.class);
        position.x = x;
        position.y = y;
        entity.add(position);

        PlayerComponent player = engine.createComponent(PlayerComponent.class);
        player.radius = Constants.PLAYER_DEFAULT_SIZE;
        player.state = PlayerState.ALIVE;
        player.team = team;
        player.name = name;
        player.startPosition.set(x, y);
        entity.add(player);

        ParticleEffectComponent particleEffect = engine.createComponent(ParticleEffectComponent.class);
        particleEffect.effect = new ParticleEffect(assetManager.getParticleEffect("player"));
        particleEffect.effect.start();
        for (ParticleEmitter emitter : particleEffect.effect.getEmitters()) {
            emitter.setContinuous(false);
            emitter.duration = 0;
            emitter.durationTimer = 0;
        }
        particleEffect.effect.update(20); // bugfix for single particle playing at the start
        entity.add(particleEffect);

        entity.add(engine.createComponent(SoundEmitterComponent.class));
        entity.add(engine.createComponent(BotComponent.class));

        InputComponent input = engine.createComponent(InputComponent.class);
        input.speed = Constants.PLAYER_MOVEMENT_SPEED;
        entity.add(input);

        LightComponent light = engine.createComponent(LightComponent.class);
        light.team = team;
        light.radius = Constants.PLAYER_DEFAULT_SIGHTDISTANCE;
        entity.add(light);

        PhysixModifierComponent modifyComponent = engine.createComponent(PhysixModifierComponent.class);
        entity.add(modifyComponent);

        modifyComponent.schedule(() -> {
            PhysixBodyComponent bodyComponent = engine.createComponent(PhysixBodyComponent.class);
            PhysixBodyDef bodyDef = new PhysixBodyDef(BodyDef.BodyType.DynamicBody, physixSystem)
                    .position(player.startPosition).fixedRotation(true);//.linearDamping(20);
            bodyComponent.init(bodyDef, physixSystem, entity);
            PhysixFixtureDef fixtureDef = new PhysixFixtureDef(physixSystem).groupIndex((short) -1)
                    .density(5).friction(0).shapeCircle(player.radius);
            Fixture fixture = bodyComponent.createFixture(fixtureDef);
            fixture.setUserData("body");
            PhysixFixtureDef fixtureDef2 = new PhysixFixtureDef(physixSystem)
                    .sensor(true).shapeCircle(player.radius);
            Fixture fixture2 = bodyComponent.createFixture(fixtureDef2);
            fixture2.setUserData("sensor");
            entity.add(bodyComponent);
        });
        engine.addEntity(entity);

        return entity;
    }

    public Entity createStaticEntity(String name, float x, float y, float radius, Team team) {
        Entity entity = engine.createEntity();
        PositionComponent position = engine.createComponent(PositionComponent.class);
        entity.add(position);
        position.x = x;
        position.y = y;

        EntityJson entityJson = entityJsonMap.get(name);
        assert (entityJson != null);
        for (Map.Entry<String, Map<String, String>> entry : entityJson.components.entrySet()) {
            Map<String, String> config = entry.getValue();
            Component component = createComponentFromConfig(entry.getKey(), config, team);
            if (component != null) {
                entity.add(component);
            }
        }

        PhysixModifierComponent modifyComponent = ComponentMappers.physixModifier.get(entity);
        if (modifyComponent != null) {
            modifyComponent.schedule(() -> {
                PhysixBodyComponent bodyComponent = engine.createComponent(PhysixBodyComponent.class);
                PhysixBodyDef bodyDef = new PhysixBodyDef(BodyDef.BodyType.DynamicBody, physixSystem)
                        .position(x, y).awake(false);
                bodyComponent.init(bodyDef, physixSystem, entity);
                PhysixFixtureDef fixtureDef = new PhysixFixtureDef(physixSystem).sensor(true).shapeCircle(radius);
                bodyComponent.createFixture(fixtureDef);
                entity.add(bodyComponent);
            });
        }

        if (game instanceof GameServer) {
            SetupComponent setup = engine.createComponent(SetupComponent.class);
            setup.team = team;
            setup.name = name;
            entity.add(setup);
        }

        engine.addEntity(entity);
        return entity;
    }

    private Component createComponentFromConfig(String type, Map<String, String> config, Team team) {
        try {
            // Some types don't need to be created on the clientside
            if (game instanceof GameLocal) {
                switch (type) {
                    case "EatableComponent": {
                        EatableComponent component = engine.createComponent(EatableComponent.class);
                        component.energy = Float.parseFloat(config.get("energy"));
                        component.sound = config.get("sound");
                        String powerupName = config.get("powerup");
                        if (powerupName != null) {
                            component.powerup = powerupSystem.createPowerup(powerupName);
                        }
                        return component;
                    }
                    case "ItemTrapComponent": {
                        ItemTrapComponent component = engine.createComponent(ItemTrapComponent.class);
                        component.team = team;
                        component.sound = config.get("sound");
                        String powerupName = config.get("powerup");
                        if (powerupName != null) {
                            component.powerup = powerupSystem.createPowerup(powerupName);
                        }
                        return component;
                    }
                    case "PizzaSliceComponent":
                        return engine.createComponent(PizzaSliceComponent.class);
                    case "DropableComponent": {
                        DropableComponent component = engine.createComponent(DropableComponent.class);
                        component.item = config.get("item");
                        component.sound = config.get("sound");
                        component.texture = assetManager.getTexture(config.get("texture"));
                        return component;
                    }
                    case "PhysixBodyComponent":
                        return engine.createComponent(PhysixModifierComponent.class);
                }
            }

            switch (type) {
                case "RenderTextureComponent": {
                    RenderTextureComponent component = engine.createComponent(RenderTextureComponent.class);
                    component.texture = assetManager.getTexture(config.get("image"));
                    assert (component.texture != null);
                    return component;
                }
                case "RenderAnimationComponent": {
                    RenderAnimationComponent component = engine.createComponent(RenderAnimationComponent.class);
                    component.animation = assetManager.getAnimation(config.get("animation"));
                    assert (component.animation != null);
                    return component;
                }
                case "LightComponent": {
                    LightComponent component = engine.createComponent(LightComponent.class);
                    component.team = team;
                    component.radius = Float.parseFloat(config.get("radius"));
                    component.shrinkPixelPerSecond = Float.parseFloat(config.get("shrinkPixelPerSecond"));
                    return component;
                }
                case "EatableComponent":
                case "ItemTrapComponent":
                case "PizzaSliceComponent":
                case "DropableComponent":
                case "PhysixBodyComponent":
                    return null;
                default:
                    logger.error("Uknown component {}", type);
            }
        } catch (NumberFormatException e) {
            logger.error("Error creating component with config", e);
        }
        return null;
    }
}
