package com.maraba.rmbtanks.screens.multiplayer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.maraba.rmbtanks.network.NetworkManager;

public class WaitingScreen {

    private BitmapFont font;
    private GlyphLayout layout;
    private NetworkManager network;
    private float timer = 0f;

    public WaitingScreen(NetworkManager network) {
        this.network = network;
        font   = new BitmapFont();
        layout = new GlyphLayout();
    }

    // Returns true when both players connected
    public boolean update() {
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
            network.disconnect();
        }
        return network.connected;
    }

    public void draw(SpriteBatch batch, ShapeRenderer shape,
                     int screenW, int screenH) {
        timer += Gdx.graphics.getDeltaTime();

        Gdx.gl.glClearColor(0.08f, 0.08f, 0.12f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0.8f, 0.2f, 0.2f, 1f);
        shape.rect(0, screenH - 8, screenW, 8);
        shape.rect(0, 0, screenW, 8);
        shape.end();

        batch.begin();

        // Animated dots
        int dots = (int)(timer * 2) % 4;
        String dotStr = ".".repeat(dots);

        font.getData().setScale(2f);
        font.setColor(1f, 0.85f, 0.1f, 1f);
        String msg = network.isHost
            ? "Waiting for opponent" + dotStr
            : "Connecting" + dotStr;
        layout.setText(font, msg);
        font.draw(batch, msg,
            (screenW - layout.width) / 2f,
            screenH / 2f + 30);

        font.getData().setScale(1.2f);
        font.setColor(0.5f, 0.5f, 0.5f, 1f);
        String status = network.statusMsg;
        layout.setText(font, status);
        font.draw(batch, status,
            (screenW - layout.width) / 2f,
            screenH / 2f - 20);

        font.getData().setScale(1f);
        font.setColor(0.4f, 0.4f, 0.4f, 1f);
        font.draw(batch, "ESC to cancel",
            screenW / 2f - 50, 40);

        batch.end();
    }

    public void dispose() {
        font.dispose();
    }
}
