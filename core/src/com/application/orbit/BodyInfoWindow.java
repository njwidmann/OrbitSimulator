package com.application.orbit;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

/**
 * Created by Nick on 5/3/2016.
 */
public class BodyInfoWindow extends Window {

    TextField positionXField;
    TextField positionYField;
    TextField velocityXField;
    TextField velocityYField;
    TextField bodyNameField;
    TextField massField;
    TextButton exitButton, saveButton;
    GameScreen gameScreen;
    DigitFilter digitFilter;

    public BodyInfoWindow(String name, Skin skin, final GameScreen gameScreen) {
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



        final Label bodyNameLabel = new Label("Name: ", skin);
        bodyNameField = new TextField("", skin);
        addTextFieldListener(bodyNameField, "Name");


        final Label positionXLabel = new Label("Position X: ", skin);
        final Label positionYLabel = new Label("Position Y: ", skin);
        positionXField = new TextField("", skin);
        positionXField.setTextFieldFilter(digitFilter);
        addTextFieldListener(positionXField, "Position X");
        positionYField = new TextField("", skin);
        positionYField.setTextFieldFilter(digitFilter);
        addTextFieldListener(positionYField, "Position Y");

        final Label velocityXLabel = new Label("Velocity X: ", skin);
        final Label velocityYLabel = new Label("Velocity Y: ", skin);
        velocityXField = new TextField("", skin);
        velocityXField.setTextFieldFilter(digitFilter);
        addTextFieldListener(velocityXField, "Velocity X");
        velocityYField = new TextField("", skin);
        velocityYField.setTextFieldFilter(digitFilter);
        addTextFieldListener(velocityYField, "Velocity Y");

        final Label massLabel = new Label("Mass: ", skin);
        massField = new TextField("", skin);
        massField.setTextFieldFilter(digitFilter);
        addTextFieldListener(massField, "Mass");

        saveButton = new TextButton("Save", skin);
        saveButton.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {

                return true;
            }

            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if(gameScreen.isBodySelected())
                    saveSettings();
                closeWindow();
            }
        });


        getTitleTable().add(exitButton).height(getPadTop());

        row();
        add(bodyNameLabel).right();
        add(bodyNameField).minWidth(300).expandX().fillX().colspan(3);

        row();
        /*add(positionLabel).colspan(3).center();
        row().fill().expandX();*/
        add(positionXLabel).right();
        add(positionXField).minWidth(300).expandX().fillX().colspan(3);
        row();
        add(positionYLabel).right();
        add(positionYField).minWidth(300).expandX().fillX().colspan(3);

        /*row().fill().expandX();
        add(velocityLabel).colspan(3).center();*/
        row();//.expandX();
        add(velocityXLabel).right();
        add(velocityXField).minWidth(300).expandX().fillX().colspan(3);
        row();
        add(velocityYLabel).right();
        add(velocityYField).minWidth(300).expandX().fillX().colspan(3);

        row();
        add(massLabel).right();
        add(massField).minWidth(300).expandX().fillX().colspan(3);

        row();
        add(saveButton).minWidth(200).colspan(4).center();


    }

    public void closeWindow() {
        setVisible(false);
        if(gameScreen.hud.getCurrentClickFunction() == HUD.ClickFunction.BODY_INFO) {
            gameScreen.hud.deactivateClickFunction(HUD.ClickFunction.BODY_INFO);
        }
    }


    public void translateWindow(float x, float y) {
        addAction(Actions.moveBy(x, y));
    }

    public void shiftWindowToField(TextField field) {
        float hudHeight = gameScreen.HUD_HEIGHT;
        float fieldY = field.getY();
        float fieldHeight = field.getHeight();
        translateWindow(0, hudHeight - fieldY - fieldHeight);
    }

    public void addTextFieldListener(final TextField field, final String title) {
        if(Gdx.app.getType() == Application.ApplicationType.Android || Gdx.app.getType() == Application.ApplicationType.iOS) {
            field.setOnscreenKeyboard(new TextField.OnscreenKeyboard() {
                @Override
                public void show(boolean visible) {
                    //Gdx.input.setOnscreenKeyboardVisible(true);

                    Gdx.input.getTextInput(new Input.TextInputListener() {
                        @Override
                        public void input(String text) {
                            field.setText(text);
                        }

                        @Override
                        public void canceled() {
                            System.out.println("Cancelled.");
                        }
                    }, title, field.getText(), "");
                }
            });
        } else { //if desktop or html
            field.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent e, float x, float y) {
                    field.selectAll();
                }
            });
        }
    }

    public void fillInfo(DynamicSprite selectedDynamicSprite) {
        Vector2 position = selectedDynamicSprite.getBody().getWorldCenter();
        Vector2 velocity = selectedDynamicSprite.getBody().getLinearVelocity();
        float mass = selectedDynamicSprite.getBody().getMass();
        String name = selectedDynamicSprite.getName();

        float scaleFactor = gameScreen.SIZE_ADJUSTMENT_FACTOR;

        bodyNameField.setDisabled(false);
        bodyNameField.setText(name);
        positionXField.setDisabled(false);
        positionXField.setText(Float.toString(position.x / scaleFactor));
        positionYField.setDisabled(false);
        positionYField.setText(Float.toString(position.y / scaleFactor));
        velocityXField.setDisabled(false);
        velocityXField.setText(Float.toString(velocity.x / scaleFactor));
        velocityYField.setDisabled(false);
        velocityYField.setText(Float.toString(velocity.y / scaleFactor));
        massField.setDisabled(false);
        massField.setText(Float.toString(mass));
        saveButton.setDisabled(false);
    }


    public void disable() {
        bodyNameField.setText("NONE SELECTED");
        bodyNameField.setDisabled(true);
        positionXField.setText("");
        positionXField.setDisabled(true);
        positionYField.setText("");
        positionYField.setDisabled(true);
        velocityXField.setText("");
        velocityXField.setDisabled(true);
        velocityYField.setText("");
        velocityYField.setDisabled(true);
        massField.setText("");
        massField.setDisabled(true);
        saveButton.setDisabled(true);
    }

    public void saveSettings() {

        float scaleFactor = gameScreen.SIZE_ADJUSTMENT_FACTOR;

        float positionX = Float.valueOf(positionXField.getText()) * scaleFactor;
        float positionY = Float.valueOf(positionYField.getText()) * scaleFactor;
        float velocityX = Float.valueOf(velocityXField.getText()) * scaleFactor;
        float velocityY = Float.valueOf(velocityYField.getText()) * scaleFactor;
        float mass = Float.valueOf(massField.getText());
        String name = bodyNameField.getText();

        if(mass > gameScreen.MAX_BODY_MASS) {
            mass = gameScreen.MAX_BODY_MASS;
        } else if(mass < gameScreen.MIN_BODY_MASS) {
            mass = gameScreen.MIN_BODY_MASS;
        }

        Body selectedBody = gameScreen.getSelectedBody();
        selectedBody.setTransform(positionX, positionY, 0);
        selectedBody.setLinearVelocity(velocityX, velocityY);
        gameScreen.setBodyMass(selectedBody, mass);
        gameScreen.getDynamicSprite(selectedBody).setName(name);

        gameScreen.doLaunchSimulation(new Vector2(velocityX, velocityY));

        gameScreen.hud.messageOverlay.showBodyName(name);
    }


}
