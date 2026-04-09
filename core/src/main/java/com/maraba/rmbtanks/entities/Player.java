package com.maraba.rmbtanks.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.maraba.rmbtanks.Sound.SoundManager;
import java.util.ArrayList;
import java.util.List;

public class Player {

    public float x, y;
    public float angle = 90f;

    static final float SPEED       = 150f;
    static final float TURN_SPEED  = 120f;
    static final float SHOOT_DELAY = 0.4f;

    public static final int DRAW_W = 55;
    public static final int DRAW_H = 95;

    private Texture texture;
    private Texture bulletTexture;
    private float shootCooldown = 0f;
    private List<Bullet> bullets = new ArrayList<>();
    private ParticleSystem particles = new ParticleSystem();
    private SoundManager sound;

    // ── OBSTACLE RECTANGLES ────────────────────────────
    private List<Rectangle> obstacles;

    // ── JUST FIRED FLAG ────────────────────────────────
    private boolean justFired = false;

    // ── HEALTH ─────────────────────────────────────────
    public int health    = 100;
    public int maxHealth = 100;
    public boolean alive = true;

    // ── BULLET INNER CLASS ─────────────────────────────
    public static class Bullet {
        public float x, y;
        public float vx, vy;
        public float angle;
        public boolean active = true;

        static final int   BW    = 6;
        static final int   BH    = 16;
        static final float SPEED = 1000f;

        public Bullet(float x, float y, float angle) {
            this.x     = x;
            this.y     = y;
            this.angle = angle;
            this.vx    = (float) Math.cos(Math.toRadians(angle)) * SPEED;
            this.vy    = (float) Math.sin(Math.toRadians(angle)) * SPEED;
        }

        public void update(float delta, int screenW, int screenH) {
            x += vx * delta;
            y += vy * delta;
            if (x < 0 || x > screenW || y < 0 || y > screenH) {
                active = false;
            }
        }

        public void draw(SpriteBatch batch, Texture tex) {
            batch.draw(
                tex,
                x - BW / 2f, y - BH / 2f,
                BW / 2f, BH / 2f,
                BW, BH,
                1f, 1f,
                angle - 90f,
                0, 0,
                tex.getWidth(),
                tex.getHeight(),
                false, false
            );
        }
    }

    // ── CONSTRUCTOR ────────────────────────────────────
    public Player(Texture texture, Texture bulletTexture,
                  float startX, float startY, SoundManager sound) {
        this.texture       = texture;
        this.bulletTexture = bulletTexture;
        this.x             = startX;
        this.y             = startY;
        this.sound         = sound;
    }

    // ── SET OBSTACLES ──────────────────────────────────
    public void setObstacles(List<Rectangle> obstacles) {
        this.obstacles = obstacles;
    }

    // ── JUST FIRED — read once then auto-reset ─────────
    public boolean isJustFired() {
        boolean f = justFired;
        justFired = false;
        return f;
    }

    // ── COLLISION CHECK ────────────────────────────────
    private boolean collidesWithObstacle(float nx, float ny) {
        if (obstacles == null) return false;
        Rectangle tankBox = new Rectangle(nx, ny, DRAW_W, DRAW_H);
        for (Rectangle obs : obstacles) {
            if (tankBox.overlaps(obs)) return true;
        }
        return false;
    }

    // ── UPDATE ─────────────────────────────────────────
    public void update(float delta, int screenW, int screenH) {

        // ── ROTATION ───────────────────────────────────
        if (Gdx.input.isKeyPressed(Keys.LEFT)) {
            angle += TURN_SPEED * delta;
        }
        if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
            angle -= TURN_SPEED * delta;
        }

        // ── MOVEMENT WITH COLLISION ────────────────────
        float dx = 0, dy = 0;

        if (Gdx.input.isKeyPressed(Keys.UP)) {
            dx += (float) Math.cos(Math.toRadians(angle)) * SPEED * delta;
            dy += (float) Math.sin(Math.toRadians(angle)) * SPEED * delta;
        }
        if (Gdx.input.isKeyPressed(Keys.DOWN)) {
            dx -= (float) Math.cos(Math.toRadians(angle)) * SPEED * delta;
            dy -= (float) Math.sin(Math.toRadians(angle)) * SPEED * delta;
        }

        // Try X movement independently
        if (!collidesWithObstacle(x + dx, y)) {
            x += dx;
        }

