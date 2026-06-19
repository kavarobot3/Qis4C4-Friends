package com.qisumei.c4.client.sound;

import com.qisumei.c4.entity.C4Entity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

public abstract class C4TickableSoundInstance extends AbstractTickableSoundInstance {
    protected final C4Entity c4;
    protected final float maxRange;

    public C4TickableSoundInstance(SoundEvent event, SoundSource source, C4Entity c4, float maxRange, boolean looping) {
        super(event, source, SoundInstance.createUnseededRandom());
        this.c4 = c4;
        this.maxRange = maxRange;
        this.x = (float) c4.getX();
        this.y = (float) c4.getY();
        this.z = (float) c4.getZ();
        this.looping = looping;
        this.delay = 0;
        this.attenuation = SoundInstance.Attenuation.LINEAR;
        this.volume = 1.0f;
    }

    @Override
    public void tick() {
        if (c4.isRemoved() || !c4.isAlive()) {
            stop();
            return;
        }
        this.x = (float) c4.getX();
        this.y = (float) c4.getY();
        this.z = (float) c4.getZ();
    }

    public void stopInstance() {
        stop();
    }
}
