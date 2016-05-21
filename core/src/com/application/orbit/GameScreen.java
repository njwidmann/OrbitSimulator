package com.application.orbit;

/**
 * Created by Nick on 3/29/2016.
 */

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import java.util.ArrayList;

public class GameScreen implements Screen, GestureDetector.GestureListener, InputProcessor {
    final GameActivity game;
    final float TIMESTEP = 1/60f;
    final float STANDARD_MASS = 1000;
    final float STANDARD_DENSITY = 1f;
    final float FRICTION = 0;
    final float GRAVITY_CONSTANT = 10f;
    static final int DEFAULT_BODY_MATRIX_N = 2;
    static final int MIN_BODY_MATRIX_N = 2;
    static final int MAX_BODY_MATRIX_N = 10;
    final float SIZE_ADJUSTMENT_FACTOR = 1/10f;
    final float MIN_BODY_MASS = 1;
    final float MAX_BODY_MASS = 1000000;
    final float HUD_HEIGHT = 960;
    final float WORLD_HEIGHT = 90;
    final float MAX_ZOOM = 10;
    final float MIN_ZOOM = 0.1f;
    final String PLANET_SKIN_FILE = "gfx/planet_skins/planet_skins.pack";

    float screenWidth, screenHeight, worldWidth, worldHeight, hudHeight, hudWidth;

    float timeStep;

    OrthographicCamera camera;
    World world;
    Box2DDebugRenderer debugRenderer;
    Body sun, planet;
    ArrayList<Body> bodies;
    boolean running, launching, pickingOrbit, chaseCamOn, addingBody, addingBodyMatrix;
    ArrayList<CircleShape> circles;
    int selectedBody;
    HUD hud;
    ExtendViewport hudViewport;

    SpriteBatch batch;
    Texture earth, bodyHighlight;
    ArrayList<DynamicSprite> sprites;

    LaunchSimulation launchSimulation;

    int bodyMatrixN;

    TextureAtlas planetSkins;


    public GameScreen(final GameActivity game) {
        this.game = game;

        //debug renderer is used for box2d debugging. shows shape outlines around bodies
        debugRenderer = new Box2DDebugRenderer();

        //get screen dimensions
        screenWidth = Gdx.graphics.getWidth();
        screenHeight = Gdx.graphics.getHeight();
        //define initial world dimensions for the camera. These can be adjusted later when a user zooms or resizes the bodyInfoWindow.
        worldHeight = WORLD_HEIGHT;
        worldWidth = worldHeight / screenHeight * screenWidth;


        // create the camera
        camera = new OrthographicCamera();
        camera.setToOrtho(false, worldWidth, worldHeight); //give camera initial world dimensions

        planetSkins = new TextureAtlas(PLANET_SKIN_FILE);

        //create box2d world. Assign gravity 0 vector
        world = new World(new Vector2(0, 0), true);
        //define array lists for bodies and circles. Bodies holds all of the planets, suns, and asteroids.
        bodies = new ArrayList<Body>();
        //circles holds all of the circle shape objects we create that will later need to be disposed of
        circles = new ArrayList<CircleShape>();

        batch = new SpriteBatch();
        // We need a sprite since it's going to move
        earth = new Texture("gfx/earth-cartoon-md.png");
        bodyHighlight = new Texture("gfx/body_highlight.png");
        sprites = new ArrayList<DynamicSprite>();

        //create initial bodies
        sun = createBody(100 * STANDARD_MASS, worldWidth / 2, worldHeight / 2);

        getDynamicSprite(sun).setMovable(false);

        planet = createBody(STANDARD_MASS, sun.getWorldCenter().x + 100, worldHeight / 2);


        bodyMatrixN = DEFAULT_BODY_MATRIX_N;

        //make planet orbit sun and asteroid orbit planet
        orbit(planet, sun);

        //launching is false. This is true if the user is launching a planet
        launching = false;
        //running is true
        running = true;

        //Create HUD
        hudHeight = HUD_HEIGHT;
        hudWidth = hudHeight*screenWidth/screenHeight;

        //Create viewport/camera for HUD to maintain same aspect ratio on resize
        OrthographicCamera hudCam = new OrthographicCamera(hudWidth, hudHeight);
        hudViewport = new ExtendViewport(hudWidth, hudHeight, hudCam);

        //create the hud
        hud = new HUD(this, hudViewport);

        //create the launch simulator
        launchSimulation = new LaunchSimulation(this, new ExtendViewport(100, 100, camera));

        //create stuff to measure input (taps, keys, mouse clicks, etc)
        InputMultiplexer im = new InputMultiplexer();
        GestureDetector gd = new GestureDetector(this);
        im.addProcessor(hud);
        im.addProcessor(gd);
        im.addProcessor(this);

        Gdx.input.setInputProcessor(im);

        timeStep = TIMESTEP;
        //no body is currently selected. This is used for lots of functionality. e.g. launching and scaling
        setSelectedBody(-1);

    }

    /**
     * This method pauses the game by setting timestep = 0
     */
    public void pauseGame() {
        running = false;
        timeStep = 0;
    }

