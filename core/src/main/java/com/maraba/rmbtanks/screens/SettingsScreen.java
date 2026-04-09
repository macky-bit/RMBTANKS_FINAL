package com.maraba.rmbtanks.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class SettingsScreen {

    private BitmapFont font;
    private BitmapFont titleFont;
    private GlyphLayout layout;

    // ── SETTINGS VALUES ────────────────────────────────
    public int tankSpeed    = 2;  // 1=Slow 2=Normal 3=Fast
    public boolean sfxOn    = true;
    private int selectedRow = 0;
    private final int ROWS  = 2;

    public SettingsScreen() {
        font = new BitmapFont();
        font.getData().setScale(1.5f);
        titleFont = new BitmapFont();
        titleFont.getData().setScale(2.5f);
        layout = new GlyphLayout();
    }

    // Returns true when ESC pressed to go back
    public boolean update() {
        if (Gdx.input.isKeyJustPressed(Keys.UP)) {
            selectedRow = (selectedRow - 1 + ROWS) % ROWS;
        }
        if (Gdx.input.isKeyJustPressed(Keys.DOWN)) {
            selectedRow = (selectedRow + 1) % ROWS;
        }
        if (Gdx.input.isKeyJustPressed(Keys.LEFT)) {
            if (selectedRow == 0) tankSpeed = Math.max(1, tankSpeed - 1);
            if (selectedRow == 1) sfxOn = !sfxOn;
        }
        if (Gdx.input.isKeyJustPressed(Keys.RIGHT)) {
            if (selectedRow == 0) tankSpeed = Math.min(3, tankSpeed + 1);
            if (selectedRow == 1) sfxOn = !sfxOn;
        }
        return Gdx.input.isKeyJustPressed(Keys.ESCAPE);
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
        String title = "SETTINGS";
        layout.setText(titleFont, title);
        titleFont.draw(batch, title,
            (screenW - layout.width) / 2f,
            screenH - 50);

        // Tank Speed setting
        font.getData().setScale(1.5f);
        font.setColor(selectedRow == 0 ? Color.RED : Color.WHITE);
        String[] speeds = {"SLOW", "NORMAL", "FAST"};
        font.draw(batch,
            "TANK SPEED:  <  " + speeds[tankSpeed - 1] + "  >",
            160, screenH / 2 + 60);

        // SFX setting
        font.setColor(selectedRow == 1 ? Color.RED : Color.WHITE);
        font.draw(batch,
            "SOUND FX:    <  " + (sfxOn ? "ON" : "OFF") + "  >",
            160, screenH / 2 - 10);

        // Hint
        font.getData().setScale(0.9f);
        font.setColor(0.5f, 0.5f, 0.5f, 1f);
        font.draw(batch,
            "UP/DOWN select   LEFT/RIGHT change   ESC back",
            130, 30);

        batch.end();
    }

    public void dispose() {
        font.dispose();
        titleFont.dispose();
    }
}
