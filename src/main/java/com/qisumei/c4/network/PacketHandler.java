package com.qisumei.c4.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("qis4c4", "main"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

    public static void register() {
        int id = 0;
        INSTANCE.registerMessage(id++, UIMessagePacket.class, UIMessagePacket::encode, UIMessagePacket::decode, UIMessagePacket::handle);
        INSTANCE.registerMessage(id++, SyncMatchStatePacket.class, SyncMatchStatePacket::encode, SyncMatchStatePacket::new, SyncMatchStatePacket::handle);
    }

    public static void sendToPlayer(ServerPlayer player, String message, int duration) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new UIMessagePacket(message, duration, true));
    }

    public static void sendToAll(String message, int duration) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), new UIMessagePacket(message, duration, false));
    }
}