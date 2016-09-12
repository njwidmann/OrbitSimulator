package com.application.orbit;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * This class simulates an expected launch trajectory of a body by mapping it out with dots.
 * Created by Nick on 4/28/2016.
 */
public class LaunchSimulation extends Stage {

    /*The user is allowed to adjust the length of the launch simulation (i.e. number of dots)
      Shorter simulations run faster but reveal less to the user about the projected launch path
      of the body
     */
    final static int MAX_NUM_DOTS = 30; //max length
    final static int MIN_NUM_DOTS = 1; //min length
    final static int DEFAULT_NUM_DOTS = 10; //default length
    final float STEP_SIZE = 0.05f; //the length of time elapsed between simulated positions
    final int STEPS_BETWEEN_DOTS = 2; //how many positions to simulate before placing a visual for the user (dot)

    GameScreen gameScreen; //the core file runnning the logic in the app
    Dot[] dots; //the dots graphics the user sees. See @Dot
    int numDots; //the number of dots the user has selected to display in the simulation
    Vector2 releaseLocation; //the position the user released their finger from the screen
    boolean visible; //whether or not the dots should be visible to the user
    boolean dynamicLaunchActivated; //whether the launch simulation is activated and this class should operate
    Body simulatingBody; //The body whose launch is being simulated

    /**
     * Constructor for the LaunchSimulation class. This should be instantiated one time inside the
     * onCreate() method of the GameScreen class. A single Launch Simulation object is then
     * responsible for simulating all of the launches in the app.
     * @param gameScreen The core object running the logic in the app. This should be the object that
     *                   calls this constructor
     * @param viewport libGDX object that determines what the unit boundaries of the screen are
     */
    public LaunchSimulation(final GameScreen gameScreen, Viewport viewport) {
        super(viewport); //Call the super constructor from the Stage parent class

        this.gameScreen = gameScreen;

        numDots = DEFAULT_NUM_DOTS; //on initial load, set to default. user can change later

        dots = new Dot[MAX_NUM_DOTS]; //create a Dot array with enough space for the max number of dots
        for(int i = 0; i < dots.length; i++) { //fill the array
            dots[i] = new Dot(gameScreen.bodyHighlight); //bodyHighlight is the graphic used for the dot
            addActor(dots[i]); //add Dot (Actor) to LaunchSimulation (Stage)
            dots[i].setVisible(false); //make the dot invisible to start
        }

        dynamicLaunchActivated = true; //dynamic launch is activated by default

    }

    /**
     * @return Whether or not dynamic launch is activated. This is set by the user. Default is true
     */
    public boolean isDynamicLaunchActivated() {
        return dynamicLaunchActivated;
    }

    /**
     * Called when the user is toggling dynamic launch in settings
     * @param bool true = dynamic launch activated
     */
    public void setDynamicLaunchActivated(boolean bool) {
        dynamicLaunchActivated = bool;
    }

    /**
     * @return the number of dots the user has set to display. Default is 10
     */
    public int getNumDots() {
        return numDots;
    }

    /**
     * Called when the user is setting the number of dots to display
     * @param numDots the number of dots
     */
    public void setNumDots(int numDots) {
        this.numDots = numDots;
    }

    /**
     * Release location is the location (x,y) of the user's finger at the time the launch is being
     * simulated. This is saved by the simulation object so that it can automatically adjust when the
     * user adjusts the number of dots.
     * @param x the x coord of the release location in world dimensions
     * @param y the y coord of the release location in world dimensions
     */
    public void setReleaseLocation(float x, float y) {
        setReleaseLocation(new Vector2(x, y));
    }

    /**
     * Release location is the location (x,y) of the user's finger at the time the launch is being
     * simulated. This is saved by the simulation object so that it can automatically adjust when the
     * user adjusts the number of dots.
     * @param location the vector (x,y) release location in world dimensions
     */
    public void setReleaseLocation(Vector2 location) {
        releaseLocation = location;
    }

    /**
     * @return The user's finger location for the launch
     */
    public Vector2 getReleaseLocation() {
        return releaseLocation;
    }

    /**
     * @return true = launch simulation (dots) is visible
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * @return the body whose launch is being simulated
     */
    public Body getSimulatingBody() {
        return simulatingBody;
    }

