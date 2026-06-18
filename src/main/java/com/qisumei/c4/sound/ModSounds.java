package com.qisumei.c4.sound;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, "qis4c4");
    public static final RegistryObject<SoundEvent> C4_PLANT = SOUND_EVENTS.register("c4plant", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("qis4c4", "c4plant")));
    public static final RegistryObject<SoundEvent> C4_COUNTDOWN = SOUND_EVENTS.register("c4countdown", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("qis4c4", "c4countdown")));
    public static final RegistryObject<SoundEvent> CTW_SOUND = SOUND_EVENTS.register("ctw", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("qis4c4", "ctw")));
    public static final RegistryObject<SoundEvent> TW_SOUND = SOUND_EVENTS.register("tw", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("qis4c4", "tw")));
    public static final RegistryObject<SoundEvent> C4_DEFUSE = SOUND_EVENTS.register("c4def", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("qis4c4", "c4def")));

    public static SoundEvent ALARM_SOUND() { return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("minecraft", "block.note_block.hat")); }
    public static void register(IEventBus bus) { SOUND_EVENTS.register(bus); }
}