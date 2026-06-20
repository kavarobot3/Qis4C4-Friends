package com.qisumei.c4.server;

import com.qisumei.c4.network.PacketHandler;
import com.qisumei.c4.network.SyncMatchStatePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraftforge.network.PacketDistributor;
import java.util.*;
import java.util.Map.Entry;

public class MatchManager {
    private static final MatchManager INSTANCE = new MatchManager();
    public static MatchManager getInstance() { return INSTANCE; }

    private int terScore = 0;
    private int konterScore = 0;
    private static final int ROUND_TIME = 115 * 20; 
    private int matchTimer = ROUND_TIME;
    private boolean bombPlanted = false;
    private int bombTimer = 0;
    private boolean roundActive = true;
    private boolean isRoundOver = false;
    private String roundWinner = "";
    private String winReason = "";

    private final Map<UUID, PlayerStats> statsMap = new HashMap<>();

    public void tick(MinecraftServer server) {
        if (!roundActive) return;
        if (isRoundOver) {
            if (server.getTickCount() % 10 == 0) syncToClients(server);
            return;
        }

        PlayerLists lists = collectPlayers(server);
        int aliveTers = 0, aliveKonters = 0;
        for (SyncMatchStatePacket.PlayerState ps : lists.terStates) if (ps.isAlive) aliveTers++;
        for (SyncMatchStatePacket.PlayerState ps : lists.konterStates) if (ps.isAlive) aliveKonters++;

        if (!bombPlanted) {
            matchTimer--;
            if (matchTimer <= 0) winRound("konter", "Время вышло!");
        } else {
            if (bombTimer > 0) bombTimer--;
        }

        if (!lists.terStates.isEmpty() && !lists.konterStates.isEmpty()) {
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
        PlayerLists lists = collectPlayers(server);
        PacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), new SyncMatchStatePacket(terScore, konterScore, matchTimer, bombPlanted, bombTimer, isRoundOver, roundWinner, winReason, lists.terStates, lists.konterStates));
    }

    private PlayerLists collectPlayers(MinecraftServer server) {
        List<SyncMatchStatePacket.PlayerState> terStates = new ArrayList<>();
        List<SyncMatchStatePacket.PlayerState> konterStates = new ArrayList<>();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            PlayerTeam team = player.getScoreboard().getPlayersTeam(player.getScoreboardName());
            if (team == null) continue;
            boolean isAlive = player.isAlive() && player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR;
            String name = player.getGameProfile().getName();
            PlayerStats ps = getStats(player.getUUID());
            if (team.getName().equalsIgnoreCase("ter")) terStates.add(new SyncMatchStatePacket.PlayerState(player.getUUID(), name, isAlive, ps.getKills(), ps.getDeaths()));
            else if (team.getName().equalsIgnoreCase("konter")) konterStates.add(new SyncMatchStatePacket.PlayerState(player.getUUID(), name, isAlive, ps.getKills(), ps.getDeaths()));
        }
        return new PlayerLists(terStates, konterStates);
    }

    private record PlayerLists(List<SyncMatchStatePacket.PlayerState> terStates, List<SyncMatchStatePacket.PlayerState> konterStates) {}

    public void setBombPlanted(boolean planted) {
        if (isRoundOver) return;
        this.bombPlanted = planted;
        if (planted) this.bombTimer = 800;
        else this.bombTimer = 0;
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
        bombTimer = 0;
        matchTimer = ROUND_TIME;
    }

    public void resetMatch() {
        terScore = 0;
        konterScore = 0;
        statsMap.clear();
        startNewRound();
    }

    public PlayerStats getStats(UUID uuid) {
        return statsMap.computeIfAbsent(uuid, k -> new PlayerStats());
    }

    public void addKill(UUID killer) {
        getStats(killer).addKill();
    }

    public void addDeath(UUID victim) {
        getStats(victim).addDeath();
    }

    public String getMVPForTeam(String team, MinecraftServer server) {
        String mvpName = null;
        int maxKills = -1;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            PlayerTeam t = player.getScoreboard().getPlayersTeam(player.getScoreboardName());
            if (t == null || !t.getName().equalsIgnoreCase(team)) continue;
            PlayerStats ps = getStats(player.getUUID());
            if (ps.getKills() > maxKills) {
                maxKills = ps.getKills();
                mvpName = player.getGameProfile().getName();
            }
        }
        return mvpName;
    }
}