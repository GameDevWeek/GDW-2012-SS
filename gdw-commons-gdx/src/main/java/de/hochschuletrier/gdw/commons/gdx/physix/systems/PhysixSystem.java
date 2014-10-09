package de.hochschuletrier.gdw.commons.gdx.physix.systems;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.RopeJointDef;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import de.hochschuletrier.gdw.commons.gdx.physix.components.PhysixBodyComponent;
import de.hochschuletrier.gdw.commons.utils.Point;
import java.util.List;

/**
 *
 * @author Santo Pfingsten
 */
public class PhysixSystem extends EntitySystem {

    public final float scale, scaleInv;
    protected final World world;
    protected final Vector2 gravity = new Vector2();
    protected final float timeStep;
    protected final int velocityIterations;
    protected final int positionIterations;
    private float timeAccumulator;
    
    public PhysixSystem(float scale, float timeStep, int velocityIterations, int positionIterations) {
        this.scale = scale;
        this.timeStep = timeStep;
        this.velocityIterations = velocityIterations;
        this.positionIterations = positionIterations;
        scaleInv = 1.0f / scale;
        world = new World(gravity, true);
    }
    
	public void update(float deltaTime) {
        timeAccumulator += deltaTime;
        if (timeAccumulator >= timeStep) {
            timeAccumulator -= timeStep;
            world.step(timeStep, velocityIterations, positionIterations);
            world.clearForces();
        }
    }

    public void reset() {
        if (world.isLocked()) {
            throw new GdxRuntimeException("PhysixSystem.reset called in locked state");
        }

        Array<Body> bodies = new Array();
        world.getBodies(bodies);
        for (Body body : bodies) {
            world.destroyBody(body);
        }

        Array<Joint> joints = new Array();
        world.getJoints(joints);
        for (Joint joint : joints) {
            world.destroyJoint(joint);
        }
    }

    public World getWorld() {
        return world;
    }

    public void destroy(PhysixBodyComponent body) {
        world.destroyBody(body.getBody());
    }

    public void setGravity(float x, float y) {
        gravity.set(x, y);
        world.setGravity(gravity);
        Array<Body> bodies = new Array();
        world.getBodies(bodies);
        for (Body body : bodies) {
            body.setAwake(true);
        }
    }

    public void ropeConnect(PhysixBodyComponent a, PhysixBodyComponent b, float length) {
        RopeJointDef ropeJointDef = new RopeJointDef();
        ropeJointDef.bodyA = a.getBody();
        ropeJointDef.bodyB = b.getBody();
        ropeJointDef.maxLength = length * scale;
        ropeJointDef.collideConnected = true;
        world.createJoint(ropeJointDef);
    }

    /**
     * Convert world to box2d coordinates
     */
    public float toBox2D(float pixel) {
        return pixel * scaleInv;
    }

    /**
     * Convert box2d to world coordinates
     */
    public float toWorld(float num) {
        return num * scale;
    }

    /**
     * Convert world to box2d coordinates
     */
    public Vector2 toBox2D(float x, float y, Vector2 out) {
        out.set(x * scaleInv, y * scaleInv);
        return out;
    }

    /**
     * Convert world to box2d coordinates
     */
    public Vector2 toBox2D(Vector2 in, Vector2 out) {
        out.set(in.x * scaleInv, in.y * scaleInv);
        return out;
    }

    /**
     * Convert box2d to world coordinates
     */
    public Vector2 toWorld(Vector2 in, Vector2 out) {
        out.set(in.x * scale, in.y * scale);
        return out;
    }

    public Vector2[] toBox2D(List<Point> pointList) {
        Vector2[] returner = new Vector2[pointList.size()];
        for (int pointCount = 0; pointCount < returner.length; pointCount++) {
            Point p = pointList.get(pointCount);
            returner[pointCount] = new Vector2(p.x * scale, p.y * scale);
        }
        return returner;
    }
}