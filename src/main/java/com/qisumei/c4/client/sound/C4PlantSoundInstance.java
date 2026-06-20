package com.qisumei.c4.client.sound;

import com.qisumei.c4.sound.ModSounds;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;

public class C4PlantSoundInstance extends AbstractSoundInstance {
    public C4PlantSoundInstance(BlockPos pos) {
        super(ModSounds.C4_PLANT.get(), SoundSource.BLOCKS, SoundInstance.createUnseededRandom());
        this.x = pos.getX() + 0.5f;
        this.y = pos.getY() + 0.5f;
        this.z = pos.getZ() + 0.5f;
        this.volume = 1.0f;
        this.pitch = 1.0f;
        this.looping = false;
        this.attenuation = SoundInstance.Attenuation.LINEAR;
    }
}
