package com.application.orbit;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * This class simulates an expected orbit by mapping it out with dots.
 * Created by Nick on 4/28/2016.
 */
public class LaunchSimulation extends Stage {

    final static int MAX_NUM_DOTS = 30;
    final static int MIN_NUM_DOTS = 1;
    final static int DEFAULT_NUM_DOTS = 10;
    final int STEPS_BETWEEN_DOTS = 2;
    final float STEP_SIZE = 0.05f;
    Vector2 releaseLocation;

    GameScreen gameScreen;
    Dot[] dots;
    int numDots;
    boolean visible, dynamicLaunchActivated;
    Body simulatingBody;

    public LaunchSimulation(final GameScreen gameScreen, Viewport viewport) {
        super(viewport);

        this.gameScreen = gameScreen;

        numDots = DEFAULT_NUM_DOTS;

        dots = new Dot[MAX_NUM_DOTS];
        for(int i = 0; i < dots.length; i++) {
            dots[i] = new Dot(gameScreen.bodyHighlight);
            addActor(dots[i]);
            dots[i].setVisible(false);
        }

        dynamicLaunchActivated = true;

    }

    public boolean isDynamicLaunchActivated() {
        return dynamicLaunchActivated;
    }

    public void setDynamicLaunchActivated(boolean bool) {
        dynamicLaunchActivated = bool;
    }

    public int getNumDots() {
        return numDots;
    }

    public void setNumDots(int numDots) {
        this.numDots = numDots;
    }

    public void setReleaseLocation(float x, float y) {
        releaseLocation = new Vector2(x, y);
    }

    public void setReleaseLocation(Vector2 location) {
        releaseLocation = location;
    }

    public Vector2 getReleaseLocation() {
        return releaseLocation;
    }

    public boolean isVisible() {
        return visible;
    }

    public Body getSimulatingBody() {
        return simulatingBody;
    }

    /**
     * This method simulates the orbit for "body" given "velocity". It uses the 4th order Runge Kutta method to simulate
     * where the body will be at each iteration. It assumes that none of the other bodies on the
     * screen will move. It uses numDots Dot objects (basically just an image of a dot) to map out
     * where the body will be over time.
     * @param body the body to simulate
     * @param velocity0 The velocity as a Vector2 object
     */
    public void doSimulation(Body body, Vector2 velocity0) {
        simulatingBody = body;
        visible = true;
        if(numDots <= 1) {
            return;
        }
        if(gameScreen.getDynamicSprite(body).isMovable()) { //if the body is locked in place, it won't move
            Vector2[] positions = new Vector2[numDots * STEPS_BETWEEN_DOTS]; //we will simulate 10 steps in between each dot
            Vector2[] velocities = new Vector2[positions.length];
            positions[0] = body.getWorldCenter(); //the starting position for the simulation will be the current location of the body
            velocities[0] = velocity0;

            for (int i = 1; i < positions.length; i++) {

                if(dynamicLaunchActivated) { //dynamic launch predicts path of body due to gravity

                    velocities[i] = getVelocity(STEP_SIZE, positions[i-1], velocities[i-1]);
                    positions[i]  = getPosition(STEP_SIZE, positions[i-1], velocities[i-1]);

                } else { //if dynamic launch simulation is off, just go straight
                    //update position: x1 = x0 + v0t , x2 = x1 + v1t, x(n) = x(n-1) + v(n-1)*t
                    positions[i] = new Vector2(positions[i - 1].x + velocity0.x * STEP_SIZE, positions[i - 1].y + velocity0.y * STEP_SIZE);

                }
            }

            //now update the positions, visibility, and sizes of the dots
            for (int i = 0; i < numDots; i++) {
                dots[i].setVisible(true);
                float length = 2 * gameScreen.getZoom();
                dots[i].setSize(length, length);
                Vector2 position = positions[STEPS_BETWEEN_DOTS * i];
                dots[i].setPositionCenter(position.x, position.y);
            }
        }


    }


    public Vector2 getAcceleration(Body body, Vector2 pos) {
        Vector2 acceleration = new Vector2(0,0);

        for (Body body2 : gameScreen.bodies) { //loop through all of the bodies on the screen for each new acceleration
            if (body != body2) { //don't apply gravity between a body and itself
                float m1 = body.getMass();
                float m2 = body2.getMass();

                Vector2 r = new Vector2(body2.getWorldCenter()).sub(pos); //radius vector between the body and the latest simulated position

                //get r magnitude. It is important to get this before getting r_hat because calling r.nor() will actually normalize the r vector, not just return r_hat
                float r_mag = r.len() / (float) Math.sqrt(gameScreen.SIZE_ADJUSTMENT_FACTOR);

                //get r unit vector
                Vector2 r_hat = r.nor();

                //F = G(m1)(m2) / ||r||^2 * r_hat
                Vector2 f = r_hat.scl((float) (gameScreen.GRAVITY_CONSTANT * m1 * m2 / Math.pow(r_mag, 2)));

                //get acceleration from f
                Vector2 a = f.scl(1 / body.getMass()); // a = f/m

                acceleration.add(a);

            }
        }

        return acceleration;
    }


    public Vector2 getVelocity(float step, Vector2 pos0, Vector2 vel0) {
        Vector2 a = new Vector2(pos0);
        Vector2 A = getAcceleration(simulatingBody, a);
        Vector2 b = new Vector2(vel0).add(new Vector2(A).scl(step / 2)).scl(step / 2).add(pos0); //dv_dt0(x0 + dt/2 * (v0 + dt/2 * A))
        Vector2 B = getAcceleration(simulatingBody, b);
        Vector2 c = new Vector2(vel0).add(new Vector2(B).scl(step / 2)).scl(step / 2).add(pos0); //dv_dt0(x0 + dt/2 * (v0 + dt/2 * B))
        Vector2 C = getAcceleration(simulatingBody, c);
        Vector2 d = new Vector2(vel0).add(new Vector2(C).scl(step    )).scl(step    ).add(pos0); //dv_dt0(x0 + dt   * (v0 + dt   * C))
        Vector2 D = getAcceleration(simulatingBody, d);

        Vector2 dv_dt = A.add(B.scl(2)).add(C.scl(2)).add(D);
        dv_dt.scl(1/6f); // (A + 2*B + 2*C + D) / 6

        return dv_dt.scl(step).add(vel0); // velocity = v0 + dv_dt * dt
    }

    public Vector2 getPosition(float step, Vector2 pos0, Vector2 vel0) {
        Vector2 e = new Vector2(pos0);
        Vector2 E = getVelocity(0, e, vel0);
        Vector2 f = new Vector2(pos0).add(new Vector2(E).scl(step/2));
        Vector2 F = getVelocity(step/2, f, vel0);
        Vector2 g = new Vector2(pos0).add(new Vector2(F).scl(step/2));
        Vector2 G = getVelocity(step / 2, g, vel0);
        Vector2 h = new Vector2(pos0).add(new Vector2(G).scl(step));
        Vector2 H = getVelocity(step, h, vel0);

        Vector2 dx_dt = E.add(F.scl(2)).add(G.scl(2)).add(H);
        dx_dt.scl(1/6f);

        return dx_dt.scl(step).add(pos0);
    }

    /**
     * This method stops the simulation by hiding all of the dots
     */
    public void stopSimulation() {
        for(int i = 0; i < dots.length; i++) {
            dots[i].setVisible(false);
        }
        visible = false;
    }

    public void updateSimulation() {
        if (isVisible()) {
            stopSimulation();
            doSimulation(getSimulatingBody(), getSimulatingBody().getLinearVelocity());
        }
    }

}
