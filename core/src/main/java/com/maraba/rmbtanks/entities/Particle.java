package com.maraba.rmbtanks.entities;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Particle {

    public float x, y;
    public float vx, vy;
    public float life;        // countdown to 0
    public float maxLife;
    public float size;
    public float r, g, b;    // color
    public boolean active = true;

    public Particle(float x, float y, float vx, float vy,
                    float life, float size, float r, float g, float b) {
        this.x       = x;
        this.y       = y;
        this.vx      = vx;
        this.vy      = vy;
        this.life    = life;
        this.maxLife = life;
        this.size    = size;
        this.r       = r;
        this.g       = g;
        this.b       = b;
    }

    public void update(float delta) {
        x    += vx * delta;
        y    += vy * delta;
        life -= delta;
        // Slow down over time
        vx   *= 0.95f;
        vy   *= 0.95f;
        if (life <= 0) active = false;
    }

    public void draw(ShapeRenderer shape) {
        // Fade out as life decreases
        float alpha = life / maxLife;
        float currentSize = size * alpha;
        shape.setColor(r, g, b, alpha);
        shape.circle(x, y, currentSize);
    }
}
