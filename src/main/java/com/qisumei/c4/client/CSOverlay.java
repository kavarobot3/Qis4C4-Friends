package com.qisumei.c4.client;

import com.qisumei.c4.network.SyncMatchStatePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import java.util.ArrayList;
import java.util.List;

public class CSOverlay implements IGuiOverlay {

    // Флаг управления отображением баннера (по умолчанию включён)
    public static boolean showBanner = true;

    private static int terScore = 0, konterScore = 0, timeLeftTicks = 0;
    private static boolean bombPlanted = false, isRoundOver = false;
    private static String roundWinner = "", winReason = "";
    private static List<SyncMatchStatePacket.PlayerState> terPlayers = new ArrayList<>();
    private static List<SyncMatchStatePacket.PlayerState> konterPlayers = new ArrayList<>();

    public static List<SyncMatchStatePacket.PlayerState> getTerPlayers() { return terPlayers; }
    public static List<SyncMatchStatePacket.PlayerState> getKonterPlayers() { return konterPlayers; }

    public static void updateState(SyncMatchStatePacket packet) {
        terScore = packet.terScore;
        konterScore = packet.konterScore;
        timeLeftTicks = packet.timeLeftTicks;
        bombPlanted = packet.bombPlanted;
        isRoundOver = packet.isRoundOver;
        roundWinner = packet.roundWinner;
        winReason = packet.winReason;
        terPlayers = packet.terPlayers;
        konterPlayers = packet.konterPlayers;
    }

    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.player == null) return;

        Font font = mc.font;
        int centerX = screenWidth / 2;
        int y = 5;

        // Верхняя панель со счётом и таймером
        guiGraphics.fill(centerX - 65, y, centerX + 65, y + 25, 0xAA000000);
        int seconds = timeLeftTicks / 20;
        String timeStr = bombPlanted ? "BOMB" : String.format("%02d:%02d", seconds / 60, seconds % 60);

        guiGraphics.drawString(font, String.valueOf(terScore), centerX - 40 - font.width(String.valueOf(terScore)) / 2, y + 8, 0xFFD700, true);
        guiGraphics.drawString(font, timeStr, centerX - font.width(timeStr) / 2, y + 8, bombPlanted ? 0xFF0000 : 0xFFFFFF, true);
        guiGraphics.drawString(font, String.valueOf(konterScore), centerX + 40 - font.width(String.valueOf(konterScore)) / 2, y + 8, 0x00BFFF, true);

        // Головы игроков
        int terStartX = centerX - 70 - (terPlayers.size() * 14);
        for (int i = 0; i < terPlayers.size(); i++) {
            renderPlayerHead(guiGraphics, mc, terPlayers.get(i), terStartX + (i * 14), y + 5);
        }
        int konterStartX = centerX + 70;
        for (int i = 0; i < konterPlayers.size(); i++) {
            renderPlayerHead(guiGraphics, mc, konterPlayers.get(i), konterStartX + (i * 14), y + 5);
        }

        // Баннер окончания раунда – отображается только если флаг showBanner == true
        if (isRoundOver && !roundWinner.isEmpty() && showBanner) {
            int bannerY = 35;
            guiGraphics.fill(centerX - 110, bannerY, centerX + 110, bannerY + 22, 0xAA000000);
            int color = roundWinner.equalsIgnoreCase("ter") ? 0xFFFFC800 : 0xFF4A90E2;
            guiGraphics.fill(centerX - 110, bannerY, centerX + 110, bannerY + 1, color);
            guiGraphics.fill(centerX - 110, bannerY + 21, centerX + 110, bannerY + 22, color);
            String winTitle = roundWinner.equalsIgnoreCase("ter") ? "ПОБЕДА ТЕРРОРИСТОВ (TER)" : "ПОБЕДА СПЕЦНАЗА (KONTER)";
            guiGraphics.drawString(font, winTitle, centerX - font.width(winTitle) / 2, bannerY + 4, color, true);
            if (!winReason.isEmpty()) {
                guiGraphics.drawString(font, winReason, centerX - font.width(winReason) / 2, bannerY + 26, 0xCCCCCC, true);
            }
        }
    }

    private void renderPlayerHead(GuiGraphics guiGraphics, Minecraft mc, SyncMatchStatePacket.PlayerState state, int x, int y) {
        ResourceLocation skin = DefaultPlayerSkin.getDefaultSkin(state.uuid);
        if (mc.getConnection() != null) {
            PlayerInfo info = mc.getConnection().getPlayerInfo(state.uuid);
            if (info != null) skin = info.getSkinLocation();
        }
        guiGraphics.blit(skin, x, y, 12, 12, 8.0F, 8.0F, 8, 8, 64, 64);
        guiGraphics.blit(skin, x, y, 12, 12, 40.0F, 8.0F, 8, 8, 64, 64);
        if (!state.isAlive) {
            guiGraphics.fill(x, y, x + 12, y + 12, 0xAA000000);
            guiGraphics.drawString(mc.font, "x", x + 3, y + 1, 0xFF0000, false);
        }
    }
}