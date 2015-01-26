package de.hochschuletrier.gdw.ss12.game.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import de.hochschuletrier.gdw.ss12.game.ComponentMappers;
import de.hochschuletrier.gdw.ss12.game.components.ParticleEffectComponent;
import de.hochschuletrier.gdw.ss12.game.components.PlayerComponent;
import de.hochschuletrier.gdw.ss12.game.data.PlayerEffect;

public class UpdatePlayerEffectsSystem extends IteratingSystem {

    public UpdatePlayerEffectsSystem() {
        super(Family.all(PlayerComponent.class).get(), 0);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        PlayerComponent player = ComponentMappers.player.get(entity);
        ParticleEffectComponent particleEffect = ComponentMappers.particleEffect.get(entity);
        boolean updateSpawnShapeSize = Math.abs(player.radius - player.lastSpawnShapeSize) > 5;
        if (updateSpawnShapeSize) {
            player.lastSpawnShapeSize = player.radius;
        }

        for (PlayerEffect effect : PlayerEffect.values()) {
            ParticleEmitter emitter = particleEffect.getEmitter(effect.name());
            if (emitter != null) {
                if ((effect.getBit() & player.effectBits) != 0) {
                    // Reset duration timer
                    emitter.duration = Integer.MAX_VALUE;
                    emitter.durationTimer = 0;

                    // Update spawn shape size
                    if (updateSpawnShapeSize) {
                        setParticleSpawnShapeSize(emitter, player.radius * effect.shapeScale);
                    }
                } else {
                    // Stop effect
                    emitter.duration = 0;
                    emitter.durationTimer = 0;
                }
            }
        }
    }

    private void setParticleSpawnShapeSize(ParticleEmitter emitter, float size) {
        final ParticleEmitter.ScaledNumericValue spawnWidth = emitter.getSpawnWidth();
        spawnWidth.setHigh(size);
        spawnWidth.setLow(size);
        final ParticleEmitter.ScaledNumericValue spawnHeight = emitter.getSpawnHeight();
        spawnHeight.setHigh(size);
        spawnHeight.setLow(size);
        emitter.start();
    }
}
