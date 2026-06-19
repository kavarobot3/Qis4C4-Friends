package com.qisumei.c4.handler;

import com.qisumei.c4.entity.C4Entity;
import com.qisumei.c4.network.PacketHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="qis4c4")
public class C4InteractionHandler {
    private static final Map<UUID, Integer> activeDefusing = new HashMap<>();
    private static final Map<UUID, Boolean> clientHoldingRMB = new HashMap<>();

    private static C4Entity getLookedAtC4(Player player) {
        AABB box = player.getBoundingBox().inflate(4.0);
        for (Entity e : player.level().getEntities(player, box, e -> e instanceof C4Entity)) {
            if (e.isAlive()) return (C4Entity)e;
        }
        return null;
    }

    public static void setPlayerHoldingRMB(UUID uuid, boolean holding) {
        clientHoldingRMB.put(uuid, holding);
    }

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        
        C4Entity c4 = getLookedAtC4(player);
        if (c4 != null && player.getMainHandItem().is(Items.SHEARS)) {
            event.setCanceled(true);
            clientHoldingRMB.put(player.getUUID(), true);
            c4.startDefusing(player);
            activeDefusing.put(player.getUUID(), c4.getId());
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;
        
        Player player = event.player;
        UUID pid = player.getUUID();
        Integer cid = activeDefusing.get(pid);
        
        if (cid != null) {
            C4Entity c4 = (C4Entity)player.level().getEntity(cid);
            boolean holding = clientHoldingRMB.getOrDefault(pid, false);

            if (c4 == null || c4.isRemoved() || !holding || 
                !player.getMainHandItem().is(Items.SHEARS) || getLookedAtC4(player) != c4) {
                
                if (c4 != null) c4.cancelDefusing(player);
                activeDefusing.remove(pid);
                clientHoldingRMB.remove(pid);
                if (player instanceof ServerPlayer sp) {
                    PacketHandler.sendToPlayer(sp, "DEFUSE_STOP", 0);
                }
            }
        }
    }
}