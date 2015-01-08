package de.hochschuletrier.gdw.ss12.game.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;
import de.hochschuletrier.gdw.commons.gdx.physix.components.PhysixBodyComponent;
import de.hochschuletrier.gdw.commons.gdx.physix.systems.PhysixSystem;
import de.hochschuletrier.gdw.ss12.game.ComponentMappers;
import de.hochschuletrier.gdw.ss12.game.Constants;
import de.hochschuletrier.gdw.ss12.game.components.data.IgnoreEatable;
import de.hochschuletrier.gdw.ss12.game.components.BotComponent;
import de.hochschuletrier.gdw.ss12.game.components.EatableComponent;
import de.hochschuletrier.gdw.ss12.game.components.InputComponent;
import de.hochschuletrier.gdw.ss12.game.components.PlayerComponent;
import java.util.Iterator;
import java.util.Map;

public class BotSystem extends EntitySystem implements EntityListener {

    private final static float UPDATE_TIME_HAS_TARGET = 0.5f;
    private final static int UPDATE_TIME_NO_TARGET = 2;

    private ImmutableArray<Entity> bots, players, eatables;

    protected final Vector2 position = new Vector2();

    private final BotProcessor botProcessor = new BotProcessor();
    private Engine engine;
    private World world;

    public BotSystem() {
        super(0);
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        bots = engine.getEntitiesFor(Family.all(BotComponent.class).get());
        players = engine.getEntitiesFor(Family.all(PlayerComponent.class).get());
        eatables = engine.getEntitiesFor(Family.all(EatableComponent.class).get());
        engine.addEntityListener(this);
        this.engine = engine;
    }

    @Override
    public void removedFromEngine(Engine engine) {
        super.removedFromEngine(engine);
        bots = players = eatables = null;
        engine.removeEntityListener(this);
        this.engine = null;
    }

    @Override
    public void update(float deltaTime) {
        world = engine.getSystem(PhysixSystem.class).getWorld();
        for (Entity bot : bots) {
            botProcessor.process(bot, deltaTime);
        }
    }

    @Override
    public void entityAdded(Entity entity) {
    }

    @Override
    public void entityRemoved(Entity entity) {
        if (ComponentMappers.eatable.has(entity)) {
            for (Entity bot : bots) {
                processBotEatableRemoved(bot, entity);
            }
        }
    }

    public void processBotEatableRemoved(Entity entity, Entity eatable) {
        BotComponent bot = ComponentMappers.bot.get(entity);
        if (bot.followEatable == eatable) {
            bot.followEatable = null;
            bot.nextBotUpdate = 0;
        }
        bot.ignoreEatables.remove(eatable);
    }

    private class BotProcessor {

        private Entity entity;
        private BotComponent bot;
        private PlayerComponent player;
        private PhysixBodyComponent physix;
        private InputComponent input;
        private boolean clearSight;

        public void process(Entity entity, float deltaTime) {
            this.entity = entity;
            bot = ComponentMappers.bot.get(entity);
            player = ComponentMappers.player.get(entity);
            physix = ComponentMappers.physixBody.get(entity);
            input = ComponentMappers.input.get(entity);

            if (player.isDead()) {
                input.moveDirection.setZero();
                return;
            }

            Iterator<Map.Entry<Entity, IgnoreEatable>> iter = bot.ignoreEatables.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<Entity, IgnoreEatable> entry = iter.next();
                IgnoreEatable value = entry.getValue();
                value.delay -= deltaTime;
                if (value.delay < 0) {
                    iter.remove();
                }
            }

            boolean isStuck = false; //fixme cr.isStuckX() || cr.isStuckY();
            physix.getPosition(position);
            float moveSpeedPct = 100 * physix.getLinearVelocity().len() / input.speed;

            if (!isStuck && moveSpeedPct < 5) {
                bot.nextBotUpdate = 0;
                if (bot.followEatable != null) {
                    bot.ignoreEatables.put(bot.followEatable, new IgnoreEatable(bot.followEatable, 6));
                    bot.followEatable = null;
                }
            }

            Vector2 dir = position.cpy().sub(bot.oldPos);
            bot.oldPos.set(position);
            bot.lastDelta = deltaTime;

            bot.nextBotUpdate -= deltaTime;
            if (!findTarget() && bot.nextBotUpdate < 0) {
                input.moveDirection.set((float) (Math.random() - 0.5) * 2, (float) (Math.random() - 0.5) * 2).nor();
                bot.nextBotUpdate = UPDATE_TIME_NO_TARGET;
            } else if (moveSpeedPct < 90) {
                input.moveDirection.set(dir).nor();
            }
        }

        boolean continueToFollow() {
            if (bot.followEatable == null) {
                return false;
            }

            if (!entityOfInterest(bot.followEatable)) {
                bot.followEatable = null;
                bot.nextBotUpdate = 0;
                return false;
            }

            return true;
        }

        boolean findTarget() {
            Entity lastTarget = bot.followEatable;
            if (!continueToFollow()) {
                Entity e = findEatable(players);
                if (e == null) {
                    e = findEatable(eatables);
                    if (e == null) {
                        return false;
                    }
                }

                bot.followEatable = e;
            }

            if (lastTarget != bot.followEatable || bot.nextBotUpdate <= 0) {
                PhysixBodyComponent eatablePhysix = ComponentMappers.physixBody.get(bot.followEatable);
                eatablePhysix.getPosition(input.moveDirection).sub(bot.oldPos).nor();
                bot.nextBotUpdate = UPDATE_TIME_HAS_TARGET;
            }
            return true;
        }

        boolean entityOfInterest(Entity other) {
            if (other.getId() == entity.getId()) {
                return false;
            }
            if (bot.ignoreEatables.containsKey(other)) {
                return false;
            }

            PlayerComponent otherPlayer = ComponentMappers.player.get(other);
            if (otherPlayer != null && !player.canEat(otherPlayer)) {
                return false;
            }

            PhysixBodyComponent otherPhysix = ComponentMappers.physixBody.get(other);
            float distance = otherPhysix.getPosition().dst(bot.oldPos);
            if (distance > Constants.BOT_DETECTION_RADIUS) {
                return false;
            }

            // Make sure the bot can go directly to the entity to avoid hunting against a wall
            clearSight = true;
            world.rayCast((Fixture fixture, Vector2 point, Vector2 normal, float fraction) -> {
                // Quickest way to test for non items and players
                if (fixture.getType() != Shape.Type.Circle) {
                    clearSight = false;
                    return 0;
                }
                return -1;
            }, physix.getBody().getPosition(), otherPhysix.getBody().getPosition());
            return clearSight;
        }

        Entity findEatable(ImmutableArray<Entity> entities) {
            float closestDistance = 0;
            Entity closestEntity = null;

            for (Entity e : entities) {
                if (!entityOfInterest(e)) {
                    continue;
                }
                PhysixBodyComponent otherPhysix = ComponentMappers.physixBody.get(e);
                float distance = otherPhysix.getPosition().dst(bot.oldPos);
                if (closestEntity == null || closestDistance > distance) {
                    closestDistance = distance;
                    closestEntity = e;
                }
            }
            return closestEntity;
        }
    }
}
