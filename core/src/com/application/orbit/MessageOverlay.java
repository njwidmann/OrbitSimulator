package com.application.orbit;

import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

/**
 * Created by Nick on 5/22/2016.
 */
public class MessageOverlay extends Table {

    Label tipLabel, bodyNameLabel;
    Skin skin;
    float FADEOUT_TIME = 3f;

    public enum TipMessage {
        NONE, SELECT_BODY, ORBIT
    }

    public MessageOverlay(Skin skin) {
        super(skin);

        this.skin = skin;

        tipLabel = new Label("", skin);
        tipLabel.setFontScale(1f);
        bodyNameLabel = new Label("", skin);
        bodyNameLabel.setFontScale(0.7f);

        add(bodyNameLabel);
        row();
        add(tipLabel);

        setFillParent(true);
        center();
        top();

    }

    public void showTip(TipMessage tip) {
        switch(tip) {
            case NONE:
                break;
            case SELECT_BODY:
                showTip("Select a body to edit");
                break;
            case ORBIT:
                showTip("Select a body to orbit");
                break;

        }
    }

    public void showTip(String tip) {
        tipLabel.setText(tip);
        tipLabel.addAction(Actions.alpha(1));
        tipLabel.addAction(Actions.alpha(0, FADEOUT_TIME));
    }

    public void showBodyName(String name) {
        bodyNameLabel.setText(name);
        bodyNameLabel.setVisible(true);
    }

    public void hideBodyName() {
        bodyNameLabel.setVisible(false);
    }
}
