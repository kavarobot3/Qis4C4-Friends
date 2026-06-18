package com.qisumei.c4.network;

import com.qisumei.c4.client.ClientMessageRenderer;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;

public class UIMessagePacket {
    public final String message; public final int duration; public final boolean isSelfOnly;

    public UIMessagePacket(String message, int duration, boolean isSelfOnly) {
        this.message = message; this.duration = duration; this.isSelfOnly = isSelfOnly;
    }
    public UIMessagePacket(String message, int duration) { this(message, duration, false); }

    public static void encode(UIMessagePacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.message); buf.writeInt(packet.duration); buf.writeBoolean(packet.isSelfOnly);
    }
    public static UIMessagePacket decode(FriendlyByteBuf buf) {
        return new UIMessagePacket(buf.readUtf(), buf.readInt(), buf.readBoolean());
    }

    public static void handle(UIMessagePacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if ("INSTALL_START".equals(packet.message)) ClientMessageRenderer.startInstallCountdown(4);
            else if ("DEFUSE_START".equals(packet.message)) ClientMessageRenderer.startDefuseCountdown(5);
            else ClientMessageRenderer.showMessage(Component.literal(packet.message), packet.duration);
        });
        ctx.get().setPacketHandled(true);
    }
}