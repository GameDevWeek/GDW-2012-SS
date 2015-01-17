package de.hochschuletrier.gdw.ss12.game;

import com.badlogic.ashley.core.ComponentMapper;
import de.hochschuletrier.gdw.commons.gdx.physix.components.*;
import de.hochschuletrier.gdw.ss12.game.components.*;

public class ComponentMappers {

    public static final ComponentMapper<BotComponent> bot = ComponentMapper.getFor(BotComponent.class);
    public static final ComponentMapper<EatableComponent> eatable = ComponentMapper.getFor(EatableComponent.class);
    public static final ComponentMapper<InputComponent> input = ComponentMapper.getFor(InputComponent.class);
    public static final ComponentMapper<ItemTrapComponent> itemTrap = ComponentMapper.getFor(ItemTrapComponent.class);
    public static final ComponentMapper<LightComponent> light = ComponentMapper.getFor(LightComponent.class);
    public static final ComponentMapper<PlayerComponent> player = ComponentMapper.getFor(PlayerComponent.class);
    public static final ComponentMapper<PositionComponent> position = ComponentMapper.getFor(PositionComponent.class);
    public static final ComponentMapper<RenderTextureComponent> renderTexture = ComponentMapper.getFor(RenderTextureComponent.class);
    public static final ComponentMapper<RenderAnimationComponent> renderAnimation = ComponentMapper.getFor(RenderAnimationComponent.class);
    public static final ComponentMapper<TriggerComponent> trigger = ComponentMapper.getFor(TriggerComponent.class);
    public static final ComponentMapper<DropableComponent> dropable = ComponentMapper.getFor(DropableComponent.class);
    public static final ComponentMapper<PhysixBodyComponent> physixBody = ComponentMapper.getFor(PhysixBodyComponent.class);
    public static final ComponentMapper<PhysixModifierComponent> physixModifier = ComponentMapper.getFor(PhysixModifierComponent.class);
    public static final ComponentMapper<SoundEmitterComponent> soundEmitter = ComponentMapper.getFor(SoundEmitterComponent.class);
    public static final ComponentMapper<PizzaSliceComponent> pizzaSlice = ComponentMapper.getFor(PizzaSliceComponent.class);
    public static final ComponentMapper<ParticleEffectComponent> particleEffect = ComponentMapper.getFor(ParticleEffectComponent.class);
}
