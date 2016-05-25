package com.application.orbit;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextTooltip;
import com.badlogic.gdx.scenes.scene2d.ui.TooltipManager;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;

/**
 * Created by Nick on 4/23/2016.
 */
public class HUD extends Stage {
    static final String UI_FILE = "skin/Holo-dark-xhdpi.json";
    static final String ICON_FILE = "icons/buttonnew.pack";
    static final float MENU_ANIMATION_TIME = 0.1f;
    static final float SLIDER_WIDTH = 700;
    static final float BUTTON_ALPHA = 0.5f;
    static final float BUTTON_ANIMATION_TIME = 0.05f;
    static final int PLAY_ICON_INDEX = 0;
    static final int PAUSE_ICON_INDEX = 1;
    static final int LOCK_ICON_INDEX = 10;
    static final int UNLOCK_ICON_INDEX = 11;
    static final int BODY_INFO_WINDOW_WIDTH = 600;
    static final int BODY_INFO_WINDOW_HEIGHT = 700;
    static final int SETTINGS_WINDOW_WIDTH = 600;
    static final int SETTINGS_WINDOW_HEIGHT = 700;
    static final int HELP_WINDOW_WIDTH = 1200;
    static final int HELP_WINDOW_HEIGHT = 700;


    public enum SubMenu {
        NONE, ADD, EDIT, VIEW, SETTINGS
    }

    SubMenu subMenuOpen;

    public enum ClickFunction {
        NONE, ADD_SINGLE, ADD_MULTIPLE, ORBIT, SCALE, ZOOM, BODY_INFO, SETTINGS, APP_INFO, HELP
    }


    ClickFunction currentClickFunction;

    Slider scaleSlider, zoomSlider;
    Skin uiSkin, iconSkin;
    TextureAtlas iconAtlas;
    TooltipManager tooltipManager;

    BodyInfoWindow bodyInfoWindow;
    SettingsWindow settingsWindow;
    AppInfoWindow appInfoWindow;
    HelpWindow helpWindow;

    TextButton //main menu buttons
            menuButton,
            editButton,
            viewButton,
            addButton,
            settingsButton;
    TextButton //add sub-menu buttons
            addSingleButton,
            addMultipleButton;
    TextButton //edit sub-menu buttons
            scaleButton,
            orbitButton,
            stickyButton,
            infoButton,
            deleteButton;
    TextButton //view sub-menu buttons
            zoomButton,
            resetButton,
            centerButton;
    TextButton //settings sub-menu buttons
            deleteAllButton,
            optionsButton,
            helpButton,
            appInfoButton;

    ArrayList<TextButton.TextButtonStyle> textButtonStyles;
    BitmapFont font;
    GameScreen gameScreen;
    Table mainTable, addTable, editTable, viewTable, settingsTable, scaleSliderTable, zoomSliderTable;
    MessageOverlay messageOverlay;

    boolean menuOpen;

    float menuDisplacement, submenuYDisplacement, editDisplacement, addDisplacement, viewDisplacement, settingsDisplacement,
            sliderYDisplacement, sliderXDisplacement;

