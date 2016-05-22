package com.application.orbit;

import com.application.orbit.GameScreen;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;

/**
 * Created by jyoo9_000 on 5/13/2016.
 * Modified by Nick on 5/22/2016
 */
public class AppInfoWindow extends Window {

    TextButton exitButton, saveButton;
    GameScreen gameScreen;
    DigitFilter digitFilter;
    //ScrollPane scroll;

    public AppInfoWindow(String name, Skin skin, GameScreen gameScreen) {
        super(name, skin);

        this.gameScreen = gameScreen;

        setPosition(0, 0);
        defaults().spaceBottom(10);

        digitFilter = new DigitFilter();

        Table scrollTable = new Table(skin);

        final Label titleLabel = new Label("Orbit Simulator", skin);
        titleLabel.setFontScale(1.5f);
        final Label schoolLabel = new Label("Drexel University", skin);
        schoolLabel.setFontScale(1.2f);
        final Label yearLabel = new Label("2016", skin);
        yearLabel.setFontScale(1.2f);
        final Label developerLabel = new Label("Developers:", skin);
        developerLabel.setFontScale(1.2f);

        final Label developersListLabel = new Label(
                "     Nick Widmann\n" +
                "     Ebed Jarrell\n" +
                "     Jiho Yoo", skin);


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

        scrollTable.add(titleLabel).padTop(0).padBottom(0);
        scrollTable.row();
        scrollTable.add(schoolLabel).padTop(0).padBottom(0);
        scrollTable.row();
        scrollTable.add(yearLabel).padTop(0).padBottom(0);
        scrollTable.row();
        scrollTable.add(developerLabel);
        scrollTable.row();
        scrollTable.add(developersListLabel);

        //scroll = new ScrollPane(scrollTable, skin);
        add(scrollTable).minWidth(200).colspan(4).center();

        row();

        add(saveButton).minWidth(200).colspan(4).center();


    }

    public void closeWindow() {
        setVisible(false);
        if(gameScreen.hud.getCurrentClickFunction() == HUD.ClickFunction.APP_INFO) {
            gameScreen.hud.deactivateClickFunction(HUD.ClickFunction.APP_INFO);
        }
    }

}

