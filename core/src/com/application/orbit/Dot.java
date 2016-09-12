package com.application.orbit;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

/**
 * Dot is used in the launch simulation class to show the predicted path of a body.
 * Created by Nick on 4/28/2016.
 */
public class Dot extends Image {

    /**
     * Dot extends the Image class so it takes in a Texture. This texture is the graphic asset that
     * is used to represent the simulation dot
     * @param texture the Texture object for the dot
     */
    public Dot(Texture texture) {
        super(texture);
    }

    /**
     * This method is used to set the center of the dot at a given (x,y) coordinates
     * @param x the center x position
     * @param y the center y position
     */
    public void setPositionCenter(float x, float y) {
        float width = getWidth();
        float height = getHeight();
        setPosition(x - width / 2, y - height / 2);
    }
}
