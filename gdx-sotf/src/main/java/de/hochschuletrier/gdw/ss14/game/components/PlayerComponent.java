package de.hochschuletrier.gdw.ss14.game.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import de.hochschuletrier.gdw.ss14.game.componentdata.PlayerState;
import de.hochschuletrier.gdw.ss14.game.componentdata.Powerup;
import de.hochschuletrier.gdw.ss14.game.componentdata.Powerup.ModifierType;
import de.hochschuletrier.gdw.ss14.game.componentdata.Team;
import java.util.ArrayList;
import java.util.List;

public class PlayerComponent extends Component implements Pool.Poolable {
    public final Vector2 startPosition = new Vector2();
    public final List<Powerup> newPowerups = new ArrayList();
    public final List<Powerup> powerups = new ArrayList();

    public float radius;
    public String name;
    public PlayerState state;
    public Team team;
    public long lastTeleport;
    public Entity killer;
    
    @Override
    public void reset() {
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
