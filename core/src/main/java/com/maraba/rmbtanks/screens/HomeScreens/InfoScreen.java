package com.maraba.rmbtanks.screens.HomeScreens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class InfoScreen {

    private BitmapFont font;
    private BitmapFont titleFont;
    private GlyphLayout layout;

    public InfoScreen() {
        font = new BitmapFont();
        font.getData().setScale(1.2f);
        titleFont = new BitmapFont();
        titleFont.getData().setScale(2.5f);
        layout = new GlyphLayout();
    }

    // Returns true when player presses ESC to go back
    public boolean update() {
        return Gdx.input.isKeyJustPressed(Keys.ESCAPE);
    }

    public void draw(SpriteBatch batch, ShapeRenderer shape,
                     int screenW, int screenH) {
        Gdx.gl.glClearColor(0.08f, 0.08f, 0.12f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Top bar
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0.8f, 0.2f, 0.2f, 1f);
        shape.rect(0, screenH - 8, screenW, 8);
        shape.rect(0, 0, screenW, 8);
        shape.end();

        batch.begin();

        // Title
        titleFont.setColor(1f, 0.85f, 0.1f, 1f);
        String title = "HOW TO PLAY";
        layout.setText(titleFont, title);
        titleFont.draw(batch, title,
            (screenW - layout.width) / 2f,
            screenH - 50);

        // Controls info
        font.setColor(Color.WHITE);
        font.getData().setScale(1.3f);

        String[] lines = {
            "DESCRIPTION",
            "  RMB TANKS IS A VERSION OF AZ TANKS THAT WE CREATE",
            "  ",
            "",
            "WE HOPE YOU ENJOY OUR MINI GAME",
            "  ",
            "CREATOR",
            "  MANZANO, MARK ENGELS L",
            "  BORJA, RASHEED JERMAIN P",
            "  MUNAR, MARK BRANDON",
            "",
            "ROLES",
            "  MAIN PROGRAMMER - MANZANO",
            "  MAP DESIGNER - BORJA",
            "  OBSTACLE DESIGNER - MUNAR",
        };

        int startY = screenH - 130;
        for (String line : lines) {
            if (line.equals("MOVEMENT") || line.equals("COMBAT")
                || line.equals("NAVIGATION") || line.equals("OBJECTIVE")) {
                font.setColor(1f, 0.4f, 0.4f, 1f); // red headers
            } else {
                font.setColor(0.9f, 0.9f, 0.9f, 1f);
            }
            font.draw(batch, line, 120, startY);
            startY -= 30;
        }

        // Back hint
        font.getData().setScale(0.9f);
        font.setColor(0.5f, 0.5f, 0.5f, 1f);
        font.draw(batch, "Press ESC to go back", 300, 30);

        batch.end();
    }

    public void dispose() {
        font.dispose();
        titleFont.dispose();
    }
}
