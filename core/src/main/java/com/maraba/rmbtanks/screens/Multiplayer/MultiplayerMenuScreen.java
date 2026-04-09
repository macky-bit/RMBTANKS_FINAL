package com.maraba.rmbtanks.screens.multiplayer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.maraba.rmbtanks.network.NetworkManager;

public class MultiplayerMenuScreen {

    private BitmapFont titleFont, menuFont, inputFont;
    private GlyphLayout layout;
    private NetworkManager network;

    // ── STATE ──────────────────────────────────────────
    // 0=Choose Host/Join  1=Hosting  2=Joining (type IP)
    private int state = 0;
    private int selectedOption = 0; // 0=Host 1=Join 2=Back

    // IP input
    private StringBuilder ipInput = new StringBuilder("192.168.1.");
    private float timer = 0f;

    public MultiplayerMenuScreen(NetworkManager network) {
        this.network = network;
        titleFont = new BitmapFont();
        titleFont.getData().setScale(2.5f);
        menuFont = new BitmapFont();
        menuFont.getData().setScale(2f);
        inputFont = new BitmapFont();
        inputFont.getData().setScale(1.5f);
        layout = new GlyphLayout();
    }

    // Returns: 0=stay, 1=go to waiting(host), 2=go to waiting(join), -1=back
    public int update() {
        timer += Gdx.graphics.getDeltaTime();

        if (state == 0) {
            // Navigate options
            if (Gdx.input.isKeyJustPressed(Keys.UP)) {
                selectedOption = (selectedOption - 1 + 3) % 3;
            }
            if (Gdx.input.isKeyJustPressed(Keys.DOWN)) {
                selectedOption = (selectedOption + 1) % 3;
            }
            if (Gdx.input.isKeyJustPressed(Keys.ENTER)) {
                if (selectedOption == 0) { // HOST
                    network.startHost();
                    state = 1;
                    return 1;
                }
                if (selectedOption == 1) { // JOIN
                    state = 2;
                }
                if (selectedOption == 2) return -1; // BACK
            }
            if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) return -1;

        } else if (state == 2) {
            // Typing IP address
            handleIPInput();
            if (Gdx.input.isKeyJustPressed(Keys.ENTER) && ipInput.length() > 0) {
                network.startClient(ipInput.toString());
                state = 1;
                return 2;
            }
            if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
                state = 0;
            }
        }
        return 0;
    }

    private void handleIPInput() {
        // Numbers and dots only
        for (int k = Keys.NUM_0; k <= Keys.NUM_9; k++) {
            if (Gdx.input.isKeyJustPressed(k)) {
                if (ipInput.length() < 15)
                    ipInput.append((char)('0' + (k - Keys.NUM_0)));
            }
        }
        if (Gdx.input.isKeyJustPressed(Keys.PERIOD)) {
            if (ipInput.length() < 15) ipInput.append(".");
        }
        if (Gdx.input.isKeyJustPressed(Keys.BACKSPACE)) {
            if (ipInput.length() > 0)
                ipInput.deleteCharAt(ipInput.length() - 1);
        }
    }

    public void draw(SpriteBatch batch, ShapeRenderer shape,
                     int screenW, int screenH) {
        Gdx.gl.glClearColor(0.08f, 0.08f, 0.12f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0.8f, 0.2f, 0.2f, 1f);
        shape.rect(0, screenH - 8, screenW, 8);
        shape.rect(0, 0, screenW, 8);
        shape.end();

        batch.begin();

        // Title
        titleFont.setColor(1f, 0.85f, 0.1f, 1f);
        String title = "MULTIPLAYER";
        layout.setText(titleFont, title);
        titleFont.draw(batch, title,
            (screenW - layout.width) / 2f, screenH - 50);

        if (state == 0) {
            // Show Host / Join / Back options
            String[] opts = {"HOST GAME", "JOIN GAME", "BACK"};
            int startY = screenH / 2 + 60;

            for (int i = 0; i < opts.length; i++) {
                if (i == selectedOption) {
                    menuFont.setColor(1f, 0.3f, 0.3f, 1f);
                    menuFont.draw(batch, ">",
                        screenW / 2f - 140, startY - i * 70);
                } else {
                    menuFont.setColor(0.85f, 0.85f, 0.85f, 1f);
                }
                layout.setText(menuFont, opts[i]);
                menuFont.draw(batch, opts[i],
                    (screenW - layout.width) / 2f,
                    startY - i * 70);
            }

            // Show local IP for host reference
            inputFont.setColor(0.5f, 0.5f, 0.5f, 1f);
            inputFont.draw(batch,
                "Your IP: " + network.getLocalIP(),
                20, 40);

        } else if (state == 2) {
            // IP input screen
            menuFont.setColor(Color.WHITE);
            menuFont.getData().setScale(1.5f);
            layout.setText(menuFont, "Enter Host IP Address:");
            menuFont.draw(batch, "Enter Host IP Address:",
                (screenW - layout.width) / 2f,
                screenH / 2f + 60);

            // Input box
            batch.end();
            shape.begin(ShapeRenderer.ShapeType.Filled);
            shape.setColor(0.15f, 0.15f, 0.2f, 1f);
            shape.rect(screenW / 2f - 150, screenH / 2f - 20, 300, 45);
            shape.setColor(0.8f, 0.2f, 0.2f, 1f);
            shape.rect(screenW / 2f - 150, screenH / 2f - 22, 300, 3);
            shape.end();
            batch.begin();

            // Blinking cursor
            String cursor = (timer % 1f > 0.5f) ? "|" : "";
            inputFont.setColor(Color.WHITE);
            inputFont.getData().setScale(1.8f);
            String ipText = ipInput.toString() + cursor;
            layout.setText(inputFont, ipText);
            inputFont.draw(batch, ipText,
                (screenW - layout.width) / 2f,
                screenH / 2f + 15);

            inputFont.getData().setScale(1f);
            inputFont.setColor(0.5f, 0.5f, 0.5f, 1f);
            inputFont.draw(batch,
                "Type IP address then press ENTER   ESC to go back",
                120, 40);
            menuFont.getData().setScale(2f);
        }

        batch.end();
    }

    public void dispose() {
        titleFont.dispose();
        menuFont.dispose();
        inputFont.dispose();
    }
}
