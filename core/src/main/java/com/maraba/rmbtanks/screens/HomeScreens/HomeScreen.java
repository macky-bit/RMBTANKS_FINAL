package com.maraba.rmbtanks.screens.HomeScreens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class HomeScreen {

    private final String[] options = {"SOLO PLAY", "MULTIPLAYER", "SETTINGS", "INFO"};
    private int selectedOption = 0;

    private BitmapFont menuFont;
    private GlyphLayout layout;
    private Texture background;
    private float timer = 0f;

    public HomeScreen() {
        menuFont   = new BitmapFont();
        layout     = new GlyphLayout();
        background = new Texture("BGImages/BGIMG.png");
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

        batch.begin();

        // ── BACKGROUND IMAGE — force fill entire screen ─
        float imgW = background.getWidth();
        float imgH = background.getHeight();

// Scale to fill — use whichever scale is larger
        float scaleX = (float) screenW / imgW;
        float scaleY = (float) screenH / imgH;
        float scale  = Math.max(scaleX, scaleY);

        float drawW  = imgW * scale;
        float drawH  = imgH * scale;

// Center it
        float drawX  = (screenW - drawW) / 2f;
        float drawY  = (screenH - drawH) / 2f;

        batch.draw(background, drawX, drawY, drawW, drawH);

        // ── MENU OPTIONS ───────────────────────────────
        int startY   = screenH / 2 - 10;
        int spacing  = 55;

        for (int i = 0; i < options.length; i++) {
            if (i == selectedOption) {
                float pulse = (float)(Math.sin(timer * 4f) * 0.15f + 0.85f);

                // Highlight box behind selected
                batch.end();
                shape.begin(ShapeRenderer.ShapeType.Filled);
                shape.setColor(0.8f, 0.2f, 0.1f, 0.6f);
                shape.rect(screenW / 2f - 130,
                    startY - i * spacing - 28,
                    260, 38);
                shape.end();
                batch.begin();

                menuFont.getData().setScale(2f);
                menuFont.setColor(1f, 0.9f, 0.1f, pulse); // gold pulse

                // Arrows
                menuFont.getData().setScale(1.5f);
                menuFont.draw(batch, ">",
                    screenW / 2f - 120,
                    startY - i * spacing);
                menuFont.draw(batch, "<",
                    screenW / 2f + 95,
                    startY - i * spacing);
                menuFont.getData().setScale(2f);

            } else {
                menuFont.getData().setScale(2f);
                menuFont.setColor(0.85f, 0.85f, 0.85f, 0.9f);
            }

            layout.setText(menuFont, options[i]);
            menuFont.draw(batch, options[i],
                (screenW - layout.width) / 2f,
                startY - i * spacing);
        }

        // ── CONTROLS HINT ──────────────────────────────
        menuFont.getData().setScale(0.85f);
        menuFont.setColor(0.6f, 0.6f, 0.6f, 0.8f);
        String hint = "UP / DOWN   navigate       ENTER   select";
        layout.setText(menuFont, hint);
        menuFont.draw(batch, hint,
            (screenW - layout.width) / 2f, 25);

        batch.end();
    }

    public void dispose() {
        menuFont.dispose();
        background.dispose();
    }
}
