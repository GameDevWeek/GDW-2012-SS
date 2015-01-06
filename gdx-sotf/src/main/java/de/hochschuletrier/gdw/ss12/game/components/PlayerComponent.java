package de.hochschuletrier.gdw.ss12.game.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import de.hochschuletrier.gdw.ss12.Main;
import de.hochschuletrier.gdw.ss12.game.components.data.PlayerEffect;
import de.hochschuletrier.gdw.ss12.game.components.data.PlayerState;
import de.hochschuletrier.gdw.ss12.game.components.data.PlayerStatistic;
import de.hochschuletrier.gdw.ss12.game.components.data.Powerup;
import de.hochschuletrier.gdw.ss12.game.components.data.Powerup.ModifierType;
import de.hochschuletrier.gdw.ss12.game.components.data.Team;
import java.util.ArrayList;
import java.util.List;

public class PlayerComponent extends Component implements Pool.Poolable {
    
    public final ParticleEffect particleEffect;
    public final Vector2 startPosition = new Vector2();
    public final List<Powerup> newPowerups = new ArrayList();
    public final List<Powerup> powerups = new ArrayList();
    public final PlayerStatistic statistic = new PlayerStatistic();
    public final ParticleEmitter []particleEmitters = new ParticleEmitter[PlayerEffect.values().length];

    public float radius;
    public String name;
    public PlayerState state;
    public Team team;
    public long lastTeleport;
    public Entity killer;
    public long lastSequenceId;
    public float lastSpawnShapeSize;
    public boolean hasPizzaPowerup;
    
    public PlayerComponent() {
        particleEffect = new ParticleEffect(Main.getInstance().getAssetManager().getParticleEffect("player"));
        clearParticleEffect();
    }

    private void clearParticleEffect() {
        particleEffect.start();
        for (PlayerEffect effect: PlayerEffect.values()) {
            final ParticleEmitter emitter = particleEffect.findEmitter(effect.name().toLowerCase());
            if(emitter != null) {
                emitter.setContinuous(false);
                emitter.duration = 0;
                emitter.durationTimer = 0;
                particleEmitters[effect.ordinal()] = emitter;
            }
        }
    }

    @Override
    public void reset() {
        statistic.reset();
        particleEffect.reset();
        clearParticleEffect();
        //todo
    }
    
    public boolean canEat(PlayerComponent other) {
        if(isDead())
            return false;
        for (Powerup powerup: other.powerups) {
            for(Powerup.Modifier modifier: powerup.modifiers) {
                if (modifier.type == ModifierType.IMMUNITY) {
                    return false;
                }
            }
        }
        if (radius <= other.radius || other.isDead()) {
            return false;
        }
        return team != other.team || isHalucinating();
    }
    
    public boolean isDead() {
        return state == PlayerState.DEAD || killer != null;
    }
    
    public boolean isHalucinating() {
        return state == PlayerState.HALUCINATING;
    }

    public boolean isSlipping() {
        return state == PlayerState.SLIPPING;
    }
}