        // Try Y movement independently
        if (!collidesWithObstacle(x, y + dy)) {
            y += dy;
        }

        // ── KEEP INSIDE SCREEN ─────────────────────────
        x = Math.max(0, Math.min(screenW - DRAW_W, x));
        y = Math.max(0, Math.min(screenH - DRAW_H, y));

        // ── WHEEL SMOKE ────────────────────────────────
        boolean movingForward = Gdx.input.isKeyPressed(Keys.UP) ||
            Gdx.input.isKeyPressed(Keys.DOWN);
        if (movingForward) {
            particles.emitWheelSmoke(x, y, DRAW_W, DRAW_H, angle);
        }

        // ── TANK ENGINE SOUND ──────────────────────────
        if (sound != null) {
            boolean anyMovement = movingForward ||
                Gdx.input.isKeyPressed(Keys.LEFT) ||
                Gdx.input.isKeyPressed(Keys.RIGHT);
            sound.updateTankSound(anyMovement, delta);
        }

        // ── SHOOT COOLDOWN ─────────────────────────────
        shootCooldown -= delta;

        // ── SHOOTING + MUZZLE SMOKE ────────────────────
        if (Gdx.input.isKeyJustPressed(Keys.SPACE)
            && shootCooldown <= 0f) {
            float barrelX = x + DRAW_W / 2f
                + (float) Math.cos(Math.toRadians(angle)) * (DRAW_H / 2f);
            float barrelY = y + DRAW_H / 2f
                + (float) Math.sin(Math.toRadians(angle)) * (DRAW_H / 2f);

            bullets.add(new Bullet(barrelX, barrelY, angle));
            particles.emitMuzzleSmoke(barrelX, barrelY, angle);

            // ── SET JUST FIRED FLAG ─────────────────────
            justFired = true;

            if (sound != null) {
                float duration = sound.playFireSound();
                shootCooldown = duration > 0 ? duration : SHOOT_DELAY;
            } else {
                shootCooldown = 4f;
            }
        }

        // ── UPDATE BULLETS ─────────────────────────────
        for (int i = bullets.size() - 1; i >= 0; i--) {
            bullets.get(i).update(delta, screenW, screenH);
            if (!bullets.get(i).active) {
                bullets.remove(i);
            }
        }

        // ── UPDATE PARTICLES ───────────────────────────
        particles.update(delta);
    }

    // ── DRAW ───────────────────────────────────────────
    public void draw(SpriteBatch batch) {
        batch.draw(
            texture,
            x, y,
            DRAW_W / 2f, DRAW_H / 2f,
            DRAW_W, DRAW_H,
            1f, 1f,
            angle - 90f,
            0, 0,
            texture.getWidth(),
            texture.getHeight(),
            false, false
        );

        for (Bullet b : bullets) {
            b.draw(batch, bulletTexture);
        }
    }

    public List<Bullet> getBullets() { return bullets; }

    public void drawParticles(ShapeRenderer shape) {
        particles.draw(shape);
    }

    public void drawHealthBar(ShapeRenderer shape) {
        if (!alive) return;

        float barW    = 50f;
        float barH    = 6f;
        float barX    = x + (DRAW_W - barW) / 2f;
        float barY    = y + DRAW_H + 8f;
        float percent = (float) health / maxHealth;

        shape.setColor(0.4f, 0.0f, 0.0f, 1f);
        shape.rect(barX, barY, barW, barH);

        if      (percent > 0.5f)  shape.setColor(0.2f, 0.85f, 0.2f, 1f);
        else if (percent > 0.25f) shape.setColor(1f,   0.85f, 0.0f, 1f);
        else                      shape.setColor(1f,   0.2f,  0.2f, 1f);

        shape.rect(barX, barY, barW * percent, barH);

        shape.setColor(1f, 1f, 1f, 0.6f);
        shape.rectLine(barX,        barY,
            barX + barW, barY,        1f);
        shape.rectLine(barX,        barY + barH,
            barX + barW, barY + barH, 1f);
        shape.rectLine(barX,        barY,
            barX,        barY + barH, 1f);
        shape.rectLine(barX + barW, barY,
            barX + barW, barY + barH, 1f);
    }

    public void takeDamage(int amount) {
        health -= amount;
        if (health <= 0) {
            health = 0;
            alive  = false;
        }
    }
}
