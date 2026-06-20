package com.qisumei.c4.client;

import com.qisumei.c4.client.sound.C4PlantSoundInstance;
import com.qisumei.c4.network.C4PlantSoundPacket;
import com.qisumei.c4.network.PacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = "qis4c4", bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientPacketHandler {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        PacketHandler.CLIENT_PLANT_HANDLER.set((pkt, ctx) -> {
            ctx.get().enqueueWork(() -> {
                Minecraft mc = Minecraft.getInstance();
                if (mc.level != null) {
                    mc.getSoundManager().play(new C4PlantSoundInstance(pkt.getPos()));
                }
            });
            ctx.get().setPacketHandled(true);
        });
    }
}
