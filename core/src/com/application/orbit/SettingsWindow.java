package com.application.orbit;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

/**
 * Created by Nick on 5/3/2016.
 */
public class SettingsWindow extends Window {

    Slider launchSimulationSlider, matrixSizeSlider;
    CheckBox dynamicLaunchSimulatorCheckBox, bodyFusionCheckBox;
    TextButton exitButton, saveButton;
    GameScreen gameScreen;
    DigitFilter digitFilter;

    public SettingsWindow(String name, Skin skin, final GameScreen gameScreen) {
        super(name, skin);

        this.gameScreen = gameScreen;

        setPosition(0, 0);
        defaults().spaceBottom(10);

        digitFilter = new DigitFilter();

        exitButton = new TextButton("X", skin);
        exitButton.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {

                return true;
            }

            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                closeWindow();
            }
        });


        final Label launchSimulationLabel = new Label("Launch Simulation Length: ", skin);
        int maxDots = LaunchSimulation.MAX_NUM_DOTS;
        int minDots = LaunchSimulation.MIN_NUM_DOTS;
        launchSimulationSlider = new Slider(minDots, maxDots, 1, false, skin);
        launchSimulationSlider.setDebug(false);
        launchSimulationSlider.setValue(LaunchSimulation.DEFAULT_NUM_DOTS);
        launchSimulationSlider.addListener(new ChangeListener() {
            public void changed (ChangeEvent event, Actor actor) {
                updateSimulationSize();
            }
        });

        dynamicLaunchSimulatorCheckBox = new CheckBox(" Dynamic Simulation", skin);
        dynamicLaunchSimulatorCheckBox.setChecked(true);
        dynamicLaunchSimulatorCheckBox.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                setDynamicLaunchActivated(dynamicLaunchSimulatorCheckBox.isChecked());
            }
        });

        bodyFusionCheckBox = new CheckBox(" Body Fusion", skin);
        bodyFusionCheckBox.setChecked(false);
        bodyFusionCheckBox.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                gameScreen.setBodyFusionActivated(bodyFusionCheckBox.isChecked());
            }
        });

        final Label matrixSizeLabel = new Label("Body Matrix Size: ", skin);
        int minSize = GameScreen.MIN_BODY_MATRIX_N;
        int maxSize = GameScreen.MAX_BODY_MATRIX_N;
        matrixSizeSlider = new Slider(minSize, maxSize, 1, false, skin);
        matrixSizeSlider.setDebug(false);
        matrixSizeSlider.setValue(GameScreen.DEFAULT_BODY_MATRIX_N);
        matrixSizeSlider.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                updateMatrixSize();
            }
        });

        saveButton = new TextButton("OK", skin);
        saveButton.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {

                return true;
            }

            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                closeWindow();
            }
        });


        getTitleTable().add(exitButton).height(getPadTop());

        row();
        add(launchSimulationLabel).right();
        row();
        add(launchSimulationSlider).minWidth(HUD.SETTINGS_WINDOW_WIDTH - 50);
        row();
        add(dynamicLaunchSimulatorCheckBox);
        row();
        add(bodyFusionCheckBox);
        row();
        add(matrixSizeLabel);
        row();
        add(matrixSizeSlider).minWidth(HUD.SETTINGS_WINDOW_WIDTH - 50);
        row();
        add(saveButton).minWidth(200).colspan(4).center();



    }



    public void setDynamicLaunchActivated(boolean bool) {
        gameScreen.launchSimulation.setDynamicLaunchActivated(bool);

        gameScreen.launchSimulation.updateSimulation();
    }

    public void updateSimulationSize() {
        gameScreen.launchSimulation.setNumDots((int) (launchSimulationSlider.getValue()));

        gameScreen.launchSimulation.updateSimulation();
    }

    public void updateMatrixSize() {
        gameScreen.setBodyMatrixN((int)matrixSizeSlider.getValue());
    }

    public void closeWindow() {
        setVisible(false);
        if(gameScreen.hud.getCurrentClickFunction() == HUD.ClickFunction.SETTINGS) {
            gameScreen.hud.deactivateClickFunction(HUD.ClickFunction.SETTINGS);
        }
    }




}
