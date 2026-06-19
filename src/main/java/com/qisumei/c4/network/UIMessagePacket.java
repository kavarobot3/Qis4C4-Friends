package com.qisumei.c4.network;

import com.qisumei.c4.client.CSOverlay;
import com.qisumei.c4.client.ClientMessageRenderer;
import com.qisumei.c4.handler.C4InteractionHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class UIMessagePacket {
    public final String message;
    public final int duration;
    public final boolean isSelfOnly;

    public UIMessagePacket(String message, int duration, boolean isSelfOnly) {
        this.message = message;
        this.duration = duration;
        this.isSelfOnly = isSelfOnly;
    }

    public UIMessagePacket(String message, int duration) {
        this(message, duration, false);
    }

    public static void encode(UIMessagePacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.message);
        buf.writeInt(packet.duration);
        buf.writeBoolean(packet.isSelfOnly);
    }

    public static UIMessagePacket decode(FriendlyByteBuf buf) {
        return new UIMessagePacket(buf.readUtf(), buf.readInt(), buf.readBoolean());
    }

    public static void handle(UIMessagePacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getDirection().getReceptionSide().isServer()) {
                if ("RMB_RELEASED".equals(packet.message)) {
                    C4InteractionHandler.setPlayerHoldingRMB(ctx.get().getSender().getUUID(), false);
                }
                return;
            }

            if ("BANNER_TOGGLE".equals(packet.message)) {
                CSOverlay.showBanner = !CSOverlay.showBanner;
            } else if ("BANNER_ON".equals(packet.message)) {
                CSOverlay.showBanner = true;
            } else if ("BANNER_OFF".equals(packet.message)) {
                CSOverlay.showBanner = false;
            } else if ("INSTALL_START".equals(packet.message)) {
                ClientMessageRenderer.startInstallCountdown(3000);
            } else if ("DEFUSE_START".equals(packet.message)) {
                ClientMessageRenderer.startDefuseCountdown(5);
            } else if ("INSTALL_STOP".equals(packet.message) || "DEFUSE_STOP".equals(packet.message)) {
                ClientMessageRenderer.clearCurrentMessage();
            } else {
                ClientMessageRenderer.showMessage(Component.literal(packet.message), packet.duration);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}