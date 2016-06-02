package com.application.orbit;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;

/**
 * This class is used to check for contact between bodies. If two bodies collide the bigger one absorbs the smaller.
 * Created by Nick on 6/2/2016.
 */
public class BodyContactListener implements ContactListener {

    GameScreen gameScreen;
    boolean bodyFusionActivated;

    public BodyContactListener(GameScreen gameScreen) {
        super();

        this.gameScreen = gameScreen;
        bodyFusionActivated = false;
    }

    public void setBodyFusionActivated(boolean bool) {
        bodyFusionActivated = bool;
    }

    public boolean isBodyFusionActivated() {
        return bodyFusionActivated;
    }

    @Override
    public void beginContact(Contact contact) {

        Fixture fixtureA = contact.getFixtureA();
        Body bodyA = fixtureA.getBody();
        float massA = bodyA.getMass();

        Fixture fixtureB = contact.getFixtureB();
        Body bodyB = fixtureB.getBody();
        float massB = bodyB.getMass();

        if(isBodyFusionActivated()) {

            if (massA > massB) {
                gameScreen.deleteBody(bodyB);
                massA += massB;
                gameScreen.setBodyMass(bodyA, massA);
            } else {
                gameScreen.deleteBody(bodyA);
                massB += massA;
                gameScreen.setBodyMass(bodyB, massB);
            }

        }


    }

    @Override
    public void endContact(Contact contact) {
        /*Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();
        Gdx.app.log("endContact", "between " + fixtureA.toString() + " and " + fixtureB.toString());*/
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
    }
}
