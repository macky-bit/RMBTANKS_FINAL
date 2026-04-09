package com.maraba.rmbtanks.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.maraba.rmbtanks.entities.Player;
import com.maraba.rmbtanks.network.GamePacket;
import com.maraba.rmbtanks.network.NetworkManager;

public class GameScreen {

    // ── MATCH SETTINGS ─────────────────────────────────
    static final float MATCH_TIME    = 180f; // 3 minutes
    static final float RESPAWN_TIME  = 3f;
    static final int   BULLET_DAMAGE = 25;

    private Player player;
    private int screenW, screenH;

    // ── NETWORK ────────────────────────────────────────
    private NetworkManager network;

    // ── REMOTE PLAYER (drawn from network data) ────────
    private float remoteX, remoteY, remoteAngle;
    private int   remoteHealth = 100;
    private int   remoteKills  = 0;
    private boolean remoteAlive = true;
    private Texture remoteTankTexture;

    // ── MATCH STATE ────────────────────────────────────
    private float matchTimer    = MATCH_TIME;
    private float respawnTimer  = 0f;
    private int   myKills       = 0;
    private boolean matchOver   = false;
    private String  winMessage  = "";

    // ── FONTS ──────────────────────────────────────────
    private BitmapFont font;
    private GlyphLayout layout;

    public GameScreen(Texture myTankTexture, Texture remoteTankTexture,
                      Texture bulletTexture,
                      NetworkManager network,
                      int screenW, int screenH) {
        this.screenW           = screenW;
        this.screenH           = screenH;
        this.network           = network;           // can be null in solo
        this.remoteTankTexture = remoteTankTexture; // can be null in solo

        float startX = (screenW - Player.DRAW_W) / 2f;
        float startY = (screenH - Player.DRAW_H) / 2f;
        player = new Player(myTankTexture, bulletTexture, startX, startY);

        remoteX = screenW - Player.DRAW_W - 50;
        remoteY = screenH - Player.DRAW_H - 50;

        font   = new BitmapFont();
        layout = new GlyphLayout();
    }

    public void update() {
        float delta = Gdx.graphics.getDeltaTime();

        if (matchOver) return;

        // Solo mode — no timer
        if (network != null) {
            matchTimer -= delta;
            if (matchTimer <= 0) {
                matchTimer = 0;
                endMatch();
                return;
            }
        }

        if (!player.alive) {
            respawnTimer -= delta;
            if (respawnTimer <= 0) respawnPlayer();
        }

        if (player.alive) {
            player.update(delta, screenW, screenH);
        }

        // Only do network stuff if multiplayer
        if (network != null) {
            GamePacket packet = new GamePacket(
                player.x, player.y, player.angle,
                player.health, myKills, respawnTimer
            );
            if (!player.getBullets().isEmpty()) {
                Player.Bullet last = player.getBullets()
                    .get(player.getBullets().size() - 1);
                packet.firedBullet = true;
                packet.bulletX     = last.x;
                packet.bulletY     = last.y;
                packet.bulletAngle = last.angle;
            }
            network.send(packet);

            GamePacket received = network.getLatestPacket();
            if (received != null) {
                remoteX     = received.x;
                remoteY     = received.y;
                remoteAngle = received.angle;
                remoteHealth = received.health;
                remoteKills  = received.kills;
                remoteAlive  = received.health > 0;
            }
        }
    }

    void respawnPlayer() {
        player.health = 100;
        player.alive  = true;
        // Respawn at random corner
        float[][] corners = {
            {50, 50},
            {screenW - 100, 50},
            {50, screenH - 150},
            {screenW - 100, screenH - 150}
        };
        int c = (int)(Math.random() * 4);
        player.x = corners[c][0];
        player.y = corners[c][1];
    }

    void endMatch() {
        matchOver = true;
        if (myKills > remoteKills) {
            winMessage = "YOU WIN!";
        } else if (remoteKills > myKills) {
            winMessage = "YOU LOSE!";
        } else {
            // Tiebreaker — highest HP
            if (player.health >= remoteHealth) {
                winMessage = "YOU WIN! (HP Tiebreaker)";
            } else {
                winMessage = "YOU LOSE! (HP Tiebreaker)";
            }
        }
    }

    public void draw(SpriteBatch batch, ShapeRenderer shape) {

        // Ground
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0.2f, 0.35f, 0.15f, 1f);
        shape.rect(0, 0, screenW, screenH);
        shape.end();

        // Particles
        player.drawParticles(shape);

        // Health bars
        shape.begin(ShapeRenderer.ShapeType.Filled);
        player.drawHealthBar(shape);
        if (network != null) drawRemoteHealthBar(shape);
        shape.end();

