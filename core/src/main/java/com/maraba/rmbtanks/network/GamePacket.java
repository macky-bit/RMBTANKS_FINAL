package com.maraba.rmbtanks.network;

import java.io.Serializable;

public class GamePacket implements Serializable {

    public float x, y;
    public float angle;
    public int   health;
    public int   kills;
    public float respawnTimer;

    public boolean firedBullet;
    public float   bulletX, bulletY, bulletAngle;

    // ── HIT SYSTEM ─────────────────────────────────────
    // Cumulative hits I dealt to opponent
    public int hitCount      = 0;
    // Echo back — how many hits I have received so far
    public int confirmedHits = 0;

    public GamePacket() {}

    public GamePacket(float x, float y, float angle,
                      int health, int kills, float respawnTimer) {
        this.x             = x;
        this.y             = y;
        this.angle         = angle;
        this.health        = health;
        this.kills         = kills;
        this.respawnTimer  = respawnTimer;
        this.firedBullet   = false;
        this.hitCount      = 0;
        this.confirmedHits = 0;
    }
}