    public HUD(final GameScreen gameScreen, Viewport viewport) {
        super(viewport);

        this.gameScreen = gameScreen; //the game screen that this HUD will be used for

        font = new BitmapFont(); //default font
        //textButtonStyle = new TextButton.TextButtonStyle(); //create style for buttons
        //textButtonStyle.font = font; //assign style the font

        uiSkin = new Skin(Gdx.files.internal(UI_FILE));

        iconAtlas = new TextureAtlas(ICON_FILE);
        iconSkin = new Skin(iconAtlas);

        tooltipManager = new TooltipManager();
        tooltipManager.initialTime = 1f;
        tooltipManager.subsequentTime = 0.5f;
        tooltipManager.resetTime = 1f;
        tooltipManager.animations = false;

        currentClickFunction = ClickFunction.NONE;
        subMenuOpen = SubMenu.NONE;

        //MAIN MENU

        textButtonStyles = new ArrayList<TextButton.TextButtonStyle>();

        createIconGraphic("button.start");
        createIconGraphic("button.pause");

        menuButton = new TextButton("", textButtonStyles.get(textButtonStyles.size()-1));
        createMenuButtonFunctionality();

        createIconGraphic("button.add");
        addButton = new TextButton("", textButtonStyles.get(textButtonStyles.size()-1));
        createAddButtonFunctionality();

        createIconGraphic("button.edit");
        editButton = new TextButton("", textButtonStyles.get(textButtonStyles.size()-1));
        createEditButtonFunctionality();

        createIconGraphic(("button.view"));
        viewButton = new TextButton("", textButtonStyles.get(textButtonStyles.size()-1));
        createViewButtonFunctionality();

        createIconGraphic("button.menuw");
        settingsButton = new TextButton("", textButtonStyles.get(textButtonStyles.size()-1));
        createSettingsButtonFunctionality();

        mainTable = new Table();  //create a table to hold all of the buttons for the main menu
        mainTable.setDebug(false); // turn on all debug lines (table, cell, and widget)

        mainTable.add(menuButton); //add the buttons to the table (left to right)
        mainTable.add(addButton);
        mainTable.add(editButton);
        mainTable.add(viewButton);
        mainTable.add(settingsButton);

        mainTable.setFillParent(true); //make the table boundaries fill the screen
        mainTable.right().bottom(); //put all of the table cells (buttons) in the bottom right corner
        addActor(mainTable); //add the table to the HUD (stage)

        // calculate the displacement of main menu table when it is contracted
        float x = 0;
        for(int i = 1; i < mainTable.getCells().size; i++) {
            //we just want to add the width of all the cells except for the first
            x += mainTable.getCells().get(i).getActor().getWidth();
        }
        menuDisplacement = x;

        //y displacement for sub-menus is equal to the height of the main menu
        submenuYDisplacement = mainTable.getCells().get(0).getActor().getHeight();

        //set the main menu at the contracted position
        mainTable.setPosition(menuDisplacement, 0);


        //ADD SUB MENU

        createIconGraphic("button.singleplanet");
        addSingleButton = new TextButton("", textButtonStyles.get(textButtonStyles.size()-1));
        createAddSingleButtonFunctionality();

        createIconGraphic("button.grains");
        addMultipleButton = new TextButton("", textButtonStyles.get(textButtonStyles.size()-1));
        createAddMultipleButtonFunctionality();

        addTable = new Table(); //All of the sub-menus will also be tables
        addTable.setDebug(false);

        addTable.add(addSingleButton); //The "Add bodies" sub-menu will have 2 buttons
        addTable.add(addMultipleButton);

        addTable.setFillParent(true); //same as the main menu, we want to fill the screen with the boundaries of the table
        addTable.right().bottom(); //position the cells (buttons) in the bottom right corner
        addActor(addTable); //add the table to the HUD

        //calculate displacement of table when contracted
        x = 0;
        for(int i = 0; i < addTable.getCells().size; i++) {
            //add the width of all sub-menu buttons
            x += addTable.getCells().get(i).getActor().getWidth();
        }
        addDisplacement = x;

        //position Add sub-menu in contracted position
        addTable.setPosition(addDisplacement, submenuYDisplacement);


        //EDIT SUB MENU

        createIconGraphic("button.circle");
        scaleButton = new TextButton("", textButtonStyles.get(textButtonStyles.size()-1));
        createScaleButtonFunctionality();

        createIconGraphic("button.orbit");
        orbitButton = new TextButton("", textButtonStyles.get(textButtonStyles.size()-1));
        createOrbitButtonFunctionality();

        createIconGraphic("button.lock");
        createIconGraphic("button.unlock");
        stickyButton = new TextButton("", textButtonStyles.get(textButtonStyles.size()-1));
        createStickyButtonFunctionality();

        createIconGraphic("button.info2");
        infoButton = new TextButton("", textButtonStyles.get(textButtonStyles.size()-1));
        createBodyInfoButtonFunctionality();

        createIconGraphic("button.trash");
        deleteButton = new TextButton("", textButtonStyles.get(textButtonStyles.size()-1));
        createDeleteButtonFunctionality();


        editTable = new Table();
        editTable.setDebug(false);

        editTable.add(scaleButton);
        editTable.add(orbitButton);
        editTable.add(stickyButton);
        editTable.add(infoButton);
        editTable.add(deleteButton);

        editTable.setFillParent(true);
        editTable.right().bottom();
        addActor(editTable);

        x = 0;
        for (int i = 0; i < editTable.getCells().size; i++) {
            x += editTable.getCells().get(i).getActor().getWidth();
        }
        editDisplacement = x;

        editTable.setPosition(editDisplacement, submenuYDisplacement);


        //VIEW SUB MENU

        createIconGraphic("button.zoomin");
        zoomButton = new TextButton("", textButtonStyles.get(textButtonStyles.size()-1));
        createZoomButtonFunctionality();

        createIconGraphic("button.reset");
        resetButton = new TextButton("", textButtonStyles.get(textButtonStyles.size()-1));
        createResetButtonFunctionality();

        createIconGraphic("button.cam1");
        centerButton = new TextButton("", textButtonStyles.get(textButtonStyles.size()-1));
        createCenterButtonFunctionality();

        viewTable = new Table();
        viewTable.setDebug(false);

        viewTable.add(zoomButton);
        viewTable.add(resetButton);
        viewTable.add(centerButton);

        viewTable.setFillParent(true);
        viewTable.right().bottom();
        addActor(viewTable);

        x = 0;
        for(int i = 0; i < viewTable.getCells().size; i++) {
            x += viewTable.getCells().get(i).getActor().getWidth();
        }
        viewDisplacement = x;

        viewTable.setPosition(viewDisplacement, submenuYDisplacement);


        //SETTINGS SUB MENU

        createIconGraphic("button.delete");
        deleteAllButton = new TextButton("", textButtonStyles.get(textButtonStyles.size()-1));
        createDeleteAllButtonFunctionality();

        createIconGraphic("button.setting");
        optionsButton = new TextButton("", textButtonStyles.get(textButtonStyles.size()-1));
        createOptionsButtonFunctionality();

        createIconGraphic("button.help");
        helpButton = new TextButton("", textButtonStyles.get(textButtonStyles.size()-1));
        createHelpButtonFunctionality();

        createIconGraphic("button.info");
        appInfoButton = new TextButton("", textButtonStyles.get(textButtonStyles.size()-1));
        createAppInfoButtonFunctionality();

        settingsTable = new Table();
        settingsTable.setDebug(false);

        settingsTable.add(deleteAllButton);
        settingsTable.add(optionsButton);
        settingsTable.add(helpButton);
        settingsTable.add(appInfoButton);

        settingsTable.setFillParent(true);
        settingsTable.right().bottom();
        addActor(settingsTable);

        x = 0;
        for(int i = 0; i < settingsTable.getCells().size; i++) {
            x += settingsTable.getCells().get(i).getActor().getWidth();
        }
        settingsDisplacement = x;

        settingsTable.setPosition(settingsDisplacement, submenuYDisplacement);



        //BODY SCALE SLIDER
        float maxRadius = gameScreen.getCircleRadius(gameScreen.MAX_BODY_MASS);
        float minRadius = gameScreen.getCircleRadius(gameScreen.MIN_BODY_MASS);

        scaleSlider = new Slider(minRadius, maxRadius, 0.01f, false, uiSkin);
        createScaleSliderFunctionality();
        scaleSlider.setDebug(false);

        scaleSlider.setDisabled(true);

        scaleSliderTable = new Table();
        scaleSliderTable.setDebug(false);

        //y displacement for slider is equal to the height of the main menu and sub menus
        sliderYDisplacement = submenuYDisplacement + addTable.getCells().get(0).getActor().getHeight();

        scaleSliderTable.add(scaleSlider).minWidth(SLIDER_WIDTH);

        scaleSliderTable.setFillParent(true);
        scaleSliderTable.right().bottom();
        addActor(scaleSliderTable);

        sliderXDisplacement = SLIDER_WIDTH;

        scaleSliderTable.setPosition(sliderXDisplacement, sliderYDisplacement);

        subMenuOpen = SubMenu.NONE;
        currentClickFunction = ClickFunction.NONE;


        //ZOOM SLIDER
        float maxZoom = gameScreen.MAX_ZOOM;
        float minZoom = gameScreen.MIN_ZOOM;

        zoomSlider = new Slider(minZoom, maxZoom, 0.1f, false, uiSkin);
        createZoomSliderFunctionality();
        zoomSlider.setDebug(false);

        zoomSliderTable = new Table();
        zoomSliderTable.setDebug(false);

        zoomSliderTable.add(zoomSlider).minWidth(SLIDER_WIDTH);

        zoomSliderTable.setFillParent(true);
        zoomSliderTable.right().bottom();
        addActor(zoomSliderTable);

        zoomSliderTable.setPosition(sliderXDisplacement, sliderYDisplacement);

        subMenuOpen = SubMenu.NONE;
        currentClickFunction = ClickFunction.NONE;

        //BODY INFO WINDOW

        bodyInfoWindow = new BodyInfoWindow("  Selected Body Properties", uiSkin, gameScreen);
        addActor(bodyInfoWindow);
        bodyInfoWindow.setSize(BODY_INFO_WINDOW_WIDTH, BODY_INFO_WINDOW_HEIGHT);
        bodyInfoWindow.disable();
        bodyInfoWindow.setVisible(false);

        //SETTINGS WINDOW

        settingsWindow = new SettingsWindow("  App Settings", uiSkin, gameScreen);
        addActor(settingsWindow);
        settingsWindow.setSize(SETTINGS_WINDOW_WIDTH, SETTINGS_WINDOW_HEIGHT);
        settingsWindow.setVisible(false);

        // APP INFO WINDOW

        appInfoWindow = new AppInfoWindow("  App Info", uiSkin, gameScreen);
        addActor(appInfoWindow);
        appInfoWindow.setSize(SETTINGS_WINDOW_WIDTH, SETTINGS_WINDOW_HEIGHT);
        appInfoWindow.setVisible(false);

        // HELP WINDOW
        helpWindow = new HelpWindow("  Help/Tutorial", uiSkin, gameScreen, this);
        addActor(helpWindow);
        helpWindow.setSize(HELP_WINDOW_WIDTH, HELP_WINDOW_HEIGHT);
        helpWindow.setVisible(false);

        // MESSAGE OVERLAY
        messageOverlay = new MessageOverlay(uiSkin);
        addActor(messageOverlay);


    }


