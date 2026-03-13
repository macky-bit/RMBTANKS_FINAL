package com.maraba.rmbtanks.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class PauseScreen {

    private final String[] options = {"RESUME", "BACK TO HOME", "EXIT GAME"};
    private int selectedOption = 0;

    private BitmapFont titleFont, menuFont;
    private GlyphLayout layout;
    private float timer = 0f;

    public PauseScreen() {
        titleFont = new BitmapFont();
        titleFont.getData().setScale(3f);
        menuFont  = new BitmapFont();
        menuFont.getData().setScale(2f);
        layout    = new GlyphLayout();
    }

    public int update() {
        timer += Gdx.graphics.getDeltaTime();

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

        // ── PAUSE PANEL ────────────────────────────────
        float panelW = 320f;
        float panelH = 320f;
        float panelX = (screenW - panelW) / 2f;
        float panelY = (screenH - panelH) / 2f;

        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0.1f, 0.1f, 0.15f, 1f);
        shape.rect(panelX, panelY, panelW, panelH);
        shape.setColor(0.8f, 0.2f, 0.1f, 1f);
        shape.rect(panelX, panelY + panelH - 6, panelW, 6);
        shape.rect(panelX, panelY, panelW, 6);
        shape.end();

        // ── PANEL BORDER ───────────────────────────────
        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.setColor(0.8f, 0.2f, 0.1f, 0.8f);
        shape.rect(panelX, panelY, panelW, panelH);
        shape.end();

        batch.begin();

        // ── TITLE ──────────────────────────────────────
        titleFont.setColor(1f, 0.85f, 0.1f, 1f);
        String title = "PAUSED";
        layout.setText(titleFont, title);
        titleFont.draw(batch, title,
            (screenW - layout.width) / 2f,
            panelY + panelH - 20);

        batch.end();

        // ── DIVIDER ────────────────────────────────────
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0.8f, 0.2f, 0.1f, 0.5f);
        shape.rect(panelX + 20, panelY + panelH - 80, panelW - 40, 2);
        shape.end();

        batch.begin();

        // ── MENU OPTIONS ───────────────────────────────
        float startY  = panelY + panelH - 110;
        float spacing = 70f;

        for (int i = 0; i < options.length; i++) {
            float itemY = startY - i * spacing;

            if (i == selectedOption) {
                batch.end();
                shape.begin(ShapeRenderer.ShapeType.Filled);
                shape.setColor(0.8f, 0.2f, 0.1f, 0.5f);
                shape.rect(panelX + 15, itemY - 28, panelW - 30, 38);
                shape.end();
                batch.begin();

                float pulse = (float)(Math.sin(timer * 4f) * 0.15f + 0.85f);
                menuFont.setColor(1f, 0.9f, 0.1f, pulse);
                menuFont.getData().setScale(1.8f);
                menuFont.draw(batch, ">", panelX + 20, itemY);
                menuFont.draw(batch, "<", panelX + panelW - 35, itemY);
                menuFont.getData().setScale(2f);

            } else {
                menuFont.getData().setScale(2f);
                if (i == 2) {
                    menuFont.setColor(1f, 0.4f, 0.4f, 0.9f);
                } else {
                    menuFont.setColor(0.85f, 0.85f, 0.85f, 1f);
                }
            }

            layout.setText(menuFont, options[i]);
            menuFont.draw(batch, options[i],
                (screenW - layout.width) / 2f,
                itemY);
        }

        // ── HINT ───────────────────────────────────────
        menuFont.getData().setScale(0.85f);
        menuFont.setColor(0.5f, 0.5f, 0.5f, 1f);
        String hint = "UP/DOWN navigate   ENTER select";
        layout.setText(menuFont, hint);
        menuFont.draw(batch, hint,
            (screenW - layout.width) / 2f,
            panelY + 20);

        batch.end();
    }

    public void dispose() {
        titleFont.dispose();
        menuFont.dispose();
    }
}
