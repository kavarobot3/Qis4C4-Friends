package com.qisumei.c4.client.sound;

import com.qisumei.c4.entity.C4Entity;
import com.qisumei.c4.sound.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = "qis4c4")
public class C4SoundEngine {
    private static final Map<UUID, C4SoundManager> managers = new HashMap<>();

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            managers.values().forEach(C4SoundManager::stopAll);
            managers.clear();
            return;
        }

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof C4Entity c4 && c4.isAlive()) {
                managers.computeIfAbsent(c4.getUUID(), k -> new C4SoundManager()).tick(mc, c4);
            }
        }

        managers.keySet().removeIf(uuid -> {
            C4SoundManager mgr = managers.get(uuid);
            if (mgr != null && !mgr.isEntityAlive()) {
                mgr.stopAll();
                return true;
            }
            return false;
        });
    }

    private static class C4SoundManager {
        private C4Entity c4Entity;
        private C4DefuseSoundInstance defuseSound;
        private int nextBeepTick = -1;
        private boolean togglePitch = false;

        public void tick(Minecraft mc, C4Entity c4) {
            this.c4Entity = c4;
            int ticksLeft = c4.getTicksLeft();

            // Defuse sound management
            String defuser = c4.getDefusingPlayerUUID();
            if (!defuser.isEmpty()) {
                if (defuseSound == null) {
                    defuseSound = new C4DefuseSoundInstance(c4);
                    mc.getSoundManager().play(defuseSound);
                }
            } else {
                if (defuseSound != null) {
                    defuseSound.stopInstance();
                    defuseSound = null;
                }
            }

            // Countdown beep scheduling
            if (ticksLeft > 0) {
                if (nextBeepTick == -1 || nextBeepTick > ticksLeft) {
                    nextBeepTick = ticksLeft;
                }

                if (ticksLeft == nextBeepTick) {
                    int interval;
                    float pitch;
                    var beepEvent = ModSounds.C4_BEEP2.get();

                    if (ticksLeft > 200) {
                        float progress = (800f - ticksLeft) / 600f;
                        interval = Math.max(8, (int) (20f - (progress * 12f)));
                        pitch = 1.0f;
                        beepEvent = ModSounds.C4_BEEP2.get();
                    } else if (ticksLeft > 100) {
                        float progress = (200f - ticksLeft) / 100f;
                        interval = Math.max(5, (int) (8f - (progress * 3f)));
                        pitch = togglePitch ? 0.95f : 1.2f;
                        togglePitch = !togglePitch;
                        beepEvent = ModSounds.C4_BEEP2.get();
                    } else {
                        float progress = (100f - ticksLeft) / 100f;
                        interval = Math.max(2, (int) (5f - (progress * 3f)));
                        pitch = togglePitch ? 1.25f : 1.5f;
                        togglePitch = !togglePitch;
                        beepEvent = ModSounds.C4_BEEP3.get();
                    }

                    float range = 256.0f;
                    mc.getSoundManager().play(new C4BeepSoundInstance(
                            beepEvent, SoundSource.BLOCKS, c4, range, pitch
                    ));

                    nextBeepTick = ticksLeft - interval;
                }
            }
        }

        public boolean isEntityAlive() {
            return c4Entity != null && c4Entity.isAlive();
        }

        public void stopAll() {
            if (defuseSound != null) {
                defuseSound.stopInstance();
                defuseSound = null;
            }
        }
    }
}
