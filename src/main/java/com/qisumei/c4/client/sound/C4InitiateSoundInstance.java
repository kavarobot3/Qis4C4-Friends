package com.qisumei.c4.client.sound;

import com.qisumei.c4.entity.C4Entity;
import com.qisumei.c4.sound.ModSounds;
import net.minecraft.sounds.SoundSource;

public class C4InitiateSoundInstance extends C4TickableSoundInstance {
    public C4InitiateSoundInstance(C4Entity c4) {
        super(ModSounds.C4_INITIATE.get(), SoundSource.BLOCKS, c4, 48.0f, false);
    }
}
