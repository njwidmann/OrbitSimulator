package com.application.orbit;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;

/**
 * Created by jyoo9_000 on 5/13/2016.
 * Modified by Nick on 5/22/2016
 */
public class HelpWindow extends Window {

    TextButton exitButton, saveButton;
    GameScreen gameScreen;
    DigitFilter digitFilter;
    //ScrollPane scroll;

    public HelpWindow(String name, Skin skin, GameScreen gameScreen) {
        super(name, skin);

        this.gameScreen = gameScreen;

        setPosition(0, 0);
        defaults().spaceBottom(10);

        digitFilter = new DigitFilter();

        Table scrollTable = new Table(skin);

        final Label gravConstLabel = new Label("Gravitational Constant (G) = 1", skin);
        final Label moreLabel = new Label("More Coming Soon", skin);


        exitButton = new TextButton("X", skin);
        exitButton.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {

                return true;
            }

            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                closeWindow();
            }
        });


        saveButton = new TextButton("Close", skin);
        saveButton.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {

                return true;
            }

            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

                closeWindow();
            }
        });


        getTitleTable().add(exitButton).height(getPadTop());

        scrollTable.add(gravConstLabel);
        scrollTable.row();
        scrollTable.add(moreLabel);

        //scroll = new ScrollPane(scrollTable, skin);
        add(scrollTable).minWidth(200).colspan(4).center();

        row();

        add(saveButton).minWidth(200).colspan(4).center();


    }

    public void closeWindow() {
        setVisible(false);
        if(gameScreen.hud.getCurrentClickFunction() == HUD.ClickFunction.HELP) {
            gameScreen.hud.deactivateClickFunction(HUD.ClickFunction.HELP);
        }
    }

}

