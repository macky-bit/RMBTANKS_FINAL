package com.maraba.rmbtanks.screens.GameScreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class SelectionScreen {

    // ── TANK DISPLAY ───────────────────────────────────
    static final int TANK_W   = 120;
    static final int TANK_H   = 210;
    static final int TANK_GAP = 80;

    // ── TANK NAMES & STATS ─────────────────────────────
    private final String[] tankNames = {"IRON WOLF", "STEEL HAWK", "WAR BEAR"};
    private final String[] tankDescs = {"", "", ""};

    private Texture tank1, tank2, tank3;
    private Texture background;
    private int selectedTank = 0;
    private float timer = 0f;

    private BitmapFont titleFont, nameFont, hintFont;
    private GlyphLayout layout;

    public SelectionScreen(Texture tank1, Texture tank2, Texture tank3) {
        this.tank1 = tank1;
        this.tank2 = tank2;
        this.tank3 = tank3;
        background = new Texture(Gdx.files.internal("BGImages/BGNT.png"));

        titleFont = new BitmapFont();
        titleFont.getData().setScale(3.5f);

        nameFont = new BitmapFont();
        nameFont.getData().setScale(1.8f);

        hintFont = new BitmapFont();
        hintFont.getData().setScale(1f);

        layout = new GlyphLayout();
    }

    public boolean update() {
        timer += Gdx.graphics.getDeltaTime();

        if (Gdx.input.isKeyJustPressed(Keys.LEFT)) {
            selectedTank = (selectedTank - 1 + 3) % 3;
        }
        if (Gdx.input.isKeyJustPressed(Keys.RIGHT)) {
            selectedTank = (selectedTank + 1) % 3;
        }
        if (Gdx.input.isKeyJustPressed(Keys.ENTER)) {
            return true;
        }
        return false;
    }

    public void draw(SpriteBatch batch, ShapeRenderer shape, int screenW, int screenH) {

        // ── 1. BACKGROUND ──────────────────────────────
        float imgW   = background.getWidth();
        float imgH   = background.getHeight();
        float scale  = Math.max((float) screenW / imgW, (float) screenH / imgH);
        float drawW  = imgW * scale;
        float drawH  = imgH * scale;
        float drawX  = (screenW - drawW) / 2f;
        float drawY  = (screenH - drawH) / 2f;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        batch.enableBlending();
        batch.begin();
        batch.setColor(1f, 1f, 1f, 1f);
        batch.draw(background, drawX, drawY, drawW, drawH);
        batch.end();

        // ── 2. DARK OVERLAY ────────────────────────────


        // ── 3. TITLE BAR ───────────────────────────────
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0.1f, 0.1f, 0.15f, 0.9f);
        shape.rect(0, screenH - 80, screenW, 80);
        shape.setColor(0.8f, 0.2f, 0.1f, 1f);
        shape.rect(0, screenH - 84, screenW, 4);
        shape.end();

        batch.begin();
        titleFont.setColor(1f, 0.85f, 0.1f, 1f);
        String title = "SELECT YOUR TANK";
        layout.setText(titleFont, title);
        titleFont.draw(batch, title,
            (screenW - layout.width) / 2f,
            screenH - 20);
        batch.end();

        // ── 4. TANKS ───────────────────────────────────
        int totalWidth = (TANK_W * 3) + (TANK_GAP * 2);
        int startX     = (screenW - totalWidth) / 2;
        int tankY      = (screenH - TANK_H) / 2 - 10;

        int[] xPos = {
            startX,
            startX + TANK_W + TANK_GAP,
            startX + (TANK_W + TANK_GAP) * 2
        };

        Texture[] tanks = {tank1, tank2, tank3};

        for (int i = 0; i < 3; i++) {
            int tx = xPos[i];
            boolean selected = (i == selectedTank);
            float pulse = (float)(Math.sin(timer * 4f) * 0.15f + 0.85f);

            // ── CARD BACKGROUND ────────────────────────
            shape.begin(ShapeRenderer.ShapeType.Filled);
            if (selected) {
                shape.setColor(0.15f, 0.15f, 0.22f, 0.95f);
            } else {
                shape.setColor(0.08f, 0.08f, 0.12f, 0.8f);
            }
            shape.rect(tx - 15, tankY - 50, TANK_W + 30, TANK_H + 110);
            shape.end();

            // ── CARD BORDER ────────────────────────────
            shape.begin(ShapeRenderer.ShapeType.Line);
            if (selected) {
                shape.setColor(0.8f, 0.2f, 0.1f, pulse);
            } else {
                shape.setColor(0.4f, 0.4f, 0.4f, 0.5f);
            }
            shape.rect(tx - 15, tankY - 50, TANK_W + 30, TANK_H + 110);
            shape.end();

            // ── TOP COLOR BAR ──────────────────────────
            shape.begin(ShapeRenderer.ShapeType.Filled);
            if (selected) {
                shape.setColor(0.8f, 0.2f, 0.1f, 1f);
            } else {
                shape.setColor(0.3f, 0.3f, 0.3f, 0.8f);
            }
            shape.rect(tx - 15, tankY + TANK_H + 58, TANK_W + 30, 4);
            shape.end();

            // ── TANK IMAGE ─────────────────────────────
            batch.begin();
            if (selected) {
                batch.setColor(1f, 1f, 1f, 1f);
            } else {
                batch.setColor(0.6f, 0.6f, 0.6f, 1f);
            }
            batch.draw(tanks[i], tx, tankY, TANK_W, TANK_H);
            batch.setColor(1f, 1f, 1f, 1f);
            batch.end();

            // ── TANK NUMBER ────────────────────────────
            batch.begin();
            hintFont.getData().setScale(1f);
            hintFont.setColor(0.5f, 0.5f, 0.5f, 1f);
            String num = "0" + (i + 1);
            layout.setText(hintFont, num);
            hintFont.draw(batch, num,
                tx + (TANK_W - layout.width) / 2f,
                tankY + TANK_H + 55);
            batch.end();

            // ── TANK NAME ──────────────────────────────
            batch.begin();
            nameFont.getData().setScale(1.5f);
            if (selected) {
                nameFont.setColor(1f, 0.85f, 0.1f, pulse);
            } else {
                nameFont.setColor(0.7f, 0.7f, 0.7f, 1f);
            }
            layout.setText(nameFont, tankNames[i]);
            nameFont.draw(batch, tankNames[i],
                tx + (TANK_W - layout.width) / 2f,
                tankY - 10);
            batch.end();

            // ── TANK DESC ──────────────────────────────
            batch.begin();
            hintFont.getData().setScale(0.9f);
            if (selected) {
                hintFont.setColor(0.8f, 0.5f, 0.2f, 1f);
            } else {
                hintFont.setColor(0.5f, 0.5f, 0.5f, 1f);
            }
            layout.setText(hintFont, tankDescs[i]);
            hintFont.draw(batch, tankDescs[i],
                tx + (TANK_W - layout.width) / 2f,
                tankY - 28);
            batch.end();
        }

        // ── 5. ARROWS ──────────────────────────────────
        batch.begin();
        nameFont.getData().setScale(2f);
        nameFont.setColor(1f, 0.85f, 0.1f, 1f);
        nameFont.draw(batch, "<", xPos[0] - 50, screenH / 2f);
        nameFont.draw(batch, ">", xPos[2] + TANK_W + 20, screenH / 2f);
        batch.end();

        // ── 6. BOTTOM HINT ─────────────────────────────
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0f, 0f, 0f, 0.6f);
        shape.rect(0, 0, screenW, 40);
        shape.end();

        batch.begin();
        hintFont.getData().setScale(1f);
        hintFont.setColor(0.6f, 0.6f, 0.6f, 1f);
        String hint = "LEFT / RIGHT   navigate        ENTER   confirm";
        layout.setText(hintFont, hint);
        hintFont.draw(batch, hint,
            (screenW - layout.width) / 2f, 28);
        batch.end();
    }

    public int getSelectedTank() { return selectedTank; }

    public void dispose() {
        background.dispose();
        titleFont.dispose();
        nameFont.dispose();
        hintFont.dispose();
    }
}
