package com.qisumei.c4.client;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value={Dist.CLIENT}, modid="qis4c4")
public class ClientMessageRenderer {
    private static final Map<UUID, MessageData> currentMessages = new HashMap<>();
    private static String cachedText = "";
    private static int cachedX = 0, cachedY = 0;

    public static void showMessage(Component message, int durationMs) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        currentMessages.put(mc.player.getUUID(), new MessageData(message.getString(), System.currentTimeMillis() + durationMs));
        updateCache(mc);
    }

    public static void clearCurrentMessage() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            currentMessages.remove(mc.player.getUUID());
            cachedText = "";
        }
    }

    public static void startInstallCountdown(int totalSeconds) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        long now = System.currentTimeMillis();
        currentMessages.put(mc.player.getUUID(), new MessageData("", now + totalSeconds * 1000L + 1000L, now, totalSeconds * 1000, "install"));
        updateCache(mc);
    }

    public static void startDefuseCountdown(int totalSeconds) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        long now = System.currentTimeMillis();
        currentMessages.put(mc.player.getUUID(), new MessageData("", now + totalSeconds * 1000L + 1000L, now, totalSeconds * 1000, "defuse"));
        updateCache(mc);
    }

    private static void updateCache(Minecraft mc) {
        if (mc.player == null) return;
        MessageData msg = currentMessages.get(mc.player.getUUID());
        if (msg == null || msg.isExpired()) { cachedText = ""; return; }
        String displayText = msg.isCountdown ? msg.getCountdownText() : msg.text;
        if (displayText == null || displayText.isEmpty()) { cachedText = ""; return; }
        cachedX = (mc.getWindow().getGuiScaledWidth() - mc.font.width(displayText)) / 2;
        cachedY = mc.getWindow().getGuiScaledHeight() / 2 - 30;
        cachedText = displayText;
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;
        currentMessages.entrySet().removeIf(e -> e.getValue().isExpired());
        if (currentMessages.get(mc.player.getUUID()) == null) { cachedText = ""; return; }
        updateCache(mc);
        if (!cachedText.isEmpty()) {
            event.getGuiGraphics().drawString(mc.font, cachedText, cachedX, cachedY, 0xFFFFFF, false);
        }
    }

    private static class MessageData {
        String text, type; long expireTime, startTime; boolean isCountdown; int totalDuration;
        MessageData(String text, long expireTime) { this.text = text; this.expireTime = expireTime; this.isCountdown = false; this.type = "message"; }
        MessageData(String text, long expireTime, long startTime, int totalDuration, String type) {
            this.text = text; this.expireTime = expireTime; this.isCountdown = true; this.startTime = startTime; this.totalDuration = totalDuration; this.type = type;
        }
        boolean isExpired() { return System.currentTimeMillis() > this.expireTime; }
        String getCountdownText() {
            if (!this.isCountdown) return this.text;
            long remaining = Math.max(0L, totalDuration - (System.currentTimeMillis() - startTime));
            if (remaining <= 0) return null;
            int sec = (int)((remaining + 999) / 1000);
            if ("install".equals(type)) return "§e Установка... " + sec + " сек. ";
            if ("defuse".equals(type)) return "§e Разминирование... " + sec + " сек. ";
            return text;
        }
    }
}