    /**
     * This method simulates the orbit for "body" given "velocity". It uses the 4th order Runge Kutta
     * method (RK4) to simulate where the body will be at each iteration. It assumes that none of the other
     * bodies on the screen will move. It uses numDots Dot objects (basically just an image of a dot)
     * to map out where the body will be over time.
     * @param body the body to simulate
     * @param velocity0 The velocity as a Vector2 object
     */
    public void doSimulation(Body body, Vector2 velocity0) {
        simulatingBody = body; //set instance variable simulating body to the body param
        visible = true; //dots should now be visible
        if(numDots <= 1) {
            return; //don't do the simulation if the user set the launch simulation size to 0
        }
        if(gameScreen.getDynamicSprite(body).isMovable()) { //if the body is locked in place, it won't move
            Vector2[] positions = new Vector2[numDots * STEPS_BETWEEN_DOTS]; //there will STEPS_BETWEEN_DOTS position steps in between each dot
            Vector2[] velocities = new Vector2[positions.length]; //velocity will be recalculated at each step
            positions[0] = body.getWorldCenter(); //the starting position for the simulation will be the current location of the body
            velocities[0] = velocity0; //starting velocity will be current velocity

            for (int i = 1; i < positions.length; i++) { //now actually run the simulation

                if(dynamicLaunchActivated) { //dynamic launch predicts path of body due to gravity

                    //use RK4 to find the next step's velocity and position
                    velocities[i] = getVelocity(STEP_SIZE, positions[i-1], velocities[i-1]);
                    positions[i]  = getPosition(STEP_SIZE, positions[i-1], velocities[i-1]);

                } else { //if dynamic launch simulation is off, just go STRAIGHT
                    //update position: x1 = x0 + v0t , x2 = x1 + v0t, x(n) = x(n-1) + v0*t
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


    /**
     * This method calculates the acceleration a body feels at a certain position based on the body's
     * mass and the masses and positions of the other bodies in the simulation. Uses Newton's Universal
     * Law of Gravitation to find force and then F=ma to get acceleration.
     * @param body the body to find the acceleration of
     * @param pos the position of the body
     * @return a Vector2 object representing the acceleration of the body (a_x, a_y)
     */
    public Vector2 getAcceleration(Body body, Vector2 pos) {
        Vector2 acceleration = new Vector2(0,0); //create a zero vector for the total acceleration

        /*
            loop through all of the bodies on the screen and sum up the component acceleration vectors
            due to each of the bodies
        */
        for (Body body2 : gameScreen.bodies) {
            if (body != body2) { //don't apply gravity between a body and itself
                float m1 = body.getMass();
                float m2 = body2.getMass();

                Vector2 r = new Vector2(body2.getWorldCenter()).sub(pos); //radius vector between the body and the latest simulated position

                //get r magnitude. It is important to get this before getting r_hat because calling r.nor() will actually normalize the r vector, not just return r_hat
                float r_mag = r.len() / (float) Math.sqrt(gameScreen.SIZE_ADJUSTMENT_FACTOR);

                //get r unit vector
                Vector2 r_hat = r.nor();

                //Use Newton's Universal Law of Gravitation... F = G(m1)(m2) / ||r||^2 * r_hat
                Vector2 f = r_hat.scl((float) (gameScreen.GRAVITY_CONSTANT * m1 * m2 / Math.pow(r_mag, 2)));

                //get acceleration from f
                Vector2 a = f.scl(1 / body.getMass()); // a = f/m

                acceleration.add(a); //add this component to the total

            }
        }

        return acceleration;
    }


    /**
     * This method uses RK4 to find the next velocity based on the previous position and velocity
     * @param step the time step
     * @param pos0 the previous position as a Vector2 object (x,y)
     * @param vel0 the previous velocity as a Vector2 object (v_x, v_y)
     * @return the next velocity as a Vector2 object (v_x, v_y)
     */
    public Vector2 getVelocity(float step, Vector2 pos0, Vector2 vel0) {
        Vector2 a = new Vector2(pos0);
        Vector2 A = getAcceleration(simulatingBody, a); //dv_dt0(x0)
        Vector2 b = new Vector2(vel0).add(new Vector2(A).scl(step / 2)).scl(step / 2).add(pos0);
        Vector2 B = getAcceleration(simulatingBody, b); //dv_dt0(x0 + dt/2 * (v0 + dt/2 * A))
        Vector2 c = new Vector2(vel0).add(new Vector2(B).scl(step / 2)).scl(step / 2).add(pos0);
        Vector2 C = getAcceleration(simulatingBody, c); //dv_dt0(x0 + dt/2 * (v0 + dt/2 * B))
        Vector2 d = new Vector2(vel0).add(new Vector2(C).scl(step    )).scl(step    ).add(pos0);
        Vector2 D = getAcceleration(simulatingBody, d); //dv_dt0(x0 + dt   * (v0 + dt   * C))

        Vector2 dv_dt = A.add(B.scl(2)).add(C.scl(2)).add(D);
        dv_dt.scl(1/6f); // (A + 2*B + 2*C + D) / 6

        return dv_dt.scl(step).add(vel0); // velocity = v0 + dv_dt * dt
    }

    /**
     * This method uses RK4 to find the next position based on the previous position and velocity
     * @param step the time step
     * @param pos0 the previous position as a Vector2 object (x,y)
     * @param vel0 the previous velocity as a Vector2 object (v_x, v_y)
     * @return the next position as a Vector2 object (x, y)
     */
    public Vector2 getPosition(float step, Vector2 pos0, Vector2 vel0) {
        Vector2 e = new Vector2(pos0);
        Vector2 E = getVelocity(0, e, vel0);        //dx_dt0(x0)
        Vector2 f = new Vector2(pos0).add(new Vector2(E).scl(step/2));
        Vector2 F = getVelocity(step / 2, f, vel0);   //dx_dt0(x0 + (dt/2 * E))
        Vector2 g = new Vector2(pos0).add(new Vector2(F).scl(step/2));
        Vector2 G = getVelocity(step / 2, g, vel0); //dx_dt0(x0 + (dt/2 * F))
        Vector2 h = new Vector2(pos0).add(new Vector2(G).scl(step  ));
        Vector2 H = getVelocity(step, h, vel0);     //dx_dt0(x0 + (dt   * G))

        Vector2 dx_dt = E.add(F.scl(2)).add(G.scl(2)).add(H);
        dx_dt.scl(1/6f);  // (E + 2*F + 2*G + H) / 6

        return dx_dt.scl(step).add(pos0); // position = x0 + dx_dt * dt
    }

    /**
     * This method stops the simulation by hiding all of the dots
     */
    public void stopSimulation() {
        for(int i = 0; i < dots.length; i++) { //loop through all the dots and set them all to invisible
            dots[i].setVisible(false);
        }
        visible = false;
    }

    /**
     * This method gets called when the user changes body properties (pos, vel, mass) manually in the
     * body properties menu or adjusts the size of the launch simulation.
     */
    public void updateSimulation() {
        if (isVisible()) {
            stopSimulation();
            doSimulation(getSimulatingBody(), getSimulatingBody().getLinearVelocity());
        }
    }

}
