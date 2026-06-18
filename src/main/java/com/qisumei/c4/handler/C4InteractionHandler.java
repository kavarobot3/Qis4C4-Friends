package com.qisumei.c4.handler;

import com.qisumei.c4.entity.C4Entity;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
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

    private static C4Entity getLookedAtC4(Player player) {
        AABB box = player.getBoundingBox().inflate(4.0);
        for (Entity e : player.level().getEntities(player, box, e -> e instanceof C4Entity)) return (C4Entity)e;
        return null;
    }

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        C4Entity c4 = getLookedAtC4(player);
        if (c4 != null && player.getMainHandItem().is(Items.SHEARS)) {
            event.setCanceled(true);
            c4.startDefusing(player);
            activeDefusing.put(player.getUUID(), c4.getId());
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;
        UUID pid = event.player.getUUID();
        Integer cid = activeDefusing.get(pid);
        if (cid != null) {
            C4Entity c4 = (C4Entity)event.player.level().getEntity(cid);
            if (c4 == null || c4.isRemoved() || !event.player.getMainHandItem().is(Items.SHEARS) || getLookedAtC4(event.player) != c4) {
                if (c4 != null) c4.cancelDefusing(event.player);
                activeDefusing.remove(pid);
            }
        }
    }
}