    public void createIconGraphic(String graphicString) {
        textButtonStyles.add(new TextButton.TextButtonStyle()); //create style for buttons
        textButtonStyles.get(textButtonStyles.size()-1).font = font; //assign style the font
        textButtonStyles.get(textButtonStyles.size()-1).up = iconSkin.getDrawable(graphicString);
    }

    /**
     * This method opens and closes the main menu. Called every time the menu button is clicked.
     */
    public void mainMenu() {

        if(!menuOpen) {
            mainTable.addAction(Actions.moveTo(0, 0, MENU_ANIMATION_TIME));
            gameScreen.pauseGame();
            menuButton.setStyle(textButtonStyles.get(PLAY_ICON_INDEX));
        } else {
            mainTable.addAction(Actions.moveTo(menuDisplacement, 0, MENU_ANIMATION_TIME));
            closeSubMenu(subMenuOpen);
            deactivateClickFunction(currentClickFunction);
            gameScreen.unpauseGame();
            bodyInfoWindow.setVisible(false);
            menuButton.setStyle(textButtonStyles.get(PAUSE_ICON_INDEX));
            gameScreen.launchSimulation.stopSimulation();
        }
        menuOpen = !menuOpen;

    }

    public boolean isMenuOpen() {
        return menuOpen;
    }

