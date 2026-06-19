package com.qisumei.c4.server;

public class PlayerStats {
    private int kills = 0;
    private int deaths = 0;

    public int getKills() { return kills; }
    public int getDeaths() { return deaths; }

    public void addKill() { kills++; }
    public void addDeath() { deaths++; }
    public void reset() { kills = 0; deaths = 0; }
}
