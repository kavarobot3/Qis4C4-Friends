package com.qisumei.c4.client;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public class KeyInputHandler {
    public static final KeyMapping TAB_KEY = new KeyMapping("key.c4.tab", GLFW.GLFW_KEY_TAB, "key.categories.c4");

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(TAB_KEY);
    }
}