    /**
     * This method is called whenever one of the sub-menu buttons is hit. If the clicked menu isn't open
     * it opens it. Otherwise, it closes it. If a menu is open already and a different button is clicked,
     * the open menu is closed, and the new one opened.
     * @param menu the menu to open/close
     */
    public void subMenu(SubMenu menu) {
        if(menu == subMenuOpen) {
            closeSubMenu(subMenuOpen);
        } else {
            closeSubMenu(subMenuOpen);
            openSubMenu(menu);
            deactivateClickFunction(currentClickFunction);
        }

    }

    /**
     * This method opens the given sub-menu
     * @param menu the sub-menu to open
     */
    public void openSubMenu(SubMenu menu) {
        subMenuOpen = menu;
        switch(menu) {
            case NONE:
                closeSubMenu(subMenuOpen);
                break;
            case ADD:
                addTable.addAction(Actions.moveTo(0, submenuYDisplacement, MENU_ANIMATION_TIME));
                addButton.addAction(Actions.alpha(BUTTON_ALPHA, BUTTON_ANIMATION_TIME));
                break;
            case EDIT:
                editTable.addAction(Actions.moveTo(0, submenuYDisplacement, MENU_ANIMATION_TIME));
                editButton.addAction(Actions.alpha(BUTTON_ALPHA, BUTTON_ANIMATION_TIME));
                if(!gameScreen.isBodySelected()) {
                    messageOverlay.showTip("Select a body to edit");
                }
                break;
            case VIEW:
                viewTable.addAction(Actions.moveTo(0, submenuYDisplacement, MENU_ANIMATION_TIME));
                viewButton.addAction(Actions.alpha(BUTTON_ALPHA, BUTTON_ANIMATION_TIME));
                break;
            case SETTINGS:
                settingsTable.addAction(Actions.moveTo(0, submenuYDisplacement, MENU_ANIMATION_TIME));
                settingsButton.addAction(Actions.alpha(BUTTON_ALPHA, BUTTON_ANIMATION_TIME));
                break;


        }
    }

