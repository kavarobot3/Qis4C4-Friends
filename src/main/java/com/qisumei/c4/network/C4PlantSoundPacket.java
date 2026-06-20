package com.qisumei.c4.network;

import com.qisumei.c4.client.sound.C4PlantSoundInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class C4PlantSoundPacket {
    private final BlockPos pos;

    public C4PlantSoundPacket(BlockPos pos) {
        this.pos = pos;
    }

    public C4PlantSoundPacket(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                mc.getSoundManager().play(new C4PlantSoundInstance(pos));
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
