package com.qisumei.c4.client;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = "qis4c4", bus = Mod.EventBusSubscriber.Bus.MOD)
public class KeyInputHandler {
    public static final KeyMapping TAB_KEY = new KeyMapping("key.c4.tab", GLFW.GLFW_KEY_TAB, "key.categories.c4");

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(TAB_KEY);
    }
}