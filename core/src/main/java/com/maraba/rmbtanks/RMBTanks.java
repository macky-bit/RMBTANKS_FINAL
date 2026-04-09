package com.maraba.rmbtanks;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.maraba.rmbtanks.Sound.SoundManager;
import com.maraba.rmbtanks.network.NetworkManager;
import com.maraba.rmbtanks.screens.GameScreen.GameScreen;
import com.maraba.rmbtanks.screens.GameScreen.SelectionScreen;
import com.maraba.rmbtanks.screens.HomeScreens.HomeScreen;
import com.maraba.rmbtanks.screens.HomeScreens.InfoScreen;
import com.maraba.rmbtanks.screens.HomeScreens.SettingsScreen;
import com.maraba.rmbtanks.screens.multiplayer.MultiplayerMenuScreen;
import com.maraba.rmbtanks.screens.multiplayer.WaitingScreen;

public class RMBTanks extends ApplicationAdapter {

    // ── AUDIO ──────────────────────────────────────────
    SoundManager sound;

    static final int V_WIDTH  = 1024;
    static final int V_HEIGHT = 800;

    SpriteBatch   batch;
    ShapeRenderer shape;
    Viewport      viewport;

    // ── TEXTURES ───────────────────────────────────────
    Texture tank1, tank2, tank3, bulletTexture;

    // ── NETWORK ────────────────────────────────────────
    NetworkManager network = new NetworkManager();

    // ── SCREENS ────────────────────────────────────────
    HomeScreen            homeScreen;
    MultiplayerMenuScreen multiScreen;
    WaitingScreen         waitingScreen;
    SelectionScreen       selectionScreen;
    GameScreen            gameScreen;
    InfoScreen            infoScreen;
    SettingsScreen        settingsScreen;

    // ── SCREEN IDs ─────────────────────────────────────
    // 0=Home  1=MultiMenu  2=Waiting  3=MultiSelection
    // 4=MultiGame  5=Info  6=Settings  7=SoloSelection
    // 8=SoloGame
    int currentScreen = 0;

    int myTankChoice     = 0;
    int remoteTankChoice = 0;

    @Override
    public void create() {
        batch    = new SpriteBatch();
        shape    = new ShapeRenderer();
        viewport = new FitViewport(V_WIDTH, V_HEIGHT);

        tank1         = new Texture("tanks/tank1.png");
        tank2         = new Texture("tanks/tank2.png");
        tank3         = new Texture("tanks/tank3.png");
        bulletTexture = new Texture("tanks/bullet.png");

        sound           = new SoundManager();
        homeScreen      = new HomeScreen();
        multiScreen     = new MultiplayerMenuScreen(network);
        waitingScreen   = new WaitingScreen(network);
        selectionScreen = new SelectionScreen(tank1, tank2, tank3);
        infoScreen      = new InfoScreen();
        settingsScreen  = new SettingsScreen(sound);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    Texture getTank(int index) {
        return index == 0 ? tank1 : index == 1 ? tank2 : tank3;
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0.08f, 0.08f, 0.12f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        shape.setProjectionMatrix(viewport.getCamera().combined);

        switch (currentScreen) {

            case 0: // ── HOME ───────────────────────────
                sound.setMenuVolume();
                int chosen = homeScreen.update();
                if (chosen == 0) currentScreen = 7; // SOLO
                if (chosen == 1) currentScreen = 1; // MULTIPLAYER
                if (chosen == 2) currentScreen = 6; // SETTINGS
                if (chosen == 3) currentScreen = 5; // INFO
                homeScreen.draw(batch, shape, V_WIDTH, V_HEIGHT);
                break;

            case 1: // ── MULTIPLAYER MENU ───────────────
                sound.setMenuVolume();
                int result = multiScreen.update();
                if (result == 1 || result == 2) currentScreen = 2;
                if (result == -1) currentScreen = 0;
                multiScreen.draw(batch, shape, V_WIDTH, V_HEIGHT);
                break;

            case 2: // ── WAITING ────────────────────────
                sound.setMenuVolume();
                if (waitingScreen.update()) currentScreen = 3;
                if (!network.connected &&
                    Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
                    currentScreen = 0;
                }
                waitingScreen.draw(batch, shape, V_WIDTH, V_HEIGHT);
                break;

            case 3: // ── MULTI TANK SELECTION ───────────
                sound.setMenuVolume();
                if (selectionScreen.update()) {
                    myTankChoice     = selectionScreen.getSelectedTank();
                    remoteTankChoice = (myTankChoice + 1) % 3;
                    gameScreen = new GameScreen(
                        getTank(myTankChoice),
                        getTank(remoteTankChoice),
                        bulletTexture,
                        network,
                        V_WIDTH, V_HEIGHT,
                        sound,
                        viewport       // ← added
                    );
                    currentScreen = 4;
                }
                selectionScreen.draw(batch, shape, V_WIDTH, V_HEIGHT);
                break;

            case 4: // ── MULTIPLAYER GAME ───────────────
                sound.setGameVolume();
                gameScreen.update();
                gameScreen.draw(batch, shape);
                if (gameScreen.isPaused()) {
                    sound.pause();
                    int pr = gameScreen.updatePause();
                    if (pr == 0) { gameScreen.resume(); sound.resume(); }
                    if (pr == 1) { network.disconnect(); sound.resume(); currentScreen = 0; }
                    if (pr == 2) Gdx.app.exit();
                }
                if (gameScreen.isMatchOver() &&
                    Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
                    network.disconnect();
                    currentScreen = 0;
                }
                break;

            case 5: // ── INFO ───────────────────────────
                sound.setMenuVolume();
                if (infoScreen.update()) currentScreen = 0;
                infoScreen.draw(batch, shape, V_WIDTH, V_HEIGHT);
                break;

            case 6: // ── SETTINGS ───────────────────────
                sound.setMenuVolume();
                if (settingsScreen.update()) currentScreen = 0;
                settingsScreen.draw(batch, shape, V_WIDTH, V_HEIGHT);
                break;

            case 7: // ── SOLO TANK SELECTION ────────────
                sound.setMenuVolume();
                if (selectionScreen.update()) {
                    myTankChoice = selectionScreen.getSelectedTank();
                    gameScreen = new GameScreen(
                        getTank(myTankChoice),
                        null,
                        bulletTexture,
                        null,
                        V_WIDTH, V_HEIGHT,
                        sound,
                        viewport       // ← added
                    );
                    currentScreen = 8;
                }
                selectionScreen.draw(batch, shape, V_WIDTH, V_HEIGHT);
                break;

            case 8: // ── SOLO GAME ──────────────────────
                sound.setGameVolume();
                gameScreen.update();
                gameScreen.draw(batch, shape);
                if (gameScreen.isPaused()) {
                    sound.pause();
                    int pr = gameScreen.updatePause();
                    if (pr == 0) { gameScreen.resume(); sound.resume(); }
                    if (pr == 1) { sound.resume(); currentScreen = 0; }
                    if (pr == 2) Gdx.app.exit();
                }
                break;
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        shape.dispose();
        tank1.dispose();
        tank2.dispose();
        tank3.dispose();
        bulletTexture.dispose();
        sound.dispose();
        network.disconnect();
        homeScreen.dispose();
        infoScreen.dispose();
        settingsScreen.dispose();
        multiScreen.dispose();
        waitingScreen.dispose();
    }
}
