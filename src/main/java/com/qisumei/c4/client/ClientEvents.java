package com.qisumei.c4.client;

import com.qisumei.c4.client.renderer.C4EntityRenderer;
import com.qisumei.c4.client.renderer.C4FirstPersonRenderer;
import com.qisumei.c4.qis4c4;
import com.qisumei.c4.network.PacketHandler;
import com.qisumei.c4.network.UIMessagePacket;
import com.tacz.guns.api.client.event.BeforeRenderHandEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
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
        private static ItemStack lastC4Stack = ItemStack.EMPTY;

        @SubscribeEvent
        public static void onBeforeRenderHand(BeforeRenderHandEvent event) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null) return;
            ItemStack mainHand = player.getMainHandItem();
            boolean isC4 = mainHand.is(qis4c4.QISC4_ITEM.get());

            if (isC4) {
                var renderer = IClientItemExtensions.of(mainHand).getCustomRenderer();
                if (renderer instanceof C4FirstPersonRenderer c4Renderer) {
                    c4Renderer.ensureInit(mainHand, player, Minecraft.getInstance().getFrameTime());
                }
                lastC4Stack = mainHand.copy();
            } else if (!lastC4Stack.isEmpty()) {
                var renderer = IClientItemExtensions.of(lastC4Stack).getCustomRenderer();
                if (renderer instanceof C4FirstPersonRenderer c4Renderer) {
                    c4Renderer.tryExit(lastC4Stack, c4Renderer.getPutAwayTime(lastC4Stack));
                    c4Renderer.resetInit();
                }
                lastC4Stack = ItemStack.EMPTY;
            }
        }

        @SubscribeEvent(receiveCanceled = true)
        public static void onRenderHand(RenderHandEvent event) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null) return;
            ItemStack handItem = player.getMainHandItem();
            if (!handItem.is(qis4c4.QISC4_ITEM.get())) return;
            var renderer = IClientItemExtensions.of(handItem).getCustomRenderer();
            if (!(renderer instanceof C4FirstPersonRenderer c4Renderer)) return;
            var ctx = event.getHand() == net.minecraft.world.InteractionHand.MAIN_HAND
                ? net.minecraft.world.item.ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                : net.minecraft.world.item.ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
            c4Renderer.renderFirstPerson(player, handItem, ctx, event.getPoseStack(),
                event.getMultiBufferSource(), event.getPackedLight(), event.getPartialTick());
        }

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