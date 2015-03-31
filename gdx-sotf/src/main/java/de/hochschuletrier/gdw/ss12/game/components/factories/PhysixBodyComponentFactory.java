package de.hochschuletrier.gdw.ss12.game.components.factories;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import de.hochschuletrier.gdw.commons.gdx.assets.AssetManagerX;
import de.hochschuletrier.gdw.commons.gdx.entityFactory.ComponentFactory;
import de.hochschuletrier.gdw.commons.gdx.physix.PhysixBodyDef;
import de.hochschuletrier.gdw.commons.gdx.physix.PhysixFixtureDef;
import de.hochschuletrier.gdw.commons.gdx.physix.components.PhysixBodyComponent;
import de.hochschuletrier.gdw.commons.gdx.physix.components.PhysixModifierComponent;
import de.hochschuletrier.gdw.commons.gdx.physix.systems.PhysixSystem;
import de.hochschuletrier.gdw.commons.utils.SafeProperties;

public class PhysixBodyComponentFactory extends ComponentFactory<EntityFactoryParam> {

    private PhysixSystem physixSystem;

    @Override
    public String getType() {
        return "PhysixBody";
    }

    @Override
    public void init(PooledEngine engine, AssetManagerX assetManager) {
        super.init(engine, assetManager);

        physixSystem = engine.getSystem(PhysixSystem.class);
        //fixme: read blueprints from json
    }

    @Override
    public void run(Entity entity, SafeProperties meta, SafeProperties properties, EntityFactoryParam param) {
        final PhysixModifierComponent modifyComponent = engine.createComponent(PhysixModifierComponent.class);
        final float x = param.x;
        final float y = param.y;
        modifyComponent.schedule(() -> {
            float radius = properties.getFloat("scale");
            PhysixBodyComponent bodyComponent = engine.createComponent(PhysixBodyComponent.class);
            if("player".equals(properties.getString("type"))) {
                PhysixBodyDef bodyDef = new PhysixBodyDef(BodyDef.BodyType.DynamicBody, physixSystem)
                        .position(x, y).fixedRotation(true);//.linearDamping(20);
                bodyComponent.init(bodyDef, physixSystem, entity);
                PhysixFixtureDef fixtureDef = new PhysixFixtureDef(physixSystem).groupIndex((short) -1)
                        .density(5).friction(0).shapeCircle(radius);
                Fixture fixture = bodyComponent.createFixture(fixtureDef);
                fixture.setUserData("body");
                PhysixFixtureDef fixtureDef2 = new PhysixFixtureDef(physixSystem)
                        .sensor(true).shapeCircle(radius);
                Fixture fixture2 = bodyComponent.createFixture(fixtureDef2);
                fixture2.setUserData("sensor");
            } else {
                PhysixBodyDef bodyDef = new PhysixBodyDef(BodyDef.BodyType.DynamicBody, physixSystem)
                        .position(x, y).awake(false);
                bodyComponent.init(bodyDef, physixSystem, entity);
                PhysixFixtureDef fixtureDef = new PhysixFixtureDef(physixSystem).sensor(true).shapeCircle(radius);
                bodyComponent.createFixture(fixtureDef);
            }
            entity.add(bodyComponent);
        });
        entity.add(modifyComponent);
    }
}