    /**
     * This method closes the given sub-menu
     * @param menu the sub_menu to close
     */
    public void closeSubMenu(SubMenu menu) {
        subMenuOpen = SubMenu.NONE;
        switch(menu) {
            case ADD:
                addTable.addAction(Actions.moveTo(addDisplacement, submenuYDisplacement, MENU_ANIMATION_TIME));
                addButton.addAction(Actions.alpha(1, BUTTON_ANIMATION_TIME));
                break;
            case EDIT:
                editTable.addAction(Actions.moveTo(editDisplacement, submenuYDisplacement, MENU_ANIMATION_TIME));
                editButton.addAction(Actions.alpha(1, BUTTON_ANIMATION_TIME));
                break;
            case VIEW:
                viewTable.addAction(Actions.moveTo(viewDisplacement, submenuYDisplacement, MENU_ANIMATION_TIME));
                viewButton.addAction(Actions.alpha(1, BUTTON_ANIMATION_TIME));
                break;
            case SETTINGS:
                settingsTable.addAction(Actions.moveTo(settingsDisplacement, submenuYDisplacement, MENU_ANIMATION_TIME));
                settingsButton.addAction(Actions.alpha(1, BUTTON_ANIMATION_TIME));


        }
    }

    /**
     * This method handles toggling of all of the click functions. Functions that require a tap/click.
     * If the passed click function is not already activated, it activates it. Otherwise, it deactivates
     * it. If a different one is activated, it deactivates that one before activating the new one.
     * @param function the click function to (de)activate
     */
    public void clickFunction(ClickFunction function) {
        if(currentClickFunction == function) {
            deactivateClickFunction(function);
        } else {
            deactivateClickFunction(currentClickFunction);
            activateClickFunction(function);
        }
    }

    public ClickFunction getCurrentClickFunction() {
        return currentClickFunction;
    }

    /**
     * This method activates a desired click function
     * @param function the click function to activate
     */
    public void activateClickFunction(ClickFunction function) {
        currentClickFunction = function;
        switch(function) {
            case ADD_SINGLE:
                gameScreen.setAddingBody(true);
                addSingleButton.addAction(Actions.alpha(BUTTON_ALPHA, BUTTON_ANIMATION_TIME));
                break;
            case ADD_MULTIPLE:
                gameScreen.setAddingBodyMatrix(true);
                addMultipleButton.addAction(Actions.alpha(BUTTON_ALPHA, BUTTON_ANIMATION_TIME));
                break;
            case ORBIT:
                gameScreen.setPickingOrbit(true);
                orbitButton.addAction(Actions.alpha(BUTTON_ALPHA, BUTTON_ANIMATION_TIME));
                break;
            case SCALE:
                scaleSliderTable.setPosition(0, sliderYDisplacement);
                if (gameScreen.isBodySelected()) {
                    float mass = gameScreen.getSelectedBody().getMass();
                    float radius = gameScreen.getCircleRadius(mass);
                    scaleSlider.setValue(radius);
                }
                scaleButton.addAction(Actions.alpha(BUTTON_ALPHA, BUTTON_ANIMATION_TIME));
                break;
            case BODY_INFO:
                bodyInfoWindow.setVisible(true);
                if(gameScreen.isBodySelected()) {
                    bodyInfoWindow.fillInfo(gameScreen.getDynamicSprite(gameScreen.getSelectedBody()));
                } else {
                    bodyInfoWindow.disable();
                }
                infoButton.addAction(Actions.alpha(BUTTON_ALPHA, BUTTON_ANIMATION_TIME));
                break;
            case ZOOM:
                zoomSliderTable.setPosition(0, sliderYDisplacement);
                zoomSlider.setValue(gameScreen.MAX_ZOOM + gameScreen.MIN_ZOOM - gameScreen.getZoom());
                zoomButton.addAction(Actions.alpha(BUTTON_ALPHA, BUTTON_ANIMATION_TIME));
                break;
            case SETTINGS:
                settingsWindow.setVisible(true);
                optionsButton.addAction(Actions.alpha(BUTTON_ALPHA, BUTTON_ANIMATION_TIME));
                break;
            case APP_INFO:
                appInfoWindow.setVisible(true);
                appInfoButton.addAction(Actions.alpha(BUTTON_ALPHA, BUTTON_ANIMATION_TIME));
                break;
            case HELP:
                helpWindow.setVisible(true);
                helpButton.addAction(Actions.alpha(BUTTON_ALPHA, BUTTON_ANIMATION_TIME));
                break;
        }
    }

