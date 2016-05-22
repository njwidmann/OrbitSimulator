package com.application.orbit.client;

import com.application.orbit.GameActivity;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;

public class HtmlLauncher extends GwtApplication {

        /*@Override
        public GwtApplicationConfiguration getConfig () {
                return new GwtApplicationConfiguration(480, 320);
        }*/

        @Override
        public ApplicationListener createApplicationListener () {
                return new GameActivity();
        }

        @Override
        public void onModuleLoad () {
                super.onModuleLoad();
                com.google.gwt.user.client.Window.addResizeHandler(new ResizeHandler() {
                        public void onResize(ResizeEvent ev) {
                                Gdx.graphics.setWindowedMode((int)(ev.getWidth()*0.95), (int)(ev.getHeight()*0.95));
                        }
                });
        }

        @Override
        public GwtApplicationConfiguration getConfig () {
                int height = (int)(com.google.gwt.user.client.Window.getClientHeight()*0.95);
                int width = (int) (com.google.gwt.user.client.Window.getClientWidth() *0.95);
                GwtApplicationConfiguration cfg = new GwtApplicationConfiguration(width, height);
                return cfg;
        }

        @Override
        public ApplicationListener getApplicationListener () {
                return new GameActivity();
        }

}