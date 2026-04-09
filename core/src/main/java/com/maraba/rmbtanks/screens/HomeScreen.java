package com.maraba.rmbtanks.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class HomeScreen {

    // ── MENU OPTIONS ───────────────────────────────────
    private final String[] options = {"SOLO", "PLAY", "SETTINGS", "INFO"};
    private int selectedOption = 0;

    // ── FONTS ──────────────────────────────────────────
    private BitmapFont titleFont;
    private BitmapFont menuFont;
    private GlyphLayout layout;

    // ── ANIMATION ──────────────────────────────────────
    private float timer = 0f;  // for pulsing effect

    public HomeScreen() {
        titleFont = new BitmapFont();
        titleFont.getData().setScale(3.5f);
        titleFont.setColor(Color.WHITE);

        menuFont = new BitmapFont();
        menuFont.getData().setScale(2f);

        layout = new GlyphLayout();
    }

    // Returns which option was selected (-1 = none yet)
    // 0 = PLAY, 1 = SETTINGS, 2 = INFO
    public int update() {
        // Navigate
        if (Gdx.input.isKeyJustPressed(Keys.UP)) {
            selectedOption = (selectedOption - 1 + options.length) % options.length;
        }
        if (Gdx.input.isKeyJustPressed(Keys.DOWN)) {
            selectedOption = (selectedOption + 1) % options.length;
        }
        if (Gdx.input.isKeyJustPressed(Keys.ENTER)) {
            return selectedOption;
        }
        return -1;
    }

    public void draw(SpriteBatch batch, ShapeRenderer shape,
                     int screenW, int screenH) {
        timer += Gdx.graphics.getDeltaTime();

        // ── BACKGROUND ─────────────────────────────────
        Gdx.gl.glClearColor(0.08f, 0.08f, 0.12f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // ── DECORATIVE TOP BAR ─────────────────────────
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0.8f, 0.2f, 0.2f, 1f);
        shape.rect(0, screenH - 8, screenW, 8);
        // Bottom bar
        shape.rect(0, 0, screenW, 8);
        // Side accents
        shape.setColor(0.5f, 0.1f, 0.1f, 1f);
        shape.rect(0, 0, 6, screenH);
        shape.rect(screenW - 6, 0, 6, screenH);
        shape.end();

        batch.begin();

        // ── TITLE ──────────────────────────────────────
        titleFont.setColor(1f, 0.85f, 0.1f, 1f); // gold
        String title = "RMB TANKS";
        layout.setText(titleFont, title);
        titleFont.draw(batch, title,
            (screenW - layout.width) / 2f,
            screenH - 60);

        // ── SUBTITLE ───────────────────────────────────
        menuFont.getData().setScale(1f);
        menuFont.setColor(0.7f, 0.7f, 0.7f, 1f);
        String sub = "LAN MULTIPLAYER TANK BATTLE";
        layout.setText(menuFont, sub);
        menuFont.draw(batch, sub,
            (screenW - layout.width) / 2f,
            screenH - 110);

        // ── DIVIDER LINE ───────────────────────────────
        batch.end();
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0.8f, 0.2f, 0.2f, 0.6f);
        shape.rect((screenW - 300) / 2f, screenH - 130, 300, 2);
        shape.end();
        batch.begin();

        // ── MENU OPTIONS ───────────────────────────────
        menuFont.getData().setScale(2f);
        int startY = screenH / 2 + 60;
        int spacing = 70;

        for (int i = 0; i < options.length; i++) {
            if (i == selectedOption) {
                // Pulsing highlight color
                float pulse = (float)(Math.sin(timer * 4f) * 0.15f + 0.85f);
                menuFont.setColor(1f, 0.3f * pulse, 0.3f * pulse, 1f);

                // Draw selection arrows
                menuFont.getData().setScale(1.8f);
                menuFont.draw(batch, ">",
                    screenW / 2f - 120,
                    startY - i * spacing);
                menuFont.draw(batch, "<",
                    screenW / 2f + 90,
                    startY - i * spacing);
                menuFont.getData().setScale(2f);

            } else {
                menuFont.setColor(0.85f, 0.85f, 0.85f, 1f);
            }

            layout.setText(menuFont, options[i]);
            menuFont.draw(batch, options[i],
                (screenW - layout.width) / 2f,
                startY - i * spacing);
        }

        // ── CONTROLS HINT ──────────────────────────────
        menuFont.getData().setScale(0.8f);
        menuFont.setColor(0.5f, 0.5f, 0.5f, 1f);
        String hint = "UP/DOWN to navigate   ENTER to select";
        layout.setText(menuFont, hint);
        menuFont.draw(batch, hint,
            (screenW - layout.width) / 2f,
            40);

        batch.end();
    }

    public void dispose() {
        titleFont.dispose();
        menuFont.dispose();
    }
}
