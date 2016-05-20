package com.application.orbit;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by Nick on 4/28/2016.
 */
public class BodyHighlight extends DynamicSprite {



    public BodyHighlight(Texture texture) {
        super(texture);
        this.type = Type.HIGHLIGHT;
    }

    public void showHighlight(boolean bool) {
        if(bool) {
            setAlpha(1);
        } else {
            setAlpha(0);
        }
    }

    @Override
    public void update() {
        Vector2 position = getBody().getWorldCenter();
        float radius = getShape().getRadius();
        setSize(radius * 2.5f, radius * 2.5f);
        setPositionCenter(position.x, position.y);
        setOrigin(getWidth() / 2, getHeight() / 2);
        setRotation((float)Math.toDegrees(physicsBody.getAngle()));
    }
}
