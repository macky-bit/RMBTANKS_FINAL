package com.maraba.rmbtanks.screens.GameScreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.maraba.rmbtanks.Sound.SoundManager;
import com.maraba.rmbtanks.entities.Player;
import com.maraba.rmbtanks.network.GamePacket;
import com.maraba.rmbtanks.network.NetworkManager;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GameScreen {

    // ── MATCH SETTINGS ─────────────────────────────────
    static final float MATCH_TIME   = 180f;
    static final float RESPAWN_TIME = 3f;
    static final int  BULLET_DAMAGE = 25;

    private Player player;
    private int screenW, screenH;

    // ── SPAWN POINTS ───────────────────────────────────
    private float spawnX, spawnY;

    // ── NETWORK ────────────────────────────────────────
    private NetworkManager network;

    // ── REMOTE PLAYER ──────────────────────────────────
    private float   remoteX, remoteY, remoteAngle;
    private float   smoothRemoteX, smoothRemoteY, smoothRemoteAngle;
    private int     remoteHealth = 100;
    private int     remoteKills  = 0;
    private boolean remoteAlive  = true;
    private Texture remoteTankTexture;

    // ── REMOTE BULLETS ─────────────────────────────────
    private List<RemoteBullet> remoteBullets = new ArrayList<>();

    // ── OBSTACLE RECTANGLES ────────────────────────────
    private List<Rectangle> obstacles = new ArrayList<>();

    // ── MATCH STATE ────────────────────────────────────
    private float   matchTimer   = MATCH_TIME;
    private float   respawnTimer = 0f;
    private int     myKills      = 0;
    private boolean matchOver    = false;
    private String  winMessage   = "";

    // ── PAUSE ──────────────────────────────────────────
    private boolean     paused      = false;
    private PauseScreen pauseScreen = new PauseScreen();

    // ── MAP ────────────────────────────────────────────
    private TiledMap                   map;
    private OrthogonalTiledMapRenderer mapRenderer;
    private float                      mapScale;
    private float                      mapW;
    private float                      mapH;

    // ── VIEWPORT ───────────────────────────────────────
    private Viewport viewport;

    // ── FONTS ──────────────────────────────────────────
    private BitmapFont  font;
    private GlyphLayout layout;

    // ── REMOTE BULLET CLASS ────────────────────────────
    static class RemoteBullet {
        float x, y, vx, vy;
        boolean active = true;
        static final float SPEED = 1000f;

        RemoteBullet(float x, float y, float angle) {
            this.x  = x;
            this.y  = y;
            this.vx = (float) Math.cos(Math.toRadians(angle)) * SPEED;
            this.vy = (float) Math.sin(Math.toRadians(angle)) * SPEED;
        }

        void update(float delta) {
            x += vx * delta;
            y += vy * delta;
        }
    }

    // ── CONSTRUCTOR ────────────────────────────────────
    public GameScreen(Texture myTankTexture, Texture remoteTankTexture,
                      Texture bulletTexture,
                      NetworkManager network,
                      int screenW, int screenH,
                      SoundManager sound,
                      Viewport viewport) {
        this.screenW           = screenW;
        this.screenH           = screenH;
        this.network           = network;
        this.remoteTankTexture = remoteTankTexture;
        this.viewport          = viewport;

        // ── LOAD TILED MAP ─────────────────────────────
        map      = new TmxMapLoader().load("maps/map1.tmx");
        mapW     = 32 * 32f;
        mapH     = 25 * 32f;
        float scaleX = (float) screenW / mapW;
        float scaleY = (float) screenH / mapH;
        mapScale     = Math.min(scaleX, scaleY);
        mapRenderer  = new OrthogonalTiledMapRenderer(map, mapScale);

        // ── SPAWN POSITIONS ────────────────────────────
        // Player 1 — top-left safe zone
        spawnX = 60f;
        spawnY = screenH - Player.DRAW_H - 60f;
        player = new Player(myTankTexture, bulletTexture,
            spawnX, spawnY, sound);

        // ── LOAD OBSTACLE RECTANGLES ───────────────────
        try {
            com.badlogic.gdx.maps.MapLayer obsLayer =
                map.getLayers().get("obstacles");
            if (obsLayer != null) {
                MapObjects objects = obsLayer.getObjects();
                for (RectangleMapObject obj :
                    objects.getByType(RectangleMapObject.class)) {
                    Rectangle r = obj.getRectangle();
                    obstacles.add(new Rectangle(
                        r.x      * mapScale,
                        r.y      * mapScale,
                        r.width  * mapScale,
                        r.height * mapScale
                    ));
                }
                player.setObstacles(obstacles);
                Gdx.app.log("OBSTACLES",
                    "Loaded " + obstacles.size() + " rectangles");
            } else {
                Gdx.app.log("OBSTACLES", "No obstacles layer found!");
            }
        } catch (Exception e) {
            Gdx.app.error("OBSTACLES", "Error: " + e.getMessage());
        }

        // Player 2 — bottom-right safe zone
        remoteX       = screenW - Player.DRAW_W - 60f;
        remoteY       = 60f;
        smoothRemoteX = remoteX;
        smoothRemoteY = remoteY;

        font   = new BitmapFont();
        layout = new GlyphLayout();
    }

    // ── PAUSE CONTROLS ─────────────────────────────────
    public boolean isPaused()    { return paused; }
    public void    resume()      { paused = false; }
    public int     updatePause() { return pauseScreen.update(); }

    // ── LERP HELPERS ───────────────────────────────────
    private float lerp(float a, float b, float t) {
        return a + (b - a) * Math.min(t, 1f);
    }

    private float lerpAngle(float a, float b, float t) {
        float diff = b - a;
        while (diff >  180f) diff -= 360f;
        while (diff < -180f) diff += 360f;
        return a + diff * Math.min(t, 1f);
    }

    // ── UPDATE ─────────────────────────────────────────
    public void update() {
        float delta = Gdx.graphics.getDeltaTime();

        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)
            && !matchOver && !paused) {
            paused = true;
        }

        if (paused || matchOver) return;

        // ── MATCH TIMER ────────────────────────────────
        if (network != null) {
            matchTimer -= delta;
            if (matchTimer <= 0) {
                matchTimer = 0;
                endMatch();
                return;
            }
        }

        // ── RESPAWN COUNTDOWN ──────────────────────────
        if (!player.alive) {
            respawnTimer -= delta;
            if (respawnTimer <= 0) respawnPlayer();
            return;
        }

        // ── LOCAL PLAYER UPDATE ────────────────────────
        player.update(delta, screenW, screenH);

        // ── NETWORK ────────────────────────────────────
        if (network != null) {

            // ── SEND STATE ─────────────────────────────
            GamePacket packet = new GamePacket(
                player.x, player.y, player.angle,
                player.health, myKills, respawnTimer
            );
            if (player.isJustFired() &&
                !player.getBullets().isEmpty()) {
                Player.Bullet last = player.getBullets()
                    .get(player.getBullets().size() - 1);
                packet.firedBullet = true;
                packet.bulletX     = last.x;
                packet.bulletY     = last.y;
                packet.bulletAngle = last.angle;
            }
            network.send(packet);

            // ── RECEIVE STATE ──────────────────────────
            GamePacket received = network.getLatestPacket();
            if (received != null) {
                remoteX     = received.x;
                remoteY     = received.y;
                remoteAngle = received.angle;
                remoteKills = received.kills;

                // ── HEALTH + KILL TRACKING ─────────────
                int prevHealth = remoteHealth;
                remoteHealth   = received.health;

                // Count kill only when remote health
                // transitions from alive to dead
                if (prevHealth > 0 && remoteHealth <= 0) {
                    myKills++;
                }

                // Detect remote respawn
                if (prevHealth <= 0 && remoteHealth > 0) {
                    remoteAlive = true;
                }

                // Sync alive state from health
                remoteAlive = remoteHealth > 0;

                // ── REMOTE BULLET SPAWN ────────────────
                if (received.firedBullet) {
                    remoteBullets.add(new RemoteBullet(
                        received.bulletX,
                        received.bulletY,
                        received.bulletAngle
                    ));
                }
            }

            // ── SMOOTH REMOTE POSITION ─────────────────
            float lerpSpeed = 12f;
            smoothRemoteX     = lerp(smoothRemoteX, remoteX,
                lerpSpeed * delta);
            smoothRemoteY     = lerp(smoothRemoteY, remoteY,
                lerpSpeed * delta);
            smoothRemoteAngle = lerpAngle(smoothRemoteAngle,
                remoteAngle, lerpSpeed * delta);

            // ── UPDATE REMOTE BULLETS ──────────────────
            Iterator<RemoteBullet> it = remoteBullets.iterator();
            while (it.hasNext()) {
                RemoteBullet rb = it.next();
                rb.update(delta);

                // Remove if out of bounds
                if (rb.x < 0 || rb.x > screenW ||
                    rb.y < 0 || rb.y > screenH) {
                    it.remove();
                    continue;
                }

                // Remote bullets damage local player
                if (player.alive) {
                    Rectangle myBox = new Rectangle(
                        player.x, player.y,
                        Player.DRAW_W, Player.DRAW_H);
                    if (myBox.contains(rb.x, rb.y)) {
                        it.remove();
                        player.health -= BULLET_DAMAGE;
                        if (player.health <= 0) {
                            player.health = 0;
                            player.alive  = false;
                            respawnTimer  = RESPAWN_TIME;
                        }
                    }
                }
            }

            // ── MY BULLETS HIT REMOTE ─────────────────
            // Deactivate bullet on hit
            // Kills tracked via health packet above
            if (remoteAlive) {
                Rectangle remoteBox = new Rectangle(
                    remoteX, remoteY,
                    Player.DRAW_W, Player.DRAW_H);
                for (Player.Bullet b : player.getBullets()) {
                    if (b.active && remoteBox.contains(b.x, b.y)) {
                        b.active = false;
                    }
                }
            }
        }
    }

    // ── RESPAWN ────────────────────────────────────────
    void respawnPlayer() {
        player.health = 100;
        player.alive  = true;
        respawnTimer  = RESPAWN_TIME;
        // Return to top-left spawn
        player.x = spawnX;
        player.y = spawnY;
    }

    // ── END MATCH ──────────────────────────────────────
    void endMatch() {
        matchOver = true;
        if (myKills > remoteKills) {
            winMessage = "YOU WIN!";
        } else if (remoteKills > myKills) {
            winMessage = "YOU LOSE!";
        } else {
            winMessage = player.health >= remoteHealth
                ? "YOU WIN! (HP Tiebreaker)"
                : "YOU LOSE! (HP Tiebreaker)";
        }
    }

    // ── DRAW ───────────────────────────────────────────
    public void draw(SpriteBatch batch, ShapeRenderer shape) {

        // ── TILED MAP ──────────────────────────────────
        mapRenderer.setView(viewport.getCamera().combined,
            0, 0, mapW, mapH);
        int[] layers = {0, 1, 2, 3, 4, 5, 6};
        mapRenderer.render(layers);

        // ── DEBUG: obstacle boxes ──────────────────────
        if (!obstacles.isEmpty()) {
            shape.begin(ShapeRenderer.ShapeType.Line);
            shape.setColor(1f, 0f, 0f, 1f);
            for (Rectangle r : obstacles) {
                shape.rect(r.x, r.y, r.width, r.height);
            }
            shape.end();
        }

        // ── PARTICLES ──────────────────────────────────
        player.drawParticles(shape);

        // ── REMOTE BULLETS ─────────────────────────────
        if (network != null && !remoteBullets.isEmpty()) {
            shape.begin(ShapeRenderer.ShapeType.Filled);
            shape.setColor(1f, 0.3f, 0.1f, 1f);
            for (RemoteBullet rb : remoteBullets) {
                shape.rect(rb.x - 3, rb.y - 8, 6, 16);
            }
            shape.end();
        }

        // ── HEALTH BARS ────────────────────────────────
        shape.begin(ShapeRenderer.ShapeType.Filled);
        player.drawHealthBar(shape);
        if (network != null) drawRemoteHealthBar(shape);
        shape.end();

        // ── TANKS + BULLETS ────────────────────────────
        batch.begin();
        if (player.alive) player.draw(batch);
        if (network != null && remoteAlive)
            drawRemoteTank(batch);
        batch.end();

        // ── HUD ────────────────────────────────────────
        if (network != null) drawHUD(batch, shape);
        else                  drawSoloHUD(batch);

        // ── RESPAWN COUNTDOWN ──────────────────────────
        if (!player.alive && !matchOver) drawRespawnTimer(batch);

        // ── MATCH OVER ─────────────────────────────────
        if (matchOver) drawMatchOver(batch, shape);

        // ── PAUSE SCREEN ───────────────────────────────
        if (paused) pauseScreen.draw(batch, shape, screenW, screenH);
    }

    // ── DRAW HELPERS ───────────────────────────────────
    void drawRemoteTank(SpriteBatch batch) {
        batch.draw(
            remoteTankTexture,
            smoothRemoteX, smoothRemoteY,
            Player.DRAW_W / 2f, Player.DRAW_H / 2f,
            Player.DRAW_W, Player.DRAW_H,
            1f, 1f,
            smoothRemoteAngle - 90f,
            0, 0,
            remoteTankTexture.getWidth(),
            remoteTankTexture.getHeight(),
            false, false
        );
    }

    void drawRemoteHealthBar(ShapeRenderer shape) {
        float barW    = 50f;
        float barH    = 6f;
        float barX    = smoothRemoteX + (Player.DRAW_W - barW) / 2f;
        float barY    = smoothRemoteY + Player.DRAW_H + 8f;
        float percent = remoteHealth / 100f;

        shape.setColor(0.4f, 0f, 0f, 1f);
        shape.rect(barX, barY, barW, barH);

        if      (percent > 0.5f)  shape.setColor(0.2f, 0.85f, 0.2f, 1f);
        else if (percent > 0.25f) shape.setColor(1f,   0.85f, 0f,   1f);
        else                      shape.setColor(1f,   0.2f,  0.2f, 1f);

        shape.rect(barX, barY, barW * percent, barH);
    }

    void drawHUD(SpriteBatch batch, ShapeRenderer shape) {
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0f, 0f, 0f, 0.5f);
        shape.rect(screenW / 2f - 50, screenH - 40, 100, 32);
        shape.end();

        batch.begin();
        int mins = (int)(matchTimer / 60);
        int secs = (int)(matchTimer % 60);
        font.getData().setScale(1.5f);
        font.setColor(matchTimer < 30
            ? com.badlogic.gdx.graphics.Color.RED
            : com.badlogic.gdx.graphics.Color.WHITE);
        String timeStr = String.format("%d:%02d", mins, secs);
        layout.setText(font, timeStr);
        font.draw(batch, timeStr,
            (screenW - layout.width) / 2f, screenH - 12);

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
            "ARROWS=Move   SPACE=Shoot   ESC=Pause",
            160, 20);
        batch.end();
    }

    void drawRespawnTimer(SpriteBatch batch) {
        batch.begin();
        font.getData().setScale(2f);
        font.setColor(1f, 0.8f, 0.2f, 1f);
        String msg = "Respawning in "
            + (int)(respawnTimer + 1) + "...";
        layout.setText(font, msg);
        font.draw(batch, msg,
            (screenW - layout.width) / 2f, screenH / 2f);
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
        font.setColor(won
            ? com.badlogic.gdx.graphics.Color.GREEN
            : com.badlogic.gdx.graphics.Color.RED);
        layout.setText(font, winMessage);
        font.draw(batch, winMessage,
            (screenW - layout.width) / 2f, screenH / 2f + 40);

        font.getData().setScale(1.2f);
        font.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        String score = "Kills: " + myKills
            + "  vs  " + remoteKills;
        layout.setText(font, score);
        font.draw(batch, score,
            (screenW - layout.width) / 2f, screenH / 2f - 20);

        font.getData().setScale(1f);
        font.setColor(0.6f, 0.6f, 0.6f, 1f);
        font.draw(batch, "Press ESC to return to menu",
            screenW / 2f - 120, 40);
        batch.end();
    }

    public boolean isMatchOver() { return matchOver; }

    public void dispose() {
        font.dispose();
        map.dispose();
        mapRenderer.dispose();
    }
}
