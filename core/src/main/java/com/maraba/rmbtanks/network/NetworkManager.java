package com.maraba.rmbtanks.network;

import java.io.*;
import java.net.*;

public class NetworkManager {

    public static final int PORT = 9999;

    public boolean connected = false;
    public boolean isHost    = false;
    public String  statusMsg = "Not connected";

    private ServerSocket     serverSocket;
    private Socket           socket;
    private ObjectOutputStream out;
    private ObjectInputStream  in;

    private GamePacket latestPacket = null;
    private final Object lock = new Object();

    // ── HOST ───────────────────────────────────────────
    public void startHost() {
        isHost    = true;
        statusMsg = "Waiting for player to join...";

        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(PORT);
                socket       = serverSocket.accept();
                setupStreams();
                connected = true;
                statusMsg = "Player connected!";
                startReceiving();
            } catch (Exception e) {
                statusMsg = "Host error: " + e.getMessage();
            }
        }).start();
    }

    // ── JOIN ───────────────────────────────────────────
    public void startClient(String hostIP) {
        isHost    = false;
        statusMsg = "Connecting to " + hostIP + "...";

        new Thread(() -> {
            try {
                socket    = new Socket(hostIP, PORT);
                setupStreams();
                connected = true;
                statusMsg = "Connected to host!";
                startReceiving();
            } catch (Exception e) {
                statusMsg = "Connect error: " + e.getMessage();
            }
        }).start();
    }

    // ── SETUP ──────────────────────────────────────────
    private void setupStreams() throws Exception {
        out = new ObjectOutputStream(socket.getOutputStream());
        in  = new ObjectInputStream(socket.getInputStream());
    }

    // ── SEND ───────────────────────────────────────────
    public void send(GamePacket packet) {
        if (!connected) return;
        try {
            out.writeObject(packet);
            out.flush();
            out.reset();
        } catch (Exception e) {
            connected = false;
            statusMsg = "Connection lost!";
        }
    }

    // ── RECEIVE ────────────────────────────────────────
    private void startReceiving() {
        new Thread(() -> {
            while (connected) {
                try {
                    GamePacket packet = (GamePacket) in.readObject();
                    synchronized (lock) {
                        latestPacket = packet;
                    }
                } catch (Exception e) {
                    connected = false;
                    statusMsg = "Connection lost!";
                    break;
                }
            }
        }).start();
    }

    // ── GET LATEST ─────────────────────────────────────
    public GamePacket getLatestPacket() {
        synchronized (lock) {
            GamePacket p = latestPacket;
            latestPacket = null;
            return p;
        }
    }

    // ── DISCONNECT ─────────────────────────────────────
    public void disconnect() {
        connected = false;
        try {
            if (socket       != null) socket.close();
            if (serverSocket != null) serverSocket.close();
        } catch (Exception e) {
            // ignore
        }
    }

    // ── GET LOCAL IP ───────────────────────────────────
    public String getLocalIP() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "Unknown";
        }
    }
}
