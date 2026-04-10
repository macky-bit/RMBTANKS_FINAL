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

    // ── HIT COUNT ──────────────────────────────────────
    // How many times this packet sender hit the opponent
    // Receiver uses this to reduce its own health
    public int hitCount = 0;

    public GamePacket() {}

    public GamePacket(float x, float y, float angle,
                      int health, int kills, float respawnTimer) {
        this.x            = x;
        this.y            = y;
        this.angle        = angle;
        this.health       = health;
        this.kills        = kills;
        this.respawnTimer = respawnTimer;
        this.firedBullet  = false;
        this.hitCount     = 0;
    }
}
