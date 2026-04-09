package com.maraba.rmbtanks.Sound;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

public class SoundManager {

    public static final float MENU_VOLUME = 1.0f;
    public static final float GAME_VOLUME = 0.20f;

    private Music bgm;
    private boolean muted = false;
    private float currentVolume = MENU_VOLUME;

    // ── TANK ENGINE SOUND ──────────────────────────────
    private Music tankSound;
    private static final float TANK_MIN  = 0.30f;
    private static final float TANK_MAX  = 1.00f;
    private static final float RAMP_TIME = 3.00f;
    private float tankMoveTimer = 0f;

    // ── FIRE SOUND ─────────────────────────────────────
    private Music fireSound;
    private static final float FIRE_VOLUME = 0.7f;
    private float fireDuration = 0.6f; // fallback default

    public SoundManager() {
        bgm = Gdx.audio.newMusic(Gdx.files.internal("sounds/BGM.mp3"));
        bgm.setLooping(true);
        bgm.setVolume(MENU_VOLUME);
        bgm.play();

        tankSound = Gdx.audio.newMusic(Gdx.files.internal("sounds/TANK_SOUND.mp3"));
        tankSound.setLooping(true);
        tankSound.setVolume(0f);
        tankSound.play();

        // Load fire sound and measure duration
        fireSound = Gdx.audio.newMusic(Gdx.files.internal("sounds/TANKFIRE.mp3"));
        fireSound.setLooping(false);
        fireDuration = 4f;
    }

    // ── CALL THIS WHEN PLAYER FIRES ────────────────────
    public float playFireSound() {
        if (!muted) {
            fireSound.stop();           // reset if already playing
            fireSound.setVolume(FIRE_VOLUME);
            fireSound.play();
        }
        return fireDuration;
    }

    public void updateTankSound(boolean moving, float delta) {
        if (muted) {
            tankSound.setVolume(0f);
            tankMoveTimer = 0f;
            return;
        }

        if (moving) {
            tankMoveTimer += delta;
            if (tankMoveTimer > RAMP_TIME) tankMoveTimer = RAMP_TIME;

            float t   = tankMoveTimer / RAMP_TIME;
            float vol = TANK_MIN + (TANK_MAX - TANK_MIN) * t;
            tankSound.setVolume(vol);

            if (!tankSound.isPlaying()) tankSound.play();
        } else {
            tankMoveTimer = 0f;
            tankSound.setVolume(0f);
        }
    }

    public void setMenuVolume() {
        currentVolume = MENU_VOLUME;
        applyVolume();
        tankSound.setVolume(0f);
        tankMoveTimer = 0f;
    }

    public void setGameVolume() {
        currentVolume = GAME_VOLUME;
        applyVolume();
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
        applyVolume();
        if (muted) {
            bgm.pause();
            tankSound.setVolume(0f);
            fireSound.stop();
        } else {
            if (!bgm.isPlaying()) bgm.play();
        }
    }

    public boolean isMuted() { return muted; }

    public void pause() {
        if (!muted) bgm.pause();
        tankSound.setVolume(0f);
        tankMoveTimer = 0f;
    }

    public void resume() {
        if (!muted) bgm.play();
    }

    private void applyVolume() {
        bgm.setVolume(muted ? 0f : currentVolume);
    }

    public void dispose() {
        bgm.dispose();
        tankSound.dispose();
        fireSound.dispose();
    }
}
