package de.hochschuletrier.gdw.ss12.game.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import de.hochschuletrier.gdw.commons.gdx.assets.AssetManagerX;
import de.hochschuletrier.gdw.commons.gdx.physix.components.PhysixBodyComponent;
import de.hochschuletrier.gdw.commons.gdx.physix.systems.PhysixSystem;
import de.hochschuletrier.gdw.ss12.game.ComponentMappers;
import de.hochschuletrier.gdw.ss12.game.Constants;
import de.hochschuletrier.gdw.ss12.game.Game;
import de.hochschuletrier.gdw.ss12.game.data.PlayerState;
import de.hochschuletrier.gdw.ss12.game.components.PlayerComponent;
import de.hochschuletrier.gdw.ss12.game.components.PositionComponent;
import de.hochschuletrier.gdw.ss12.game.data.NoticeType;
import de.hochschuletrier.gdw.ss12.game.interfaces.SystemGameInitializer;

public class UpdatePlayerSystem extends IteratingSystem implements SystemGameInitializer {

    private PhysixSystem physixSystem;
    private Engine engine;
    private Game game;

    public UpdatePlayerSystem() {
        super(Family.all(PlayerComponent.class).get(), 0);
    }

    @Override
    public void initGame(Game game, AssetManagerX assetManager) {
        this.game = game;
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        physixSystem = engine.getSystem(PhysixSystem.class);
        this.engine = engine;
    }

    @Override
    public void removedFromEngine(Engine engine) {
        super.removedFromEngine(engine);
        physixSystem = null;
        this.engine = null;
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
    }

    @Override
    public void processEntity(Entity entity, float deltaTime) {
        PlayerComponent player = ComponentMappers.player.get(entity);
        PhysixBodyComponent physix = ComponentMappers.physixBody.get(entity);

        if (player.killer != null) {
            triggerPlayerDeath(entity, player, "player_eaten");
            game.playEntitySound("player_eat_player", player.killer, false);

            PlayerComponent killer = ComponentMappers.player.get(player.killer);
            killer.statistic.kills++;
            game.scheduleNoticeForPlayer(player.team == killer.team ? NoticeType.FRIENDLY_EATEN : NoticeType.ENEMY_EATEN, 0, player.killer);
            game.scheduleNoticeForPlayer(NoticeType.DEATH, 0, entity);
            player.killer = null;
        } else if (!player.isDead()) {
            float shrinkPixelThisFrame = Constants.PLAYER_SHRINK_PIXEL_PER_SECOND * deltaTime;
            player.radius -= shrinkPixelThisFrame * (player.radius / Constants.PLAYER_MAX_SIZE);

            // check if player radius got under min_size
            if (player.radius < Constants.PLAYER_MIN_SIZE) {
                triggerPlayerDeath(entity, player, "player_starved");
                game.scheduleNoticeForPlayer(NoticeType.STARVATION, 0, entity);
            } else {
                if (player.radius > Constants.PLAYER_MAX_SIZE) {
                    player.radius = Constants.PLAYER_MAX_SIZE;
                }
                ComponentMappers.light.get(entity).setFromPlayerRadius(player.radius);
            }

            // Adjust body and sensor size
            float radius = physixSystem.toBox2D(player.radius);
            physix.getFixtureByUserData("body").getShape().setRadius(radius);
            physix.getFixtureByUserData("sensor").getShape().setRadius(radius);
        }
    }

    private void triggerPlayerDeath(Entity entity, PlayerComponent player, String animationEntity) {
        player.statistic.deaths++;
        final PositionComponent position = ComponentMappers.position.get(entity);
        engine.getSystem(EntitySpawnSystem.class).createStaticEntity(animationEntity, position.x, position.y, Constants.PLAYER_DEFAULT_SIZE, null);
        player.powerups.clear();
        player.state = PlayerState.DEAD;
        player.radius = Constants.PLAYER_DEFAULT_SIZE;
        ComponentMappers.light.get(entity).radius = 0;
        position.ignorePhysix = true;
        ComponentMappers.physixBody.get(entity).setActive(false);
        engine.getSystem(PowerupSystem.class).removePlayerPowerups(entity, player);
    }
}
