package com.maraba.rmbtanks.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import java.util.ArrayList;
import java.util.List;

public class Player {

    public float x, y;
    public float angle = 90f;

    static final float SPEED      = 150f;
    static final float TURN_SPEED = 120f;
    static final float SHOOT_DELAY = 0.4f;

    public static final int DRAW_W = 35;
    public static final int DRAW_H = 65;

    private Texture texture;
    private Texture bulletTexture;
    private float shootCooldown = 0f;
    private List<Bullet> bullets = new ArrayList<>();
    private ParticleSystem particles = new ParticleSystem();

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

        static final int BW = 6;   // bullet draw width
        static final int BH = 16;  // bullet draw height
        static final float SPEED = 400f;

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
                x - BW / 2f, y - BH / 2f,  // position
                BW / 2f, BH / 2f,            // origin (center)
                BW, BH,                       // size
                1f, 1f,                       // scale
                angle - 90f,                  // rotation
                0, 0,                         // src x, y
                tex.getWidth(),
                tex.getHeight(),
                false, false
            );
        }
    }

    // ── CONSTRUCTOR ────────────────────────────────────
    public Player(Texture texture, Texture bulletTexture,
                  float startX, float startY) {
        this.texture       = texture;
        this.bulletTexture = bulletTexture;
        this.x             = startX;
        this.y             = startY;
    }

    // ── UPDATE ─────────────────────────────────────────
    public void update(float delta, int screenW, int screenH) {

        // ── MOVEMENT ───────────────────────────────────
        if (Gdx.input.isKeyPressed(Keys.LEFT)) {
            angle += TURN_SPEED * delta;
        }
        if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
            angle -= TURN_SPEED * delta;
        }
        if (Gdx.input.isKeyPressed(Keys.UP)) {
            x += (float) Math.cos(Math.toRadians(angle)) * SPEED * delta;
            y += (float) Math.sin(Math.toRadians(angle)) * SPEED * delta;
        }
        if (Gdx.input.isKeyPressed(Keys.DOWN)) {
            x -= (float) Math.cos(Math.toRadians(angle)) * SPEED * delta;
            y -= (float) Math.sin(Math.toRadians(angle)) * SPEED * delta;
        }

        // ── KEEP INSIDE SCREEN ─────────────────────────
        x = Math.max(0, Math.min(screenW - DRAW_W, x));
        y = Math.max(0, Math.min(screenH - DRAW_H, y));

        // ── WHEEL SMOKE ────────────────────────────────
        if (Gdx.input.isKeyPressed(Keys.UP) ||
            Gdx.input.isKeyPressed(Keys.DOWN)) {
            particles.emitWheelSmoke(x, y, DRAW_W, DRAW_H, angle);
        }

        // ── SHOOTING + MUZZLE SMOKE ────────────────────
        shootCooldown -= delta;
        if (Gdx.input.isKeyJustPressed(Keys.SPACE) && shootCooldown <= 0f) {
            float barrelX = x + DRAW_W / 2f
                + (float) Math.cos(Math.toRadians(angle)) * (DRAW_H / 2f);
            float barrelY = y + DRAW_H / 2f
                + (float) Math.sin(Math.toRadians(angle)) * (DRAW_H / 2f);
            bullets.add(new Bullet(barrelX, barrelY, angle));
            particles.emitMuzzleSmoke(barrelX, barrelY, angle);
            shootCooldown = SHOOT_DELAY;
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
        // Draw tank
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

        // Draw bullets
        for (Bullet b : bullets) {
            b.draw(batch, bulletTexture);
        }

    }

    public List<Bullet> getBullets() {
        return bullets;
    }

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

        // Background (dark red)
        shape.setColor(0.4f, 0.0f, 0.0f, 1f);
        shape.rect(barX, barY, barW, barH);

        // Health fill color
        if (percent > 0.5f) {
            shape.setColor(0.2f, 0.85f, 0.2f, 1f); // green
        } else if (percent > 0.25f) {
            shape.setColor(1f, 0.85f, 0.0f, 1f);   // yellow
        } else {
            shape.setColor(1f, 0.2f, 0.2f, 1f);    // red
        }

        // Filled portion
        shape.rect(barX, barY, barW * percent, barH);

        // Border
        shape.setColor(1f, 1f, 1f, 0.6f);
        shape.rectLine(barX, barY, barX + barW, barY, 1f);
        shape.rectLine(barX, barY + barH, barX + barW, barY + barH, 1f);
        shape.rectLine(barX, barY, barX, barY + barH, 1f);
        shape.rectLine(barX + barW, barY, barX + barW, barY + barH, 1f);
    }

    public void takeDamage(int amount) {
        health -= amount;
        if (health <= 0) {
            health = 0;
            alive  = false;
        }
    }

}
