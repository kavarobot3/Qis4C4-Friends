package com.qisumei.c4.client.sound;

import com.qisumei.c4.entity.C4Entity;
import com.qisumei.c4.sound.ModSounds;
import net.minecraft.sounds.SoundSource;

public class C4DefuseSoundInstance extends C4TickableSoundInstance {
    public C4DefuseSoundInstance(C4Entity c4) {
        super(ModSounds.C4_DEFUSE.get(), SoundSource.BLOCKS, c4, 16.0f, false);
    }
}
