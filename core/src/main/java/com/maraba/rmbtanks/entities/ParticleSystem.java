package com.maraba.rmbtanks.entities;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ParticleSystem {

    private List<Particle> particles = new ArrayList<>();
    private Random rand = new Random();

    // ── WHEEL SMOKE ────────────────────────────────────
    // Call this every frame while tank is moving
    public void emitWheelSmoke(float tankX, float tankY,
                               float tankW, float tankH,
                               float angle) {
        // Emit from back of tank
        float backX = tankX + tankW / 2f
            - (float) Math.cos(Math.toRadians(angle)) * (tankH / 2f);
        float backY = tankY + tankH / 2f
            - (float) Math.sin(Math.toRadians(angle)) * (tankH / 2f);

        // Left wheel smoke
        float leftX = backX - (float) Math.sin(Math.toRadians(angle)) * 12f;
        float leftY = backY + (float) Math.cos(Math.toRadians(angle)) * 12f;

        // Right wheel smoke
        float rightX = backX + (float) Math.sin(Math.toRadians(angle)) * 12f;
        float rightY = backY - (float) Math.cos(Math.toRadians(angle)) * 12f;

        // Emit 1-2 particles per frame from each wheel
        for (int i = 0; i < 2; i++) {
            float vx = (rand.nextFloat() - 0.5f) * 20f;
            float vy = (rand.nextFloat() - 0.5f) * 20f;
            float life = 0.4f + rand.nextFloat() * 0.3f;
            float size = 3f + rand.nextFloat() * 3f;
            // Dark grey smoke
            particles.add(new Particle(leftX,  leftY,  vx, vy,
                life, size, 0.3f, 0.3f, 0.3f));
            particles.add(new Particle(rightX, rightY, vx, vy,
                life, size, 0.3f, 0.3f, 0.3f));
        }
    }

    // ── MUZZLE SMOKE ───────────────────────────────────
    // Call this once when tank fires
    public void emitMuzzleSmoke(float barrelX, float barrelY, float angle) {
        for (int i = 0; i < 12; i++) {
            float spread = (rand.nextFloat() - 0.5f) * 60f;
            float speed  = 30f + rand.nextFloat() * 60f;
            float vx = (float) Math.cos(Math.toRadians(angle + spread)) * speed;
            float vy = (float) Math.sin(Math.toRadians(angle + spread)) * speed;
            float life = 0.3f + rand.nextFloat() * 0.4f;
            float size = 4f  + rand.nextFloat() * 5f;
            // White/light grey muzzle smoke
            float gray = 0.7f + rand.nextFloat() * 0.3f;
            particles.add(new Particle(barrelX, barrelY, vx, vy,
                life, size, gray, gray, gray));
        }
    }

    // ── UPDATE ALL ─────────────────────────────────────
    public void update(float delta) {
        for (int i = particles.size() - 1; i >= 0; i--) {
            particles.get(i).update(delta);
            if (!particles.get(i).active) {
                particles.remove(i);
            }
        }
    }

    // ── DRAW ALL ───────────────────────────────────────
    public void draw(ShapeRenderer shape) {
        shape.begin(ShapeRenderer.ShapeType.Filled);
        for (Particle p : particles) {
            p.draw(shape);
        }
        shape.end();
    }
}