        // Tanks
        batch.begin();
        if (player.alive) player.draw(batch);
        if (network != null && remoteAlive) drawRemoteTank(batch);
        batch.end();

        // HUD — only show timer in multiplayer
        if (network != null) drawHUD(batch, shape);

        // Solo HUD — just show controls hint
        if (network == null) drawSoloHUD(batch);

        if (!player.alive && !matchOver) drawRespawnTimer(batch);
        if (matchOver) drawMatchOver(batch, shape);
    }

    void drawRemoteTank(SpriteBatch batch) {
        batch.draw(
            remoteTankTexture,
            remoteX, remoteY,
            Player.DRAW_W / 2f, Player.DRAW_H / 2f,
            Player.DRAW_W, Player.DRAW_H,
            1f, 1f,
            remoteAngle - 90f,
            0, 0,
            remoteTankTexture.getWidth(),
            remoteTankTexture.getHeight(),
            false, false
        );
    }

    void drawRemoteHealthBar(ShapeRenderer shape) {
        float barW    = 50f;
        float barH    = 6f;
        float barX    = remoteX + (Player.DRAW_W - barW) / 2f;
        float barY    = remoteY + Player.DRAW_H + 8f;
        float percent = remoteHealth / 100f;

        shape.setColor(0.4f, 0f, 0f, 1f);
        shape.rect(barX, barY, barW, barH);

        if (percent > 0.5f)      shape.setColor(0.2f, 0.85f, 0.2f, 1f);
        else if (percent > 0.25f) shape.setColor(1f, 0.85f, 0f, 1f);
        else                      shape.setColor(1f, 0.2f, 0.2f, 1f);

        shape.rect(barX, barY, barW * percent, barH);
    }

    void drawHUD(SpriteBatch batch, ShapeRenderer shape) {
        // Timer background
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0f, 0f, 0f, 0.5f);
        shape.rect(screenW / 2f - 50, screenH - 40, 100, 32);
        shape.end();

        batch.begin();

        // Match timer
        int mins = (int)(matchTimer / 60);
        int secs = (int)(matchTimer % 60);
        font.getData().setScale(1.5f);
        font.setColor(matchTimer < 30 ?
            com.badlogic.gdx.graphics.Color.RED :
            com.badlogic.gdx.graphics.Color.WHITE);
        String timeStr = String.format("%d:%02d", mins, secs);
        layout.setText(font, timeStr);
        font.draw(batch, timeStr,
            (screenW - layout.width) / 2f, screenH - 12);

        // Kills
        font.getData().setScale(1.2f);
        font.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        font.draw(batch, "YOU: " + myKills, 10, screenH - 12);
        font.draw(batch, "OPP: " + remoteKills,
            screenW - 90, screenH - 12);

        batch.end();
    }

    void drawSoloHUD(SpriteBatch batch) {
        batch.begin();
        font.getData().setScale(0.9f);
        font.setColor(0.5f, 0.5f, 0.5f, 1f);
        font.draw(batch,
            "ARROWS=Move   SPACE=Shoot   ESC=Menu",
            170, 20);
        batch.end();
    }

    void drawRespawnTimer(SpriteBatch batch) {
        batch.begin();
        font.getData().setScale(2f);
        font.setColor(1f, 0.8f, 0.2f, 1f);
        String msg = "Respawning in " + (int)(respawnTimer + 1) + "...";
        layout.setText(font, msg);
        font.draw(batch, msg,
            (screenW - layout.width) / 2f,
            screenH / 2f);
        batch.end();
    }

    void drawMatchOver(SpriteBatch batch, ShapeRenderer shape) {
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0f, 0f, 0f, 0.7f);
        shape.rect(0, 0, screenW, screenH);
        shape.end();

        batch.begin();
        font.getData().setScale(3f);
        boolean won = winMessage.startsWith("YOU WIN");
        font.setColor(won ?
            com.badlogic.gdx.graphics.Color.GREEN :
            com.badlogic.gdx.graphics.Color.RED);
        layout.setText(font, winMessage);
        font.draw(batch, winMessage,
            (screenW - layout.width) / 2f,
            screenH / 2f + 40);

        font.getData().setScale(1.2f);
        font.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        String score = "Kills: " + myKills + "  vs  " + remoteKills;
        layout.setText(font, score);
        font.draw(batch, score,
            (screenW - layout.width) / 2f,
            screenH / 2f - 20);

        font.getData().setScale(1f);
        font.setColor(0.6f, 0.6f, 0.6f, 1f);
        font.draw(batch, "Press ESC to return to menu",
            screenW / 2f - 120, 40);
        batch.end();
    }

    public boolean isMatchOver() { return matchOver; }

    public void dispose() {
        font.dispose();
    }
}
