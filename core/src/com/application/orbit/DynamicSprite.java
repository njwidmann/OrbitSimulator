package com.application.orbit;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Shape;

/**
 * Created by Nick on 4/19/2016.
 */
public class DynamicSprite extends Sprite {
    Body physicsBody;
    boolean selected;
    BodyHighlight highlight;
    public enum Type {BODY, HIGHLIGHT}
    Type type;
    boolean movable;
    String name;

    public static int bodyNum = 0;

    /**
     * The dynamic sprite class is an extension to the sprite class that includes a box2d body
     * @param texture
     */
    public DynamicSprite(Texture texture) {
        super(texture);

        this.type = Type.BODY;

        setMovable(true);

    }


    public DynamicSprite(Texture texture, Body body) {
        this(texture);
        bodyNum++;
        this.name = "Body_"+bodyNum;

        attachBody(body);

    }

    public DynamicSprite(TextureAtlas.AtlasRegion texture) {
        super(texture);
        bodyNum++;
        this.name = "Body_"+bodyNum;

        this.type = Type.BODY;

        setMovable(true);

    }


    public boolean isMovable() {
        return movable;
    }

    /**
     * sets whether the body will be affected by gravity, if it will move from collisions, and whether
     * it can be launched
     * @param bool
     */
    public void setMovable(boolean bool) {
        movable = bool;
        if(physicsBody != null) {
            if (bool) {
                physicsBody.setLinearDamping(0);
            } else {
                physicsBody.setLinearDamping(10000000);
            }
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void createHighlight(Texture texture) {
        highlight = new BodyHighlight(texture);
        highlight.showHighlight(false);
        highlight.attachBody(physicsBody);
    }

    public void setSelected(boolean bool) {
        selected = bool;
    }
    /**
     * This method can be called to attach a body to the sprite
     * @param body
     */
    public void attachBody(Body body) {
        this.physicsBody = body;
    }

    /**
     * This method returns the physics body attached to the sprite
     * @return
     */
    public Body getBody() {
        return physicsBody;
    }

    /**
     * This method returns the nth fixture attached to the sprite's body. A fixture stores most of the properties of the body
     * @param n
     * @return
     */
    public Fixture getFixture(int n) {
        return physicsBody.getFixtureList().get(n); //bodies can have multiple fixtures so the get() method returns a list. We want the fixture at index n.
    }

    /**
     * This method returns the default fixture (0)
     * @return
     */
    public Fixture getFixture() {
        return getFixture(0);
    }

    /**
     * This method returns the shape object attached to the body. This is used for handling collisions.
     * @return
     */
    public Shape getShape() {
        return getFixture().getShape();
    }

    /**
     * This update method must be called in the render loop in the running screen class. It updates the sprite
     * to match the properties of the body
     */
    public void update() {
        Vector2 position = getBody().getWorldCenter();
        float radius = getShape().getRadius();
        setSize(radius * 2, radius * 2);
        setPositionCenter(position.x, position.y);
        setOrigin(getWidth() / 2, getHeight() / 2);
        setRotation((float)Math.toDegrees(physicsBody.getAngle()));

        if (selected) {
            highlight.showHighlight(true);
            highlight.update();
        } else {
            highlight.showHighlight(false);
        }

    }

    /**
     * This method is used to set the center of a sprite at a given (x,y) coordinates
     * @param x
     * @param y
     */
    public void setPositionCenter(float x, float y) {
        float width = getWidth();
        float height = getHeight();
        setPosition(x-width/2, y-height/2);
    }


}
