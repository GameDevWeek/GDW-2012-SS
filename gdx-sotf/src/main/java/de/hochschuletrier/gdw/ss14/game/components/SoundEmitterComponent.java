package de.hochschuletrier.gdw.ss14.game.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;
import de.hochschuletrier.gdw.commons.gdx.sound.SoundEmitter;

public class SoundEmitterComponent extends Component implements Pool.Poolable {
    public final SoundEmitter emitter = new SoundEmitter();

    @Override
    public void reset() {
        emitter.dispose();
    }
}