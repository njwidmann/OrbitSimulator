package com.application.orbit;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
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
    ScrollPane scroll;
    HUD hud;

    public HelpWindow(String name, Skin skin, GameScreen gameScreen, HUD hud) {
        super(name, skin);

        this.gameScreen = gameScreen;
        this.hud = hud;

        setPosition(0, 0);
        defaults().spaceBottom(10);

        digitFilter = new DigitFilter();

        Table scrollTable = new Table(skin);

        TextureAtlas iconAtlas = hud.iconAtlas;

        final Label general = new Label("General", skin);
        general.setFontScale(1.5f);

        final Label description = new Label("This app can be used to simulate the motion of solar bodies\n" +
                "in space. The interactions between bodies are found using\n" +
                "Newton's Universal Law of Gravitation with a gravitational\n" +
                "constant (G) = 10.", skin);

        final Label controlsTitle = new Label("Controls", skin);
        controlsTitle.setFontScale(1.5f);

        final Label controls = new Label("Bodies can be added using the add function in the main menu.\n\n" +
                "Bodies can be selected by tapping/clicking on them.\n\n" +
                "A selected body can be launched by pressing down, dragging\n" +
                "back and releasing.\n\n" +
                "A selected body's mass and size can be scaled by pinching\n" +
                "or scrolling.\n\n" +
                "The camera can be zoomed by pinching or scrolling if no body\n" +
                "is selected.\n\n" +
                "The camera can be panned by pressing down outside of a\n" +
                "selected body and dragging.\n\n" +
                "Many additional controls are available in the main menu.", skin);


        final Label menuLabel = new Label("Menu Buttons", skin);
        menuLabel.setFontScale(1.5f);

        final Image addIconImage = new Image(iconAtlas.createSprite("button.add"));
        final Label addTitle = new Label("Adding Bodies", skin);
        addTitle.setFontScale(1.2f);

        final Image addSingleIcon = new Image(iconAtlas.createSprite("button.singleplanet"));
        final Image addMatrixIcon = new Image(iconAtlas.createSprite("button.grains"));
        final Label addSingleLabel = new Label("Add a single body.", skin);
        final Label addMatrixLabel = new Label( "Add a square n-by-n matrix of bodies \n" +
                                                "(n can be adjusted in settings).\n" +
                                                "Note: adding too many bodies will\n" +
                                                "slow down the simulation.", skin);

        final Image editIcon = new Image(iconAtlas.createSprite("button.edit"));
        final Label editTitle = new Label("Edit Selected Body", skin);
        editTitle.setFontScale(1.2f);

        final Image scaleIcon = new Image(iconAtlas.createSprite("button.circle"));
        final Image orbitIcon = new Image(iconAtlas.createSprite("button.orbit"));
        final Image lockIcon = new Image(iconAtlas.createSprite("button.lock"));
        final Image unlockIcon = new Image(iconAtlas.createSprite("button.unlock"));
        final Image infoIcon = new Image(iconAtlas.createSprite("button.info2"));
        final Image deleteIcon = new Image(iconAtlas.createSprite("button.trash"));

        final Label scaleLabel = new Label("Scale size of selected body", skin);
        final Label orbitLabel = new Label( "Select another body for the\n" +
                                            "current selected body to orbit.\n" +
                                            "This function does not consider\n" +
                                            "gravity from other bodies and\n" +
                                            "assumes the body being orbited\n" +
                                            "will remain stationary.", skin);
        final Label lockLabel = new Label(  "When a body is locked, it will\n" +
                                            "not be affected by gravity\n" +
                                            "or collisions with other bodies.", skin);
        final Label unlockLabel = new Label("When a body is unlocked, it will\n" +
                                            "be affected by gravity and\n" +
                                            "collisions with other bodies.", skin);
        final Label infoLabel = new Label(  "The body properties menu shows a\n" +
                                            "body's name, position, velocity,\n " +
                                            "and mass.\n" +
                                            "These properties can be expressly\n" +
                                            "defined to create specific simulations.", skin);
        final Label deleteLabel = new Label("Delete the selected body.", skin);

        final Image viewIcon = new Image(iconAtlas.createSprite("button.view"));
        final Image zoomIcon = new Image(iconAtlas.createSprite("button.zoomin"));
        final Image resetIcon = new Image(iconAtlas.createSprite("button.reset"));
        final Image centerIcon = new Image(iconAtlas.createSprite("button.cam1"));

        final Label viewTitle = new Label("Camera Settings", skin);
        viewTitle.setFontScale(1.2f);
        final Label zoomLabel = new Label("Adjust the camera's zoom.", skin);
        final Label resetLabel = new Label( "Reset the camera back to the\n" +
                                            "original position and zoom.", skin);
        final Label centerLabel = new Label("Center the camera on the current\n" +
                "selected body. While turned on,\n" +
                "the camera will follow whichever\n" +
                "body is selected, even if that\n" +
                "body changes.", skin);


        final Image moreIcon = new Image(iconAtlas.createSprite("button.menuw"));
        final Image deleteAllIcon = new Image(iconAtlas.createSprite("button.delete"));
        final Image settingsIcon = new Image(iconAtlas.createSprite("button.setting"));

        final Label moreTitle = new Label("More Options", skin);
        moreTitle.setFontScale(1.2f);
        final Label deleteAllLabel = new Label( "Delete all bodies.", skin);
        final Label settingsLabel = new Label(  "Open settings menu. This menu\n" +
                                                "provides options for adjusting\n" +
                                                "launch simulation and size of\n" +
                                                "body matrix and allows for\n" +
                                                "toggling of body fusion.\n" +
                                                "Note: Having a long dynamic\n" +
                                                "launch simulation may slow\n" +
                                                "performance on some devices.", skin);



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

        scrollTable.add(general).colspan(2);
        scrollTable.row();
        scrollTable.add(description).colspan(2).left();
        scrollTable.row();
        scrollTable.add(controlsTitle).colspan(2);
        scrollTable.row();
        scrollTable.add(controls).colspan(2).left();
        scrollTable.row();
        scrollTable.add(menuLabel).colspan(2);
        scrollTable.row();
        scrollTable.add(addIconImage);
        scrollTable.add(addTitle).left();
        scrollTable.row();
        scrollTable.add(addSingleIcon);
        scrollTable.add(addSingleLabel).left();
        scrollTable.row();
        scrollTable.add(addMatrixIcon);
        scrollTable.add(addMatrixLabel).left();
        scrollTable.row();
        scrollTable.add(editIcon);
        scrollTable.add(editTitle).left();
        scrollTable.row();
        scrollTable.add(scaleIcon);
        scrollTable.add(scaleLabel).left();
        scrollTable.row();
        scrollTable.add(orbitIcon);
        scrollTable.add(orbitLabel).left();
        scrollTable.row();
        scrollTable.add(lockIcon);
        scrollTable.add(lockLabel).left();
        scrollTable.row();
        scrollTable.add(unlockIcon);
        scrollTable.add(unlockLabel).left();
        scrollTable.row();
        scrollTable.add(infoIcon);
        scrollTable.add(infoLabel).left();
        scrollTable.row();
        scrollTable.add(deleteIcon);
        scrollTable.add(deleteLabel).left();
        scrollTable.row();
        scrollTable.add(viewIcon);
        scrollTable.add(viewTitle).left();
        scrollTable.row();
        scrollTable.add(zoomIcon);
        scrollTable.add(zoomLabel).left();
        scrollTable.row();
        scrollTable.add(resetIcon);
        scrollTable.add(resetLabel).left();
        scrollTable.row();
        scrollTable.add(centerIcon);
        scrollTable.add(centerLabel).left();
        scrollTable.row();
        scrollTable.add(moreIcon);
        scrollTable.add(moreTitle).left();
        scrollTable.row();
        scrollTable.add(deleteAllIcon);
        scrollTable.add(deleteAllLabel).left();
        scrollTable.row();
        scrollTable.add(settingsIcon);
        scrollTable.add(settingsLabel).left();



        scrollTable.row();
        //scrollTable.add(moreLabel);

        scroll = new ScrollPane(scrollTable, skin);
        add(scroll).minWidth(200).colspan(4).center();

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

