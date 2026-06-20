package com.qisumei.c4.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

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

    public BlockPos getPos() {
        return pos;
    }
}
