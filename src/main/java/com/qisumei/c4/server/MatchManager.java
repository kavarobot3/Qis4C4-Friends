package com.qisumei.c4.server;

import com.qisumei.c4.network.PacketHandler;
import com.qisumei.c4.network.SyncMatchStatePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraftforge.network.PacketDistributor;
import java.util.ArrayList;
import java.util.List;

public class MatchManager {
    private static final MatchManager INSTANCE = new MatchManager();
    public static MatchManager getInstance() { return INSTANCE; }

    private int terScore = 0;
    private int konterScore = 0;
    private static final int ROUND_TIME = 115 * 20; 
    private int matchTimer = ROUND_TIME;
    private boolean bombPlanted = false;
    private boolean roundActive = true;
    private boolean isRoundOver = false;
    private String roundWinner = "";
    private String winReason = "";

    public void tick(MinecraftServer server) {
        if (!roundActive) return;
        if (isRoundOver) {
            if (server.getTickCount() % 10 == 0) syncToClients(server);
            return;
        }

        List<SyncMatchStatePacket.PlayerState> terStates = new ArrayList<>();
        List<SyncMatchStatePacket.PlayerState> konterStates = new ArrayList<>();
        int aliveTers = 0;
        int aliveKonters = 0;
        int totalTers = 0;
        int totalKonters = 0;

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            PlayerTeam team = player.getScoreboard().getPlayersTeam(player.getScoreboardName());
            if (team == null) continue;
            boolean isAlive = player.isAlive() && player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR;
            String name = player.getGameProfile().getName();
            
            if (team.getName().equalsIgnoreCase("ter")) {
                totalTers++;
                terStates.add(new SyncMatchStatePacket.PlayerState(player.getUUID(), name, isAlive));
                if (isAlive) aliveTers++;
            } else if (team.getName().equalsIgnoreCase("konter")) {
                totalKonters++;
                konterStates.add(new SyncMatchStatePacket.PlayerState(player.getUUID(), name, isAlive));
                if (isAlive) aliveKonters++;
            }
        }

        if (!bombPlanted) {
            matchTimer--;
            if (matchTimer <= 0) winRound("konter", "Время вышло!");
        }

        if (totalTers > 0 && totalKonters > 0) {
            if (!bombPlanted) {
                if (aliveTers == 0) winRound("konter", "Все террористы уничтожены!");
                else if (aliveKonters == 0) winRound("ter", "Все контр-террористы уничтожены!");
            } else {
                if (aliveKonters == 0) winRound("ter", "Все контр-террористы уничтожены!");
            }
        }

        if (server.getTickCount() % 10 == 0) syncToClients(server);
    }

    private void syncToClients(MinecraftServer server) {
        List<SyncMatchStatePacket.PlayerState> terStates = new ArrayList<>();
        List<SyncMatchStatePacket.PlayerState> konterStates = new ArrayList<>();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            PlayerTeam team = player.getScoreboard().getPlayersTeam(player.getScoreboardName());
            if (team == null) continue;
            boolean isAlive = player.isAlive() && player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR;
            String name = player.getGameProfile().getName();
            if (team.getName().equalsIgnoreCase("ter")) terStates.add(new SyncMatchStatePacket.PlayerState(player.getUUID(), name, isAlive));
            else if (team.getName().equalsIgnoreCase("konter")) konterStates.add(new SyncMatchStatePacket.PlayerState(player.getUUID(), name, isAlive));
        }
        PacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), new SyncMatchStatePacket(terScore, konterScore, matchTimer, bombPlanted, isRoundOver, roundWinner, winReason, terStates, konterStates));
    }

    public void setBombPlanted(boolean planted) {
        if (isRoundOver) return;
        this.bombPlanted = planted;
    }

    public void triggerDefuse() { if (roundActive && !isRoundOver) winRound("konter", "Бомба обезврежена!"); }
    public void triggerExplosion() { if (roundActive && !isRoundOver) winRound("ter", "Бомба взорвана!"); }

    public void winRound(String winner, String reason) {
        if (isRoundOver) return;
        isRoundOver = true;
        this.roundWinner = winner;
        this.winReason = reason;
        if (winner.equalsIgnoreCase("ter")) terScore++;
        else if (winner.equalsIgnoreCase("konter")) konterScore++;
    }

    public void startNewRound() {
        isRoundOver = false;
        roundWinner = "";
        winReason = "";
        bombPlanted = false;
        matchTimer = ROUND_TIME;
    }

    public void resetMatch() {
        terScore = 0;
        konterScore = 0;
        startNewRound();
    }
}