    /**
     * This method unpauses the game by changing the timestep back to TIMESTEP
     */
    public void unpauseGame() {
        running = true;
        timeStep = TIMESTEP;
    }

    /**
     * This method is called every frame to check for zooming. This way zoom can be continuous.
     * TODO: set this to pinch when no planet is selected
     */
    private void handleZoom() {
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            //If the DOWN Key is pressed, zoom out
            setZoom(getZoom() + 0.2f);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            //If the UP Key is pressed, zoom in
            setZoom(getZoom() - 0.2f);
        }
    }

    /**
     * This is a setter method for the zoom property of the camera
     * @param zoom the zoom with 1 = normal. 2 = 2x zoom
     */
    public void setZoom(float zoom) {

        if (zoom > MAX_ZOOM) {
            zoom = MAX_ZOOM;
        } else if (zoom < MIN_ZOOM) {
            zoom = MIN_ZOOM;
        }

        camera.zoom = zoom;
        worldHeight = camera.viewportHeight;
        worldWidth = camera.viewportWidth;

        Gdx.app.log("GameScreen", "ZOOM = " + zoom);

    }


    /**
     * this is a getter method for the zoom property of the camera
     * @return the zoom
     */
    public float getZoom() {
        return camera.zoom;
    }

    /**
     * This method pans the camera by world coordinates deltaX, deltaY
     * @param deltaX
     * @param deltaY
     */
    private void translateCamera(float deltaX, float deltaY) {
        camera.translate(deltaX,deltaY);
        // tell the camera to update its matrices.
        camera.update();
    }

    /**
     * This pans the camera by the vector delta
     * @param delta
     */
    private void translateCamera(Vector2 delta) {
        translateCamera(delta.x, delta.y);
    }

    /**
     * This method centers the camera at 0,0
     */
    public void centerCamera() {
        centerCamera(camera.viewportWidth / 2, camera.viewportHeight / 2);
    }

    /**
     * This method centers camera on pos
     * @param pos Vector2 position to center camera at
     */
    public void centerCamera(Vector2 pos) {
        centerCamera(pos.x, pos.y);
    }

    /**
     * This method centers camera on (x,y)
     * @param x
     * @param y
     */
    public void centerCamera(float x, float y) {
        camera.position.x = x;
        camera.position.y = y;
    }

    /**
     * This method resets the camera back to the original position and zoom (centered on the main sun with zoom = 1)
     */
    public void resetCamera() {
        centerCamera();
        camera.zoom = 1;
        setChaseCamOn(false);
    }

    public void setChaseCamOn(boolean bool) {
        chaseCamOn = bool;
    }

    public boolean isChaseCamOn() {
        return chaseCamOn;
    }

    public DynamicSprite getDynamicSpriteFromTextureRegion(TextureAtlas.AtlasRegion region) {
        if (region.packedWidth == region.originalWidth && region.packedHeight == region.originalHeight) {
            if (region.rotate) {
                DynamicSprite sprite = new DynamicSprite(region);
                sprite.setBounds(0, 0, region.getRegionHeight(), region.getRegionWidth());
                sprite.rotate90(true);
                return sprite;
            }
            return new DynamicSprite(region);
        }
        return new DynamicSprite(region);
    }

    public TextureAtlas.AtlasRegion getRandomPlanetSkin() {
        int rand = (int)(Math.random() * 10) + 1;
        String name = "Planet" + rand;
        return getRegionFromAtlas(planetSkins, name);
    }

    public TextureAtlas.AtlasRegion getRegionFromAtlas(TextureAtlas atlas, String regionName) {
        Array<TextureAtlas.AtlasRegion> regions = atlas.getRegions();
        for (int i = 0, n = regions.size; i < n; i++)
            if (regions.get(i).name.equals(regionName)) return regions.get(i);
        return null;
    }

    /**
     * this method creates a new body and adds it to the bodies arraylist
     * @param mass
     * @param x x position
     * @param y y position
     * @return the new body
     */
    private Body createBody(float mass, float x, float y) {
        Body body = createCircle(mass, x, y);

        //Texture texture = DynamicSprite.getRandomTexture(planetSkins);

        //Sprite basicSprite = DynamicSprite.getRandomSprite(planetSkins);

        TextureAtlas.AtlasRegion region = getRandomPlanetSkin();

        //Now we create a sprite (graphic) for the body
        DynamicSprite sprite = getDynamicSpriteFromTextureRegion(region); //the dynamic sprite class is optimized for use with bodies
        sprite.attachBody(body);

        sprite.createHighlight(bodyHighlight); //create the selection highlight
        sprite.setPosition(body.getPosition().x, body.getPosition().y);
        sprites.add(sprite);

        //attach the sprite to the body so it can be identified later
        body.setUserData(sprite);

        bodies.add(body);

        return body;
    }

    /**
     * This method destroys a given body
     * @param body The body to destroy
     */
    public void deleteBody(Body body) {
        int bodyIndex = bodies.indexOf(body);
        bodies.remove(body);
        world.destroyBody(body);
        sprites.get(bodyIndex).setAlpha(0); //set sprite to transparent
        sprites.remove(bodyIndex);

        if(bodyIndex == selectedBody) {
            selectedBody = -1;
        }

    }

    /**
     * This method deletes a body given its index
     * @param bodyIndex The index of the body to delete
     */
    public void deleteBody(int bodyIndex) {
        deleteBody(bodies.get(bodyIndex));
    }

    /**
     * This method deletes the current selected body
     */
    public void deleteSelectedBody() {
        if(isBodySelected()) {
            deleteBody(selectedBody);
        }
    }

    /**
     * This method deletes all bodies
     */
    public void deleteAllBodies() {
        for(int i = bodies.size() - 1; i >= 0; i--) {
            deleteBody(i);
        }
    }

    /**
     * This method creates an nxn matrix of bodies. All have mass "mass". The top-leftmost is located at (x,y)
     * @param mass
     * @param x
     * @param y
     * @param n
     * @return an arraylist with all the bodies in the matrix
     */
    private ArrayList<Body> createBodyMatrix(float mass, float x, float y, float n) {
        float radius = getCircleRadius(mass);
        ArrayList<Body> matrixBodies = new ArrayList<Body>();
        for(int i = 0; i < n; i++) {
            for(int j = 0; j < n; j++) {
                Body newBody = createBody(mass, x + radius * 2*i, y + radius * 2*j);
                matrixBodies.add(newBody);
            }
        }
        return matrixBodies;
    }

    public void setBodyMatrixN(int n) {
        bodyMatrixN = n;
    }

    /**
     * Make body "planet" orbit body "sun". This method uses the planet's radius from the sun and the
     * sun's mass to calculate what tangential velocity the planet needs to have to orbit the sun. It
     * then gives the planet that velocity.
     * @param planet
     * @param sun
     */
    private void orbit(Body planet, Body sun) {
        float sun_mass = sun.getMass();
        float distance = sun.getWorldCenter().dst(planet.getWorldCenter()) / SIZE_ADJUSTMENT_FACTOR;
        //mv^2/r = G(m1)(m2)/r^2
        float velocity_magnitude = (float)Math.sqrt(GRAVITY_CONSTANT * sun_mass / distance);

        Vector2 r = sun.getWorldCenter().sub(planet.getWorldCenter());

        Vector2 r_hat = r.nor();

        Vector2 v_hat = r_hat.rotate90(-1);

        Vector2 velocity = v_hat.scl(velocity_magnitude);

        planet.setLinearVelocity(velocity);

    }

    /**
     * This method creates a circlular body of mass "mass" and coordinates x,y
     * @param mass
     * @param x
     * @param y
     * @return The circular body
     */
    private Body createCircle(float mass, float x, float y) {

        //mass = mass * SIZE_ADJUSTMENT_FACTOR;

        // First we create a body definition
        BodyDef bodyDef = new BodyDef();
        // We set our body to dynamic so it can experience forces, for something like ground which doesn't move we would set it to StaticBody
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        // Set our body's starting position in the world
        bodyDef.position.set(x, y);

        // Create our body in the world using our body definition
        Body body = world.createBody(bodyDef);

        // Create a circle shape and set its radius
        CircleShape circle = new CircleShape();
        circles.add(circle); //add the circle to the circles arraylist so that it can be disposed of on close
        double radius = getCircleRadius(mass); //get the circle's radius based on mass. This function uses a mass-volume conversion
        circle.setRadius((float)radius);


        // Use the mass to calculate density based on radius
        double density = mass / (Math.PI * Math.pow(radius, 2));

        // Create a fixture definition to apply our shape to. This is what gives a body its properties
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.density = (float)density;
        fixtureDef.friction = FRICTION; //coefficient of friction on the body's surface

        // Create our fixture and attach it to the body
        body.createFixture(fixtureDef);


        return body;
    }


    /**
     * Gets the dynamic sprite for the given body
     * @param body
     * @return
     */
    public DynamicSprite getDynamicSprite(Body body) {
        int bodyIndex = bodies.indexOf(body);
        return sprites.get(bodyIndex);
    }


    /**
     * This method is used to set the selected body movable or not. If a body is movable, it can be
     * launched and is affected by gravity and collisions. If not, it's the opposite.
     * @param bool true = movable
     */
    public void setSelectedBodyMovable(boolean bool) {
        if (isBodySelected()) {
            setBodyMovable(getSelectedBody(), bool);
        }
    }

    /**
     * This method is used to set a body movable or not. If a body is movable, it can be
     * launched and is affected by gravity and collisions. If not, it's the opposite.
     * @param bool true = movable
     * @param body the body to adjust movable property
     */
    public void setBodyMovable(Body body, boolean bool) {
        getDynamicSprite(body).setMovable(bool);
    }

    /**
     * Returns whether a given body is movable.
     * @param body the body to check
     * @return movable = true
     */
    public boolean isBodyMovable(Body body) {
        return getDynamicSprite(body).isMovable();
    }

    /**
     * Returns whether the selected body is movable
     * @return movable = true
     */
    public boolean isSelectedBodyMovable() {
        if(isBodySelected())
            return isBodyMovable(getSelectedBody());
        return true;
    }

    /**
     * This method toggles the movable setting for the selected body
     */
    public void toggleSelectedBodyMovable() {
        setSelectedBodyMovable(!isSelectedBodyMovable());
    }

    public void dragForLaunchSimulation() {
        Vector2 releaseLocation = launchSimulation.getReleaseLocation();
        dragForLaunchSimulation(releaseLocation);
    }

    public void doLaunchSimulation(Vector2 launchVector) {
        //Simulate the launch for the body using the launch vector as it's new velocity.
        launchSimulation.doSimulation(getSelectedBody(), launchVector);
    }

    public void dragForLaunchSimulation(Vector2 releaseLocation) {

        Body body = getSelectedBody();

        launchSimulation.setReleaseLocation(new Vector2(releaseLocation.x, releaseLocation.y));

        Vector2 bodyLocation = body.getWorldCenter();

        Vector2 launchVector = new Vector2(releaseLocation).sub(bodyLocation); //create launch vector

        Gdx.app.log("GameScreen", "launchVector = (" + launchVector.x + "," + launchVector.y + ")");

        launchVector.scl(-1); //invert it so that the body launches away from the direction pulled. It's like pulling back a slingshot

        if(body.getMass() > STANDARD_MASS) {
            // scale the acceleration so that bigger bodies have lower accelerations for the same pull
            float scaleFactor = (float)(Math.pow(STANDARD_MASS/body.getMass(), 1/7.0));
            launchVector.scl(scaleFactor);
        }

        doLaunchSimulation(launchVector);
    }

    /**
     * This method calculates a circle's (or actually a sphere's) radius based on a given mass
     * @param mass
     * @return the radius
     */
    public float getCircleRadius(float mass) {
        return (float)(Math.pow(3*mass * SIZE_ADJUSTMENT_FACTOR/4/Math.PI/STANDARD_DENSITY, 1/3.0));
    }

    /**
     * This method calculates a circle's (sphere's) mass given a radius
     * @param radius
     * @return the mass
     */
    public float getCircleMass(float radius) {
        return (float)(4.0/3*Math.PI*Math.pow(radius, 3)*STANDARD_DENSITY/SIZE_ADJUSTMENT_FACTOR);
    }

    /**
     * This method changes a body (planet, sun, etc) mass by deltaMass
     * @param body The body whom's mass to change
     * @param deltaMass The amount to change it by
     */
    public void changeBodyMass(Body body, float deltaMass) {
        //get the fixture and shape of the body
        Fixture fixture = body.getFixtureList().get(0); //bodies can have multiple fixtures so the get() method returns a list. We want the fixture at index 0.
        Shape circle = fixture.getShape();
        float mass = body.getMass();

        mass = mass + deltaMass; //adjust mass

        //mass has a lower limit of 1 and an upper limit of 1,000,000 inclusive
        if (mass < MIN_BODY_MASS) {
            mass = MIN_BODY_MASS;
        } else if (mass > MAX_BODY_MASS) {
            mass = MAX_BODY_MASS;
        }

        setBodyMass(body, mass);

    }

    /**
     * This method sets the mass for a given body. It also scales the radius accordingly
     * @param body
     * @param mass
     */
    public void setBodyMass(Body body, float mass) {
        Fixture fixture = body.getFixtureList().get(0); //bodies can have multiple fixtures so the get() method returns a list. We want the fixture at index 0.
        Shape circle = fixture.getShape();

        //recalculate radius and density based on new mass
        double radius = getCircleRadius(mass);

        double density = mass / (Math.PI * Math.pow(radius, 2));

        //assign new properties
        fixture.setDensity((float) density);
        circle.setRadius((float) radius);

        //reset mass data of the body so that changes can take effect
        body.resetMassData();
    }


    private float accumulator = 0;

    /**
     * This method advances the world by TIMESTEP. It adjusts for lag.
     * @param deltaTime time elapsed since last call
     */
    private void doPhysicsStep(float deltaTime) {
        // fixed time step
        // max frame time to avoid spiral of death (on slow devices)
        float frameTime = Math.min(deltaTime, 0.25f);
        accumulator += frameTime;
        while (accumulator >= TIMESTEP) {
            world.step(TIMESTEP, 6, 2);
            accumulator -= TIMESTEP;
        }
        //world.step(deltaTime, 6, 2);

    }

    /**
     * This method selects a body so that it can be launched, scaled in size, followed with the camera, or given an orbit.
     * @param bodyIndex the index corresponding to the body in the bodies arraylist. -1 = no body selected
     */
    public void setSelectedBody(int bodyIndex) {
        if(selectedBody >= 0)
            sprites.get(selectedBody).setSelected(false);

        selectedBody = bodyIndex;

        if(selectedBody >= 0) {
            float mass = getSelectedBody().getMass();
            float radius = getCircleRadius(mass);
            hud.scaleSlider.setDisabled(false);
            hud.scaleSlider.setValue(radius);
            sprites.get(selectedBody).setSelected(true);
            hud.bodyInfoWindow.fillInfo(getDynamicSprite(getSelectedBody()));

        } else {
            hud.scaleSlider.setDisabled(true);
            hud.bodyInfoWindow.disable();

        }

        hud.updateStickyIcon();
    }

    /**
     * This method returns the selected body
     * @return
     */
    public Body getSelectedBody() {
        try {
            return bodies.get(selectedBody);
        } catch (IndexOutOfBoundsException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * This method sets the launching bool
     * @param bool
     */
    private void setLaunching(boolean bool) {
        launching = bool;
        if(!bool && !hud.isMenuOpen())
            launchSimulation.stopSimulation();
        //TODO Add visual
    }

    /**
     * Sets the addingBody bool. When true, users can click to add bodies.
     * @param bool
     */
    public void setAddingBody(boolean bool) {
        addingBody = bool;
        setSelectedBody(-1);
        //TODO Add visual
    }

    /**
     * Sets the addingBodyMatrix bool. When true, users can click to add matrices of bodies.
     * @param bool
     */
    public void setAddingBodyMatrix(boolean bool) {
        addingBodyMatrix = bool;
        setSelectedBody(-1);
        //TODO Add visual
    }


    /**
     * This method returns a boolean saying whether any body is selected (true) or not (false)
     * @return
     */
    public boolean isBodySelected() {
        return selectedBody != -1;
    }

    /**
     * This method sets the boolean pickingOrbit. This is true when the user has a selected body and they are in the process of
     * selecting another body for that body to orbit
     * @param bool
     */
    public void setPickingOrbit(boolean bool) {
        pickingOrbit = bool;
        //TODO: Add graphics to support picking orbit
    }

    /**
     * This method applies gravity the gravity from body1 to body2. It uses newton's law with G = 1.
     * @param body1
     * @param body2
     */
    public void applyGravityBetweenBodies(Body body1, Body body2) {
        float m1 = body1.getMass();
        float m2 = body2.getMass();
        Vector2 r = body1.getWorldCenter().sub(body2.getWorldCenter());

        //get r magnitude. It is important to get this before getting r_hat because calling r.nor() will actually normalize the r vector, not just return r_hat
        float r_mag = r.len() / (float)Math.sqrt(SIZE_ADJUSTMENT_FACTOR);

        //get r unit vector
        Vector2 r_hat = r.nor();

        //F = G(m1)(m2) / ||r||^2 * r_hat
        Vector2 f = r_hat.scl((float)(GRAVITY_CONSTANT * m1 * m2 / Math.pow(r_mag, 2)));

        //apply the force to body2
        body2.applyForceToCenter(f.x, f.y, true);

    }

    /**
     * This method scales vector screenPosition to get a vector representing its WORLD coordinates.
     * This is important because when registering locations of inputs (taps, pans, pinches, etc) the
     * functions give the position on the SCREEN in pixels instead of the scaled position in our Box2D
     * world.
     * @param screenPosition
     * @return
     */
    public Vector2 getWorldPosition(Vector2 screenPosition) {
        Vector3 pos = new Vector3(screenPosition, 0);
        camera.unproject(pos);
        Vector2 worldPosition = new Vector2(pos.x, pos.y);
        return worldPosition;
    }

    /**
     * This method performs the opposite of getWorldPosition(Vector2 screenPosition). It takes a
     * screen position as input and returns the world position. This is used to place sprites in the
     * correct location in the world (sprites are placed using screen coordinates).
     * @param worldPosition
     * @return
     */
    public Vector2 getScreenPosition(Vector2 worldPosition) {
        Vector3 pos = new Vector3(worldPosition, 0);
        camera.project(pos);
        Vector2 screenPosition = new Vector2(pos.x, pos.y);
        return screenPosition;
    }

    /**
     * This method scales a given screen distance to the world distance. The screen (measured in pixels)
     * has a different scale than the box2d world (meters)
     * @param distance
     * @return
     */
    public float scaleDistanceToWorld(float distance) {
        return distance * camera.viewportHeight*(1/getZoom() / screenHeight);
    }

    /**
     * This method scales a given world distance to the screen distance. The screen (measured in pixels)
     * has a different scale than the box2d world (meters)
     * @param distance
     * @return
     */
    public float scaleDistanceToScreen(float distance) {
        return distance * screenHeight / camera.viewportHeight*(1/getZoom()); //zoom needs to be accounted for
    }

    /**
     * This method runs continuously in an endless loop while the game is running.
     * @param delta time elapsed since last call. Used for frames
     */
    @Override
    public void render(float delta) {
        // clear the screen with a dark blue color. The
        // arguments to glClearColor are the red, green
        // blue and alpha component in the range [0,1]
        // of the color to be used to clear the screen.
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


        //calling this method checks to see if the user is zooming. We want it to be a continuous zoom.
        handleZoom();


        //make camera follow selected planet if chaseCamOn
        if(isBodySelected() && chaseCamOn)
            centerCamera(bodies.get(selectedBody).getWorldCenter());

        // tell the camera to update its matrices.
        camera.update();

        //if the game is running
        if(running) {
            //for every body, apply gravity between itself and all the other bodies
            for (Body body1 : bodies) {

                    for (Body body2 : bodies) {
                        if (body1 != body2 && getDynamicSprite(body2).isMovable()) { //don't apply a body's own gravity to itself.
                            applyGravityBetweenBodies(body1, body2);
                        }
                    }

            }
        }

        //debugRenderer.render(world, camera.combined);

        //Gdx.app.log("GameScreen", "delta = " + delta);

        //update all the sprites
        batch.begin();
        batch.setProjectionMatrix(camera.combined);
        for(DynamicSprite sprite : sprites) {
            sprite.update();
            sprite.draw(batch);
            sprite.highlight.draw(batch);
        }
        batch.end();


        //advance world by TIMESTEP (1/60 second)
        doPhysicsStep(timeStep);


        //update launch simulation
        if(launching && isBodySelected()) {
            dragForLaunchSimulation();
        }
        launchSimulation.draw();

        //update HUD
        hud.act(TIMESTEP);
        hud.draw();


    }


    /**
     * This method is called when a user resizes the game on their computer. It adjusts the world dimensions
     * to maintain the right ratio
     * @param width
     * @param height
     */
    @Override
    public void resize(int width, int height) {
        screenWidth = Gdx.graphics.getWidth();
        screenHeight = Gdx.graphics.getHeight();

        worldWidth = worldHeight / screenHeight * screenWidth;

        camera.setToOrtho(false, worldWidth, worldHeight);
        centerCamera(); //put the sun at the middle

        //resize hud
        hud.getViewport().update(width, height, true);

    }

    @Override
    public void show() {
        // start the playback of the background music
        // when the screen is shown
        //rainMusic.play();
    }

    @Override
    public void hide() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    /**
     * This method disposes of all visual elements when the game is closed
     */
    @Override
    public void dispose() {
        //dispose of circle shapes
        for (CircleShape circle : circles) {
            circle.dispose();
        }

        earth.dispose();
        bodyHighlight.dispose();
        hud.uiSkin.dispose();
    }

    /**
     * This method is called when a user presses down on a key
     * @param keycode
     * @return
     */
    @Override
    public boolean keyDown (int keycode) {
        Gdx.app.log("GameScreen", "keyDown registered - keycode = " + keycode);

        if (keycode == Input.Keys.O) {
            //If the O key is pressed and a body is selected, turn on orbit selection. Now the next body tapped will be orbited by the selected planet
            if (isBodySelected()) {
                if(!pickingOrbit) {
                    setPickingOrbit(true);
                } else {
                    setPickingOrbit(false);
                }
            }
        }

        if (keycode == Input.Keys.C) {
            //If the C key is pressed, center camera and reset zoom
            resetCamera();
        }

        if (keycode == Input.Keys.F) {
            //If the F key is pressed, toggle camera following selected planet
            setChaseCamOn(!chaseCamOn);
        }

        if (keycode == Input.Keys.N) {
            //If the N key is pressed, toggle adding new bodies
            setAddingBody(!addingBody);
            setAddingBodyMatrix(false);
            setSelectedBody(-1);
        }

        if (keycode == Input.Keys.M) {
            //If the M key is pressed, toggle adding matrices of bodies
            setAddingBodyMatrix(!addingBodyMatrix);
            setAddingBody(false);
            setSelectedBody(-1);
        }

        if (keycode == Input.Keys.P) {
            //If the P key is pressed, toggle playing/pausing of game
            if(running) {
                pauseGame();
            } else {
                unpauseGame();
            }
        }

        if (keycode == Input.Keys.D) {
            //If the D key is pressed, delete the selected body

            deleteSelectedBody();

        }

        if (keycode == Input.Keys.T) {
            //If the T key is pressed, toggle the selected body's movability

            toggleSelectedBodyMovable();
        }


        return false;
    }

    @Override
    public boolean keyUp (int keycode) {
        Gdx.app.log("GameScreen", "keyUp registered - keycode = " + keycode);

        return false;
    }

    @Override
    public boolean keyTyped (char character) {
        Gdx.app.log("GameScreen", "keyTyped registered - char = " + character);

        return false;
    }


    /**
     * This method is called when the user presses down on the screen or clicks.
     * @param x
     * @param y
     * @param pointer
     * @param button
     * @return
     */
    @Override
    public boolean touchDown (int x, int y, int pointer, int button) {

        Vector2 touchLocation = new Vector2(x, y);

        touchLocation = getWorldPosition(touchLocation); //adjust touch location to world coordinates

        Gdx.app.log("GameScreen", "touchDown registered at (" + touchLocation.x + "," + touchLocation.y + ")");

        float shortestDistance = Integer.MAX_VALUE;
        int tappedBody = -1;
        //check to see if any of the bodies were tapped.
        for(int i = 0; i < bodies.size(); i++) {
            Body body = bodies.get(i);
            Vector2 bodyCenter = body.getWorldCenter();
            float bodyRadius = body.getFixtureList().get(0).getShape().getRadius();
            float touchDistanceFromBodyCenter = Math.abs(bodyCenter.dst(touchLocation)); //get touch distance from the center of the body
            //the tap is considered to be inside the body if it is within the radius or 50 world units
            boolean tapInsideBody = touchDistanceFromBodyCenter < bodyRadius || touchDistanceFromBodyCenter < 10 * getZoom();
            if(tapInsideBody && touchDistanceFromBodyCenter < shortestDistance) { //we want to find the closest body to our tap in case multiple bodies are in range
                tappedBody = i;
                shortestDistance = touchDistanceFromBodyCenter;
            }
        }
        //if a body was tapped select the tapped body (this is the closest one to the tap)
        if(tappedBody >= 0 && !launching && selectedBody == tappedBody) {
            setLaunching(true);
            launchSimulation.setReleaseLocation(getSelectedBody().getWorldCenter());
        }
        return false;
    }

    /**
     * called when a user releases their finger from the screen
     * @param x
     * @param y
     * @param pointer
     * @param button
     * @return
     */
    @Override
    public boolean touchUp (int x, int y, int pointer, int button) {

        //if the user is currently launching a planet (their touch down was on a planet), launch the planet
        if(launching && isBodySelected()) {
            Body body = bodies.get(selectedBody);
            Vector2 releaseLocation = new Vector2(x, y);
            releaseLocation = getWorldPosition(releaseLocation); //convert release location to world coords

            Gdx.app.log("GameScreen", "touchUp registered at (" + releaseLocation.x + "," + releaseLocation.y + ")");

            Gdx.app.log("GameScreen", "launch release location = (" + x + "," + y + ")");
            Vector2 bodyLocation = body.getWorldCenter();
            Gdx.app.log("GameScreen", "body center = (" + bodyLocation.x + "," + bodyLocation.y + ")");

            Vector2 launchVector = releaseLocation.sub(bodyLocation); //create launch vector
            Gdx.app.log("GameScreen", "launchVector = (" + launchVector.x + "," + launchVector.y + ")");

            launchVector.scl(-1); //invert it so that the body launches away from the direction pulled. It's like pulling back a slingshot

            if(body.getMass() > STANDARD_MASS) {
                // scale the acceleration so that bigger bodies have lower accelerations for the same pull
                float scaleFactor = (float)(Math.pow(STANDARD_MASS/body.getMass(), 1/7.0));
                launchVector = launchVector.scl(scaleFactor);
            }

            //assign launch vector as new velocity for the body
            body.setLinearVelocity(launchVector);

            doLaunchSimulation(launchVector);

            setLaunching(false);

        }



        return false;
    }

    @Override
    public boolean touchDragged (int x, int y, int pointer) {
        //Gdx.app.log("GameScreen", "touchDragged registered");

        return false;
    }

    @Override
    public boolean mouseMoved (int x, int y) {
        //Gdx.app.log("GameScreen", "mouseMoved registered");

        return false;
    }

    /**
     * Called on scrolling of the mouse wheel
     * @param amount
     * @return
     */
    @Override
    public boolean scrolled (int amount) {
        Gdx.app.log("GameScreen", "scroll registered - amount = " +  amount);

        //TODO scale mass of selected body

        /*if(Gdx.app.getType() == Application.ApplicationType.WebGL) {
            amount *= 0.05f;
        }*/

        amount *= 2;

        if(isBodySelected()) {

            Body body = bodies.get(selectedBody);

            float scaled_amount = (float) Math.sqrt(body.getMass()) * amount;

            scaled_amount *= 10;

            changeBodyMass(body, scaled_amount);

            Gdx.app.log("GameScreen", "Mass adjusted - mass = " + body.getMass());
        } else {

            setZoom(getZoom() + amount * 0.1f);
        }


        return false;
    }

    @Override
    public boolean touchDown (float x, float y, int pointer, int button) {
        Gdx.app.log("GameScreen", "touchDown registered");
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {

        Vector2 touchLocation = new Vector2(x, y);

        touchLocation = getWorldPosition(touchLocation); //adjust touch location to world coordinates

        Gdx.app.log("GameScreen", "tap registered at (" + touchLocation.x + "," + touchLocation.y + ")");

        float shortestDistance = Integer.MAX_VALUE;
        int tappedBody = -1;
        if(addingBody || addingBodyMatrix) {
            //check to see if any of the current bodies were tapped.
            for (int i = 0; i < bodies.size(); i++) {
                Body body = bodies.get(i);
                Vector2 bodyCenter = body.getWorldCenter();
                float bodyRadius = body.getFixtureList().get(0).getShape().getRadius();
                float touchDistanceFromBodyCenter = Math.abs(bodyCenter.dst(touchLocation)); //get touch distance from the center of the body
                //the tap is considered to be inside the body if it is within the radius
                if(touchDistanceFromBodyCenter < bodyRadius) {
                    tappedBody = i;
                    break;
                }
            }
            //if a body was tapped, tappedBody is the index of the tapped body in the bodies arraylist
            if (tappedBody < 0) { //if not tappedBody = -1
                if(addingBody) {
                    //add new body at the tap location with STANDARD_MASS
                    createBody(STANDARD_MASS, touchLocation.x, touchLocation.y);
                    setSelectedBody(bodies.size() - 1); //select the new body
                } else if (addingBodyMatrix) {
                    //add new body at the tap location with STANDARD_MASS
                    createBodyMatrix(STANDARD_MASS, touchLocation.x, touchLocation.y, bodyMatrixN);
                    setSelectedBody(-1); //select no body
                }
            }
        } else {
            //check to see if any of the bodies were tapped.
            for (int i = 0; i < bodies.size(); i++) {
                Body body = bodies.get(i);
                Vector2 bodyCenter = body.getWorldCenter();
                float bodyRadius = body.getFixtureList().get(0).getShape().getRadius();
                float touchDistanceFromBodyCenter = Math.abs(bodyCenter.dst(touchLocation)); //get touch distance from the center of the body
                //the tap is considered to be inside the body if it is within the radius or 50 world units
                boolean tapInsideBody = touchDistanceFromBodyCenter < bodyRadius || touchDistanceFromBodyCenter < 10 * getZoom();
                if (tapInsideBody && touchDistanceFromBodyCenter < shortestDistance) { //we want to find the closest body to our tap in case multiple bodies are in range
                    tappedBody = i;
                    shortestDistance = touchDistanceFromBodyCenter;
                }
            }
            //if a body was tapped, tappedBody is the index of the tapped body in the bodies arraylist
            if (tappedBody >= 0) {
                //if the user is selecting an orbit for the already selected planet
                if (pickingOrbit) {
                    //if they didn't tap the body already selected
                    if (tappedBody != selectedBody) {
                        //make that selected planet orbit the tapped body
                        orbit(getSelectedBody(), bodies.get(tappedBody));
                        doLaunchSimulation(getSelectedBody().getLinearVelocity());
                        if(hud.getCurrentClickFunction() == HUD.ClickFunction.ORBIT)
                            hud.deactivateClickFunction(HUD.ClickFunction.ORBIT);

                    }
                    // if they just picked an orbit or they tapped the planet already selected, turn off orbit selecting
                    setPickingOrbit(false);
                } else {
                    //if they aren't selecting an orbit, make the tapped body the new globally selected body
                    setSelectedBody(tappedBody);
                }
            } else {
                //if they didn't select a planet
                if (pickingOrbit) {
                    //if picking orbit, stop picking orbit but keep the current planet selected
                    setPickingOrbit(false);
                } else {
                    //otherwise, deselect the planet. The player can now zoom and pan around
                    setSelectedBody(-1);
                }

            }
        }



        return false;
    }

    @Override
    public boolean longPress(float x, float y) {
        Gdx.app.log("GameScreen", "longPress registered");
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        Gdx.app.log("GameScreen", "fling registered");

        return false;
    }

    /**
     * This method is called when a user drags their finger/mouse across the screen after tapping/clicking down without releasing
     * @param x
     * @param y
     * @param deltaX
     * @param deltaY
     * @return
     */
    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        Gdx.app.log("GameScreen", "pan registered");
        //if the user isn't currently launching a planet, we want them to be able to pan around the world
        if(!launching) {
            //convert pan's screen coordinates to world coordinates
            Vector2 pos = new Vector2(x,y);
            pos = getWorldPosition(pos);
            Vector2 pos0 = new Vector2(x-deltaX,y-deltaY);
            pos0 = getWorldPosition(pos0);

            //create pan vector
            Vector2 delta = pos.sub(pos0).scl(-1);
            translateCamera(delta); // pan camera
        } else { //if a body is being launched, create a simulation for the launch and the body's movement
            if(isBodySelected()) {
                Body body = getSelectedBody();

                Vector2 screenReleaseLocation = new Vector2(x, y);

                Vector2 releaseLocation = getWorldPosition(screenReleaseLocation);

                dragForLaunchSimulation(releaseLocation);
            }
        }
        return false;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        Gdx.app.log("GameScreen", "panStop registered");
        return false;
    }

    @Override
    public boolean zoom (float originalDistance, float currentDistance){
        Gdx.app.log("GameScreen", "zoom registered");
        return false;
    }

    /**
     * Called when a user pinches their fingers on their touch screen
     * @param initialFirstPointer
     * @param initialSecondPointer
     * @param firstPointer
     * @param secondPointer
     * @return
     */
    @Override
    public boolean pinch (Vector2 initialFirstPointer, Vector2 initialSecondPointer, Vector2 firstPointer, Vector2 secondPointer){
        Gdx.app.log("GameScreen", "pinch registered");

        //TODO: Fix this up BIG TIME

        float initialDistance = Math.abs(initialFirstPointer.dst(initialSecondPointer));
        float finalDistance = Math.abs(firstPointer.dst(secondPointer));

        float changeInDistance = finalDistance - initialDistance;

        changeBodyMass(planet, changeInDistance);

        setLaunching(false); //make sure this scaling isn't registered as a launch

        return false;
    }

    @Override
    public void pinchStop() {

    }

}
