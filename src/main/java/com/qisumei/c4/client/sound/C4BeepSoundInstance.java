package com.qisumei.c4.client.sound;

import com.qisumei.c4.entity.C4Entity;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public class C4BeepSoundInstance extends C4TickableSoundInstance {
    private final float pitch;

    public C4BeepSoundInstance(SoundEvent event, SoundSource source, C4Entity c4, float maxRange, float pitch) {
        super(event, source, c4, maxRange, false);
        this.pitch = pitch;
        this.volume = 1.0f;
    }

    @Override
    public float getPitch() {
        return pitch;
    }
}
