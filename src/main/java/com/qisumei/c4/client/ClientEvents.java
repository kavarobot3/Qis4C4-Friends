package com.qisumei.c4.client;

import com.qisumei.c4.qis4c4;
import com.qisumei.c4.network.PacketHandler;
import com.qisumei.c4.network.UIMessagePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = qis4c4.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientEvents {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer((EntityType) qis4c4.C4_ENTITY.get(), C4EntityRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.PLAYER_HEALTH.id(), "cs_score_hud", new CSOverlay());
        event.registerAboveAll("cs_tab_menu", new CSTabOverlay());
    }

    @Mod.EventBusSubscriber(modid = qis4c4.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeClientEvents {
        private static boolean wasClicking = false;

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                CSTabOverlay.active = KeyInputHandler.TAB_KEY.isDown();

                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) {
                    boolean isClicking = mc.options.keyUse.isDown();
                    if (wasClicking && !isClicking) {
                        PacketHandler.INSTANCE.sendToServer(new UIMessagePacket("RMB_RELEASED", 0));
                    }
                    wasClicking = isClicking;
                }
            }
        }
    }
}