    /**
     * THis method deactivates a desired click function
     * @param function the click function to activate
     */
    public void deactivateClickFunction(ClickFunction function) {
        currentClickFunction = ClickFunction.NONE;
        switch (function) {
            case ADD_SINGLE:
                gameScreen.setAddingBody(false);
                addSingleButton.addAction(Actions.alpha(1, BUTTON_ANIMATION_TIME));
                break;
            case ADD_MULTIPLE:
                gameScreen.setAddingBodyMatrix(false);
                addMultipleButton.addAction(Actions.alpha(1, BUTTON_ANIMATION_TIME));
                break;
            case ORBIT:
                gameScreen.setPickingOrbit(false);
                orbitButton.addAction(Actions.alpha(1, BUTTON_ANIMATION_TIME));
                break;
            case SCALE:
                scaleSliderTable.setPosition(sliderXDisplacement, sliderYDisplacement);
                scaleButton.addAction(Actions.alpha(1, BUTTON_ANIMATION_TIME));
                break;
            case BODY_INFO:
                bodyInfoWindow.setVisible(false);
                infoButton.addAction(Actions.alpha(1, BUTTON_ANIMATION_TIME));
                break;
            case ZOOM:
                zoomSliderTable.setPosition(sliderXDisplacement, sliderYDisplacement);
                zoomButton.addAction(Actions.alpha(1, BUTTON_ANIMATION_TIME));
                break;
            case SETTINGS:
                settingsWindow.setVisible(false);
                optionsButton.addAction(Actions.alpha(1, BUTTON_ANIMATION_TIME));
                break;
            case APP_INFO:
                appInfoWindow.setVisible(false);
                appInfoButton.addAction(Actions.alpha(1, BUTTON_ANIMATION_TIME));
                break;
            case HELP:
                helpWindow.setVisible(false);
                helpButton.addAction(Actions.alpha(1, BUTTON_ANIMATION_TIME));
                break;
        }
    }


