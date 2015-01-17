package de.hochschuletrier.gdw.ss12.game.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.utils.Pool;

public class ParticleEffectComponent extends Component implements Pool.Poolable {

    public ParticleEffect effect;
    public boolean draw;

    @Override
    public void reset() {
        effect = null;
        draw = true;
    }

    public ParticleEmitter getEmitter(String name) {
        for (ParticleEmitter emitter : effect.getEmitters()) {
            if(name.equalsIgnoreCase(emitter.getName())) {
                return emitter;
            }
        }
        return null;
    }
}
