package com.qisumei.c4.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import com.qisumei.c4.client.CSOverlay;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class SyncMatchStatePacket {
    public final int terScore, konterScore, timeLeftTicks;
    public final boolean bombPlanted, isRoundOver;
    public final String roundWinner, winReason;
    public final List<PlayerState> terPlayers, konterPlayers;

    public SyncMatchStatePacket(int tScore, int ctScore, int time, boolean planted, boolean over, String winner, String reason, List<PlayerState> tPlayers, List<PlayerState> ctPlayers) {
        this.terScore = tScore; this.konterScore = ctScore; this.timeLeftTicks = time;
        this.bombPlanted = planted; this.isRoundOver = over; this.roundWinner = winner; this.winReason = reason;
        this.terPlayers = tPlayers; this.konterPlayers = ctPlayers;
    }

    public SyncMatchStatePacket(FriendlyByteBuf buf) {
        this.terScore = buf.readInt(); this.konterScore = buf.readInt(); this.timeLeftTicks = buf.readInt();
        this.bombPlanted = buf.readBoolean(); this.isRoundOver = buf.readBoolean();
        this.roundWinner = buf.readUtf(); this.winReason = buf.readUtf();
        this.terPlayers = new ArrayList<>();
        int tCount = buf.readInt(); 
        for (int i = 0; i < tCount; i++) this.terPlayers.add(new PlayerState(buf.readUUID(), buf.readUtf(), buf.readBoolean()));
        this.konterPlayers = new ArrayList<>();
        int ctCount = buf.readInt(); 
        for (int i = 0; i < ctCount; i++) this.konterPlayers.add(new PlayerState(buf.readUUID(), buf.readUtf(), buf.readBoolean()));
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(terScore); buf.writeInt(konterScore); buf.writeInt(timeLeftTicks);
        buf.writeBoolean(bombPlanted); buf.writeBoolean(isRoundOver);
        buf.writeUtf(roundWinner); buf.writeUtf(winReason);
        buf.writeInt(terPlayers.size()); for (PlayerState p : terPlayers) { buf.writeUUID(p.uuid); buf.writeUtf(p.name); buf.writeBoolean(p.isAlive); }
        buf.writeInt(konterPlayers.size()); for (PlayerState p : konterPlayers) { buf.writeUUID(p.uuid); buf.writeUtf(p.name); buf.writeBoolean(p.isAlive); }
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> CSOverlay.updateState(this));
        ctx.get().setPacketHandled(true);
    }

    public static class PlayerState {
        public final UUID uuid; public final String name; public final boolean isAlive;
        public PlayerState(UUID uuid, String name, boolean isAlive) { 
            this.uuid = uuid; this.name = name; this.isAlive = isAlive; 
        }
    }
}