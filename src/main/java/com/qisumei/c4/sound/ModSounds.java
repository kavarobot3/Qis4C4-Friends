package com.qisumei.c4.sound;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, "qis4c4");

    // Радиусы затухания (в блоках):
    // - установка: 12 блоков (слышно только рядом)
    // - тиканье: 32 блока (слышно почти на всей карте, но затухает)
    // - разминирование: 8 блоков (очень локально)
    // - взрыв: 48 блоков (громкий и далеко)
    // - победа: 30 блоков
    public static final RegistryObject<SoundEvent> C4_PLANT = SOUND_EVENTS.register("c4plant",
        () -> SoundEvent.createFixedRangeEvent(new ResourceLocation("qis4c4", "c4plant"), 12.0f));

    public static final RegistryObject<SoundEvent> C4_COUNTDOWN = SOUND_EVENTS.register("c4countdown",
        () -> SoundEvent.createFixedRangeEvent(new ResourceLocation("qis4c4", "c4countdown"), 32.0f));

    public static final RegistryObject<SoundEvent> C4_DEFUSE = SOUND_EVENTS.register("c4def",
        () -> SoundEvent.createFixedRangeEvent(new ResourceLocation("qis4c4", "c4def"), 8.0f));

    public static final RegistryObject<SoundEvent> TW_SOUND = SOUND_EVENTS.register("tw",
        () -> SoundEvent.createFixedRangeEvent(new ResourceLocation("qis4c4", "tw"), 48.0f));

    public static final RegistryObject<SoundEvent> CTW_SOUND = SOUND_EVENTS.register("ctw",
        () -> SoundEvent.createFixedRangeEvent(new ResourceLocation("qis4c4", "ctw"), 30.0f));

    public static final RegistryObject<SoundEvent> C4_DISARMFINISH = SOUND_EVENTS.register("c4_disarmfinish",
        () -> SoundEvent.createFixedRangeEvent(new ResourceLocation("qis4c4", "c4_disarmfinish"), 16.0f));

    public static final RegistryObject<SoundEvent> C4_INITIATE = SOUND_EVENTS.register("c4_initiate",
        () -> SoundEvent.createFixedRangeEvent(new ResourceLocation("qis4c4", "c4_initiate"), 32.0f));

    public static final RegistryObject<SoundEvent> C4_BEEP2 = SOUND_EVENTS.register("c4_beep2",
        () -> SoundEvent.createFixedRangeEvent(new ResourceLocation("qis4c4", "c4_beep2"), 32.0f));

    public static final RegistryObject<SoundEvent> C4_BEEP2_10SEC = SOUND_EVENTS.register("c4_beep2_10sec",
        () -> SoundEvent.createFixedRangeEvent(new ResourceLocation("qis4c4", "c4_beep2_10sec"), 32.0f));

    public static final RegistryObject<SoundEvent> C4_BEEP3 = SOUND_EVENTS.register("c4_beep3",
        () -> SoundEvent.createFixedRangeEvent(new ResourceLocation("qis4c4", "c4_beep3"), 32.0f));

    public static final RegistryObject<SoundEvent> C4_BEEP3_10SEC = SOUND_EVENTS.register("c4_beep3_10sec",
        () -> SoundEvent.createFixedRangeEvent(new ResourceLocation("qis4c4", "c4_beep3_10sec"), 32.0f));

    public static final RegistryObject<SoundEvent> C4_CLICK = SOUND_EVENTS.register("c4_click",
        () -> SoundEvent.createFixedRangeEvent(new ResourceLocation("qis4c4", "c4_click"), 8.0f));

    public static final RegistryObject<SoundEvent> KEY_PRESS1 = SOUND_EVENTS.register("key_press1",
        () -> SoundEvent.createFixedRangeEvent(new ResourceLocation("qis4c4", "key_press1"), 8.0f));

    public static final RegistryObject<SoundEvent> KEY_PRESS2 = SOUND_EVENTS.register("key_press2",
        () -> SoundEvent.createFixedRangeEvent(new ResourceLocation("qis4c4", "key_press2"), 8.0f));

    public static final RegistryObject<SoundEvent> KEY_PRESS3 = SOUND_EVENTS.register("key_press3",
        () -> SoundEvent.createFixedRangeEvent(new ResourceLocation("qis4c4", "key_press3"), 8.0f));

    public static final RegistryObject<SoundEvent> KEY_PRESS4 = SOUND_EVENTS.register("key_press4",
        () -> SoundEvent.createFixedRangeEvent(new ResourceLocation("qis4c4", "key_press4"), 8.0f));

    public static final RegistryObject<SoundEvent> KEY_PRESS5 = SOUND_EVENTS.register("key_press5",
        () -> SoundEvent.createFixedRangeEvent(new ResourceLocation("qis4c4", "key_press5"), 8.0f));

    public static final RegistryObject<SoundEvent> KEY_PRESS6 = SOUND_EVENTS.register("key_press6",
        () -> SoundEvent.createFixedRangeEvent(new ResourceLocation("qis4c4", "key_press6"), 8.0f));

    public static final RegistryObject<SoundEvent> KEY_PRESS7 = SOUND_EVENTS.register("key_press7",
        () -> SoundEvent.createFixedRangeEvent(new ResourceLocation("qis4c4", "key_press7"), 8.0f));

    public static void register(IEventBus bus) {
        SOUND_EVENTS.register(bus);
    }
}