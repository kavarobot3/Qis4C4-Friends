package com.qisumei.c4.api;

import com.qisumei.c4.server.MatchManager;

public class CSBombAPI {
    public static void onBombPlanted() { MatchManager.getInstance().setBombPlanted(true); }
    public static void onBombDefused() { MatchManager.getInstance().triggerDefuse(); }
    public static void onBombExploded() { MatchManager.getInstance().triggerExplosion(); }
}