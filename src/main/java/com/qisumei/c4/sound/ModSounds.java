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

    // Звук ноты для ускоренного биения (используется как заменитель, но мы заменим его на C4_COUNTDOWN)
    public static SoundEvent ALARM_SOUND() {
        return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("minecraft", "block.note_block.hat"));
    }

    public static void register(IEventBus bus) {
        SOUND_EVENTS.register(bus);
    }
}