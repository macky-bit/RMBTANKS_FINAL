package com.maraba.rmbtanks.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class SelectionScreen {

    // ── CHANGE THESE TO RESIZE TANKS ──────────────────
    static final int TANK_W   = 100;
    static final int TANK_H   = 175;
    static final int TANK_GAP = 60;
    // ──────────────────────────────────────────────────

    private Texture tank1, tank2, tank3;
    private int selectedTank = 0;

    public SelectionScreen(Texture tank1, Texture tank2, Texture tank3) {
        this.tank1 = tank1;
        this.tank2 = tank2;
        this.tank3 = tank3;
    }

    // Returns true when player pressed ENTER to confirm
    public boolean update() {
        if (Gdx.input.isKeyJustPressed(Keys.LEFT)) {
            selectedTank = (selectedTank - 1 + 3) % 3;
        }
        if (Gdx.input.isKeyJustPressed(Keys.RIGHT)) {
            selectedTank = (selectedTank + 1) % 3;
        }
        if (Gdx.input.isKeyJustPressed(Keys.ENTER)) {
            return true; // confirmed!
        }
        return false;
    }

    public void draw(SpriteBatch batch, ShapeRenderer shape, int screenW, int screenH) {
        int totalWidth = (TANK_W * 3) + (TANK_GAP * 2);
        int startX     = (screenW - totalWidth) / 2;
        int tankY      = (screenH - TANK_H)     / 2;

        int x1 = startX;
        int x2 = startX + TANK_W + TANK_GAP;
        int x3 = startX + (TANK_W + TANK_GAP) * 2;

        // Highlight box
        int[] xPositions = {x1, x2, x3};
        int padding = 10;
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0.2f, 0.6f, 1f, 1f);
        shape.rect(
            xPositions[selectedTank] - padding,
            tankY - padding,
            TANK_W + padding * 2,
            TANK_H + padding * 2
        );
        shape.end();

        // Draw tanks
        batch.begin();
        batch.draw(tank1, x1, tankY, TANK_W, TANK_H);
        batch.draw(tank2, x2, tankY, TANK_W, TANK_H);
        batch.draw(tank3, x3, tankY, TANK_W, TANK_H);
        batch.end();
    }

    public int getSelectedTank() {
        return selectedTank;
    }
}
