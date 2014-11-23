package de.hochschuletrier.gdw.ss14.game.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Vector2;
import de.hochschuletrier.gdw.commons.gdx.physix.components.PhysixBodyComponent;
import de.hochschuletrier.gdw.ss14.game.ComponentMappers;
import de.hochschuletrier.gdw.ss14.game.Constants;
import de.hochschuletrier.gdw.ss14.game.componentdata.IgnoreEatable;
import de.hochschuletrier.gdw.ss14.game.components.BotComponent;
import de.hochschuletrier.gdw.ss14.game.components.EatableComponent;
import de.hochschuletrier.gdw.ss14.game.components.InputComponent;
import de.hochschuletrier.gdw.ss14.game.components.PlayerComponent;
import java.util.Iterator;
import java.util.Map;

public class BotSystem extends EntitySystem implements EntityListener {
    private ImmutableArray<Entity> bots, players, eatables;

    protected final Vector2 position = new Vector2();
    
    private final BotProcessor botProcessor = new BotProcessor();

    public BotSystem() {
        super(0);
    }

    public BotSystem(int priority) {
        super(priority);
    }
		
	@Override
	public void addedToEngine(Engine engine) {
		super.addedToEngine(engine);
        bots = engine.getEntitiesFor(Family.all(BotComponent.class).get());
        players = engine.getEntitiesFor(Family.all(PlayerComponent.class).get());
        eatables = engine.getEntitiesFor(Family.all(EatableComponent.class).get());
        engine.addEntityListener(this);
	}

	@Override
	public void removedFromEngine(Engine engine) {
        super.removedFromEngine(engine);
        bots = players = eatables = null;
        engine.removeEntityListener(this);
	}

	@Override
	public void update(float deltaTime) {
        for(Entity bot: bots)
            botProcessor.process(bot, deltaTime);
	}

    @Override
    public void entityAdded(Entity entity) {
    }

    @Override
    public void entityRemoved(Entity entity) {
        if(ComponentMappers.eatable.has(entity)) {
            for(Entity bot: bots)
                processBotEatableRemoved(bot, entity);
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
        
        public void process(Entity entity, float deltaTime) {
            this.entity = entity;
            bot = ComponentMappers.bot.get(entity);
            player = ComponentMappers.player.get(entity);
            physix = ComponentMappers.physixBody.get(entity);
            input = ComponentMappers.input.get(entity);
            
            if(player.isDead()) {
                input.moveDirection.setZero();
                return;
            }
            
            Iterator<Map.Entry<Entity,IgnoreEatable>> iter = bot.ignoreEatables.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<Entity,IgnoreEatable> entry = iter.next();
                IgnoreEatable value = entry.getValue();
                value.delay -= deltaTime;
                if (value.delay < 0) {
                    iter.remove();
                }
            }

            boolean isStuck = false; //cr.isStuckX() || cr.isStuckY();
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
                bot.nextBotUpdate = 3;
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
                bot.nextBotUpdate = 1;
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
            if(otherPlayer != null && !player.canEat(otherPlayer)) {
                return false;
            }

            PhysixBodyComponent otherPhysix = ComponentMappers.physixBody.get(other);
            float distance = otherPhysix.getPosition().dst(bot.oldPos);
            return distance < Constants.BOT_DETECTION_RADIUS;
        }

        Entity findEatable(ImmutableArray<Entity> entities) {
            float closestDistance = 0;
            Entity closestEntity = null;

            for(Entity e: entities) {
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