    /**
     * This method creates the functionality for the main menu button. Opens and closes on click.
     */
    private void createMenuButtonFunctionality() {
        menuButton.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                menuButton.addAction(Actions.alpha(BUTTON_ALPHA));

                return true;
            }

            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                mainMenu();
                menuButton.addAction(Actions.alpha(1));
            }
        });
        menuButton.addListener(new TextTooltip("Open/Close menu", tooltipManager, uiSkin));
    }

    /**
     * This method creates the functionality for the "Add" sub-menu button. Opens/closes the sub-menu
     * on click. If a different one is open, it closes it first.
     */
    private void createAddButtonFunctionality() {
        addButton.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                addButton.addAction(Actions.alpha(BUTTON_ALPHA));

                return true;
            }

            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                subMenu(SubMenu.ADD);
            }
        });
        addButton.addListener(new TextTooltip("Add new bodies", tooltipManager, uiSkin));
    }

    /**
     * This method creates the functionality for the "Edit" sub-menu button. Opens/closes the sub-menu
     * on click. If a different one is open, it closes it first.
     */
    private void createEditButtonFunctionality() {
        editButton.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                editButton.addAction(Actions.alpha(BUTTON_ALPHA));

                return true;
            }

            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

                subMenu(SubMenu.EDIT);
            }
        });
        editButton.addListener(new TextTooltip("Edit selected body", tooltipManager, uiSkin));
    }

    /**
     * This method creates the functionality for the "View" sub-menu button. Opens/closes the sub-menu
     * on click. If a different one is open, it closes it first.
     */
    private void createViewButtonFunctionality() {
        viewButton.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                viewButton.addAction(Actions.alpha(BUTTON_ALPHA));

                return true;
            }

            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                subMenu(SubMenu.VIEW);
            }
        });
        viewButton.addListener(new TextTooltip("Camera settings", tooltipManager, uiSkin));
    }

    /**
     * This method creates the functionality for the "Settings" sub-menu button. Opens/closes the sub-menu
     * on click. If a different one is open, it closes it first.
     */
    private void createSettingsButtonFunctionality() {
        settingsButton.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                settingsButton.addAction(Actions.alpha(BUTTON_ALPHA));

                return true;
            }

            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                subMenu(SubMenu.SETTINGS);
            }
        });
        settingsButton.addListener(new TextTooltip("More options", tooltipManager, uiSkin));
    }

    /**
     * This method creates the functionality for the add single body button. It toggles the adding
     * single body on tap setting.
     */
    private void createAddSingleButtonFunctionality() {
        addSingleButton.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                addSingleButton.addAction(Actions.alpha(BUTTON_ALPHA));

                return true;
            }

            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                clickFunction(ClickFunction.ADD_SINGLE);
            }
        });
        addSingleButton.addListener(new TextTooltip("Add single body", tooltipManager, uiSkin));
    }

    /**
     * This method creates the functionality for the add multiple body button. It toggles the adding
     * multiple body on tap setting.
     */
    private void createAddMultipleButtonFunctionality() {
        addMultipleButton.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                addMultipleButton.addAction(Actions.alpha(BUTTON_ALPHA));

                return true;
            }

            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                clickFunction(ClickFunction.ADD_MULTIPLE);
            }
        });
        addMultipleButton.addListener(new TextTooltip("Add body matrix", tooltipManager, uiSkin));
    }


    private void createScaleButtonFunctionality() {
        scaleButton.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                scaleButton.addAction(Actions.alpha(BUTTON_ALPHA));

                return true;
            }

            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                scaleButton.addAction(Actions.alpha(1));
                if(gameScreen.isBodySelected()) {
                    clickFunction(ClickFunction.SCALE);
                } else {
                    messageOverlay.showTip("Body must be selected to scale");
                }
            }
        });
        scaleButton.addListener(new TextTooltip("Scale selected body", tooltipManager, uiSkin));
    }


    private void createDeleteButtonFunctionality() {
        deleteButton.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                deleteButton.addAction(Actions.alpha(BUTTON_ALPHA));

                return true;
            }

            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if(gameScreen.isBodySelected()) {
                    String name = getSelectedBodyName();
                    messageOverlay.showTip(name + " deleted");
                    gameScreen.deleteSelectedBody();
                } else {
                    messageOverlay.showTip("Body must be selected to delete");
                }
                deleteButton.addAction(Actions.alpha(1));

            }
        });
        deleteButton.addListener(new TextTooltip("Delete selected body", tooltipManager, uiSkin));
    }

    private void createStickyButtonFunctionality() {
        stickyButton.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                stickyButton.addAction(Actions.alpha(BUTTON_ALPHA));

                return true;
            }

            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                gameScreen.toggleSelectedBodyMovable();
                stickyButton.addAction(Actions.alpha(1));

                updateStickyIcon();

                if(!gameScreen.isBodySelected()) {
                    messageOverlay.showTip("Body must be selected to lock");
                } else {
                    boolean sticky = !gameScreen.getDynamicSprite(gameScreen.getSelectedBody()).isMovable();
                    if(sticky) {
                        messageOverlay.showTip(getSelectedBodyName() + " locked");
                    } else {
                        messageOverlay.showTip(getSelectedBodyName() + " unlocked");
                    }

                }
            }
        });
        stickyButton.addListener(new TextTooltip("Lock/Unlock body position", tooltipManager, uiSkin));
    }

    private void createOrbitButtonFunctionality() {
        orbitButton.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                orbitButton.addAction(Actions.alpha(BUTTON_ALPHA));

                return true;
            }

            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if(gameScreen.isBodySelected()) {
                    if (gameScreen.getDynamicSprite(gameScreen.getSelectedBody()).isMovable()) {
                        clickFunction(ClickFunction.ORBIT);
                        messageOverlay.showTip("Select a body to orbit around");
                    } else {
                        messageOverlay.showTip(getSelectedBodyName() + " is locked. Unlock to set orbit");
                    }
                } else {
                    orbitButton.addAction(Actions.alpha(1));
                    messageOverlay.showTip("Body must be selected to set orbit");

                }
            }
        });
        orbitButton.addListener(new TextTooltip("Orbit", tooltipManager, uiSkin));
    }

    private void createBodyInfoButtonFunctionality() {
        infoButton.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                infoButton.addAction(Actions.alpha(BUTTON_ALPHA));

                return true;
            }

            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                clickFunction(ClickFunction.BODY_INFO);
            }
        });
        infoButton.addListener(new TextTooltip("Selected body properties", tooltipManager, uiSkin));
    }

    private void createZoomButtonFunctionality() {
        zoomButton.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                zoomButton.addAction(Actions.alpha(BUTTON_ALPHA));
                return true;
            }

            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                clickFunction(ClickFunction.ZOOM);

            }
        });
        zoomButton.addListener(new TextTooltip("Zoom camera", tooltipManager, uiSkin));
    }

    private void createOptionsButtonFunctionality() {
        optionsButton.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                optionsButton.addAction(Actions.alpha(BUTTON_ALPHA));

                return true;
            }

            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                clickFunction(ClickFunction.SETTINGS);
            }
        });
        optionsButton.addListener(new TextTooltip("Settings", tooltipManager, uiSkin));
    }

    private void createResetButtonFunctionality() {
        resetButton.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                resetButton.addAction(Actions.alpha(BUTTON_ALPHA));

                return true;
            }

            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                gameScreen.resetCamera();
                centerButton.addAction(Actions.alpha(1));
                resetButton.addAction(Actions.alpha(1));
                messageOverlay.showTip("Camera reset");
            }
        });
        resetButton.addListener(new TextTooltip("Reset camera", tooltipManager, uiSkin));
    }

    private void createCenterButtonFunctionality() {
        centerButton.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if(!gameScreen.isChaseCamOn()) {
                    centerButton.addAction(Actions.alpha(BUTTON_ALPHA));
                    messageOverlay.showTip("Camera is following selected body");
                } else {
                    centerButton.addAction(Actions.alpha(1));
                    messageOverlay.showTip("Camera is free");
                }

                gameScreen.setChaseCamOn(!gameScreen.isChaseCamOn());

                return true;
            }

            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

            }
        });
        centerButton.addListener(new TextTooltip("Center camera on selected body", tooltipManager, uiSkin));
    }

    private void createDeleteAllButtonFunctionality() {
        deleteAllButton.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                deleteAllButton.addAction(Actions.alpha(BUTTON_ALPHA));

                return true;
            }

            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                gameScreen.deleteAllBodies();
                deleteAllButton.addAction(Actions.alpha(1));
                messageOverlay.showTip("All bodies deleted");
            }
        });

        deleteAllButton.addListener(new TextTooltip("Delete all bodies", tooltipManager, uiSkin));

    }


    private void createAppInfoButtonFunctionality() {
        appInfoButton.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                appInfoButton.addAction(Actions.alpha(BUTTON_ALPHA));

                return true;
            }

            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                clickFunction(ClickFunction.APP_INFO);
            }
        });

        appInfoButton.addListener(new TextTooltip("App Info", tooltipManager, uiSkin));
    }

    private void createHelpButtonFunctionality() {
        helpButton.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                helpButton.addAction(Actions.alpha(BUTTON_ALPHA));

                return true;
            }

            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                clickFunction(ClickFunction.HELP);
            }
        });

        helpButton.addListener(new TextTooltip("Help/Tutorial", tooltipManager, uiSkin));
    }

    private void createScaleSliderFunctionality() {
        scaleSlider.addListener(new ChangeListener()
        {

            @Override
            public void changed(ChangeEvent event, Actor actor)
            {
                if(gameScreen.isBodySelected()) {
                    Slider slider = (Slider) actor;

                    float radius = slider.getValue();

                    float mass = gameScreen.getCircleMass(radius);

                    gameScreen.setBodyMass(gameScreen.getSelectedBody(), mass);
                }
            }

        });
    }

    private void createZoomSliderFunctionality() {
        zoomSlider.addListener(new ChangeListener()
        {

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Slider slider = (Slider) actor;

                    float zoom = slider.getValue();

                zoom = gameScreen.MAX_ZOOM + gameScreen.MIN_ZOOM - zoom;

                    gameScreen.setZoom(zoom);
            }


        });
    }

    public void updateStickyIcon() {
        if(gameScreen.isBodySelected()) {
            boolean sticky = !gameScreen.getDynamicSprite(gameScreen.getSelectedBody()).isMovable();
            if(sticky) {
                stickyButton.setStyle(textButtonStyles.get(LOCK_ICON_INDEX));
            } else {
                stickyButton.setStyle(textButtonStyles.get(UNLOCK_ICON_INDEX));
            }
        } else {
            stickyButton.setStyle(textButtonStyles.get(UNLOCK_ICON_INDEX));

        }
    }

    public String getSelectedBodyName() {
        return gameScreen.getDynamicSprite(gameScreen.getSelectedBody()).getName();
    }
}
