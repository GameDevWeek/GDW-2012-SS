package de.hochschuletrier.gdw.ss12.game.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.systems.IteratingSystem;
import de.hochschuletrier.gdw.commons.devcon.DevConsole;
import de.hochschuletrier.gdw.commons.devcon.cvar.CVarEnum;
import de.hochschuletrier.gdw.commons.gdx.assets.AssetManagerX;
import de.hochschuletrier.gdw.commons.gdx.audio.SoundDistanceModel;
import de.hochschuletrier.gdw.commons.gdx.audio.SoundEmitter;
import de.hochschuletrier.gdw.ss12.Main;
import de.hochschuletrier.gdw.ss12.game.ComponentMappers;
import de.hochschuletrier.gdw.ss12.game.Game;
import de.hochschuletrier.gdw.ss12.game.components.PositionComponent;
import de.hochschuletrier.gdw.ss12.game.components.SoundEmitterComponent;
import de.hochschuletrier.gdw.ss12.game.interfaces.SystemGameInitializer;

public class UpdateSoundEmitterSystem extends IteratingSystem implements SystemGameInitializer {

    private final CVarEnum<SoundDistanceModel> distanceModel = new CVarEnum("snd_distanceModel", SoundDistanceModel.INVERSE, SoundDistanceModel.class, 0, "sound distance model");
    private final CVarEnum<SoundEmitter.Mode> emitterMode = new CVarEnum("snd_mode", SoundEmitter.Mode.STEREO, SoundEmitter.Mode.class, 0, "sound mode");
    private Game game;

    public UpdateSoundEmitterSystem() {
        super(Family.all(PositionComponent.class, SoundEmitterComponent.class).get(), 0);
    }

    @Override
    public void initGame(Game game, AssetManagerX assetManager, PooledEngine engine) {
        this.game = game;
        DevConsole console = Main.getInstance().console;
        console.register(distanceModel);
        distanceModel.addListener((CVar) -> distanceModel.get().activate());

        console.register(emitterMode);
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        PositionComponent position = ComponentMappers.position.get(game.getLocalPlayer());
        SoundEmitter.setListenerPosition(position.x, position.y, 10, emitterMode.get());
        SoundEmitter.updateGlobal();
    }

    @Override
    public void processEntity(Entity entity, float deltaTime) {
        SoundEmitterComponent sound = ComponentMappers.soundEmitter.get(entity);
        PositionComponent position = ComponentMappers.position.get(entity);
        sound.emitter.setPosition(position.x, position.y, 0);
        sound.emitter.update();
    }
}
