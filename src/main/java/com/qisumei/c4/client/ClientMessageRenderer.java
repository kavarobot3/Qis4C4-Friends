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

    public static void startInstallCountdown(int totalMillis) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        long now = System.currentTimeMillis();
        currentMessages.put(mc.player.getUUID(), new MessageData("", now + totalMillis + 1000L, now, totalMillis, "install"));
        updateCache(mc);
    }

    public static void startDefuseCountdown(int totalSeconds) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        long now = System.currentTimeMillis();
        currentMessages.put(mc.player.getUUID(), new MessageData("", now + totalSeconds * 1000L, now, totalSeconds * 1000, "defuse"));
        updateCache(mc);
    }

    public static boolean isDefusing() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return false;
        MessageData msg = currentMessages.get(mc.player.getUUID());
        return msg != null && "defuse".equals(msg.type) && !msg.isExpired();
    }

    private static void updateCache(Minecraft mc) {
        if (mc.player == null) return;
        MessageData msg = currentMessages.get(mc.player.getUUID());
        if (msg == null || msg.isExpired()) { cachedText = ""; return; }
        String displayText = "defuse".equals(msg.type) ? "" : (msg.isCountdown ? msg.getCountdownText() : msg.text);
        if (displayText == null) { cachedText = ""; return; }
        if (displayText.isEmpty()) { cachedText = ""; return; }
        cachedX = (mc.getWindow().getGuiScaledWidth() - mc.font.width(displayText)) / 2;
        cachedY = mc.getWindow().getGuiScaledHeight() / 2 - 30;
        cachedText = displayText;
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;
        currentMessages.entrySet().removeIf(e -> e.getValue().isExpired());
        MessageData msg = currentMessages.get(mc.player.getUUID());
        if (msg == null) { cachedText = ""; return; }

        if ("defuse".equals(msg.type) && !msg.isExpired()) {
            int screenW = mc.getWindow().getGuiScaledWidth();
            int screenH = mc.getWindow().getGuiScaledHeight();
            GuiGraphics g = event.getGuiGraphics();
            Font font = mc.font;

            long remaining = Math.max(0L, msg.totalDuration - (System.currentTimeMillis() - msg.startTime));
            float progress = (remaining <= 0) ? 1.0f : 1.0f - (float) remaining / msg.totalDuration;
            float secFloat = remaining / 1000.0f;
            String timeStr = String.format("%.1f", secFloat);
            String label = "Разминирование...";

            int bannerW = 240;
            int bannerH = 34;
            int bannerX = (screenW - bannerW) / 2;
            int bannerY = screenH / 2 + 10;

            g.fill(bannerX, bannerY, bannerX + bannerW, bannerY + bannerH, 0xCC000000);
            g.fill(bannerX, bannerY, bannerX + bannerW, bannerY + 1, 0xFFFFAA00);

            int circleCx = bannerX + 22;
            int circleCy = bannerY + bannerH / 2;
            int circleRadius = 11;
            int circleThickness = 3;
            drawProgressRing(g, circleCx, circleCy, circleRadius, circleThickness, progress, 0xFF4488FF, 0xFF666666);

            int labelX = bannerX + 40;
            int labelY = bannerY + (bannerH - font.lineHeight) / 2;
            g.drawString(font, label, labelX, labelY, 0xFFFFFF, true);

            int timeX = bannerX + bannerW - font.width(timeStr) - 10;
            int timeY = bannerY + (bannerH - font.lineHeight) / 2;
            g.drawString(font, timeStr, timeX, timeY, 0xFFAAAAAA, true);
        } else {
            updateCache(mc);
            if (!cachedText.isEmpty()) {
                event.getGuiGraphics().drawString(mc.font, cachedText, cachedX, cachedY, 0xFFFFFF, false);
            }
        }
    }

    private static void drawProgressRing(GuiGraphics g, int cx, int cy, int outerR, int thickness, float progress, int fillColor, int emptyColor) {
        int innerR = outerR - thickness;

        g.fill(cx - 1, cy - 1, cx + 2, cy + 2, 0xFF000000);

        for (int dy = -outerR; dy <= outerR; dy++) {
            for (int dx = -outerR; dx <= outerR; dx++) {
                double dist = Math.sqrt(dx * dx + dy * dy);
                if (dist < innerR || dist > outerR) continue;
                double angle = Math.toDegrees(Math.atan2(dx, -dy));
                if (angle < 0) angle += 360;
                boolean filled = (angle / 360.0) <= progress;
                g.fill(cx + dx, cy + dy, cx + dx + 1, cy + dy + 1, filled ? fillColor : emptyColor);
            }
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
            if ("install".equals(type)) {
                float secFloat = remaining / 1000.0f;
                return "§e Установка... " + String.format("%.1f", secFloat) + " сек. ";
            }
            return text;
        }
    }
}
