package com.qisumei.c4.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final AtomicReference<BiConsumer<C4PlantSoundPacket, Supplier<net.minecraftforge.network.NetworkEvent.Context>>> CLIENT_PLANT_HANDLER = new AtomicReference<>();

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("qis4c4", "main"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

    public static void register() {
        int id = 0;
        INSTANCE.registerMessage(id++, UIMessagePacket.class, UIMessagePacket::encode, UIMessagePacket::decode, UIMessagePacket::handle);
        INSTANCE.registerMessage(id++, SyncMatchStatePacket.class, SyncMatchStatePacket::encode, SyncMatchStatePacket::new, SyncMatchStatePacket::handle);
        INSTANCE.registerMessage(id++, C4PlantSoundPacket.class, C4PlantSoundPacket::encode, C4PlantSoundPacket::new, (pkt, ctx) -> {
            var handler = CLIENT_PLANT_HANDLER.get();
            if (handler != null) {
                handler.accept(pkt, ctx);
            } else {
                ctx.get().setPacketHandled(true);
            }
        });
    }

    public static void sendToPlayer(ServerPlayer player, String message, int duration) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new UIMessagePacket(message, duration, true));
    }

    public static void sendToAll(String message, int duration) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), new UIMessagePacket(message, duration, false));
    }
}