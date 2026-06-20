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

    public static boolean showBanner = true;

    private static int terScore = 0, konterScore = 0, timeLeftTicks = 0, bombTimer = 0;
    private static boolean bombPlanted = false, isRoundOver = false;
    private static String roundWinner = "", winReason = "";
    private static List<SyncMatchStatePacket.PlayerState> terPlayers = new ArrayList<>();
    private static List<SyncMatchStatePacket.PlayerState> konterPlayers = new ArrayList<>();

    private static long bannerStartTime = 0;
    private static final long SLIDE_IN_MS = 400;
    private static final long VISIBLE_MS = 7000;
    private static final long FADE_MS = 1000;

    public static List<SyncMatchStatePacket.PlayerState> getTerPlayers() { return terPlayers; }
    public static List<SyncMatchStatePacket.PlayerState> getKonterPlayers() { return konterPlayers; }

    public static void updateState(SyncMatchStatePacket packet) {
        terScore = packet.terScore;
        konterScore = packet.konterScore;
        timeLeftTicks = packet.timeLeftTicks;
        bombPlanted = packet.bombPlanted;
        bombTimer = packet.bombTimer;
        isRoundOver = packet.isRoundOver;
        roundWinner = packet.roundWinner;
        winReason = packet.winReason;
        terPlayers = packet.terPlayers;
        konterPlayers = packet.konterPlayers;
        if (packet.isRoundOver && !packet.roundWinner.isEmpty()) {
            bannerStartTime = System.currentTimeMillis();
        }
    }

    @Override
    public void render(ForgeGui gui, GuiGraphics g, float partialTick, int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.player == null) return;

        Font font = mc.font;
        int centerX = screenWidth / 2;

        renderTopBar(g, font, mc, centerX, screenWidth, screenHeight);

        if (isRoundOver && !roundWinner.isEmpty() && showBanner) {
            renderEndBanner(g, font, mc, centerX, screenWidth, screenHeight);
        }
    }

    private void renderTopBar(GuiGraphics g, Font font, Minecraft mc, int centerX, int screenWidth, int screenHeight) {
        int y = 5;
        int boxH = bombPlanted ? 40 : 25;
        g.fill(centerX - 65, y, centerX + 65, y + boxH, 0xAA000000);

        int seconds = timeLeftTicks / 20;
        String timeStr = bombPlanted ? "BOMB" : String.format("%02d:%02d", seconds / 60, seconds % 60);

        g.drawString(font, String.valueOf(terScore), centerX - 40 - font.width(String.valueOf(terScore)) / 2, y + 8, 0xFFD700, true);
        g.drawString(font, timeStr, centerX - font.width(timeStr) / 2, y + 8, bombPlanted ? 0xFF0000 : 0xFFFFFF, true);
        g.drawString(font, String.valueOf(konterScore), centerX + 40 - font.width(String.valueOf(konterScore)) / 2, y + 8, 0x00BFFF, true);

        if (bombPlanted) {
            int bombSec = bombTimer / 20;
            String bombTimeStr = String.format("%02d:%02d", bombSec / 60, bombSec % 60);
            g.drawString(font, bombTimeStr, centerX - font.width(bombTimeStr) / 2, y + 22, 0xFF4444, true);
        }

        int headY = y + 5;
        int terStartX = centerX - 70 - (terPlayers.size() * 14);
        for (int i = 0; i < terPlayers.size(); i++) {
            renderPlayerHead(g, mc, terPlayers.get(i), terStartX + (i * 14), headY);
        }
        int konterStartX = centerX + 70;
        for (int i = 0; i < konterPlayers.size(); i++) {
            renderPlayerHead(g, mc, konterPlayers.get(i), konterStartX + (i * 14), headY);
        }
    }

    private void renderEndBanner(GuiGraphics g, Font font, Minecraft mc, int centerX, int screenWidth, int screenHeight) {
        long elapsed = System.currentTimeMillis() - bannerStartTime;
        if (elapsed > VISIBLE_MS) return;

        float fade = 1.0f;
        if (elapsed > VISIBLE_MS - FADE_MS) {
            fade = 1.0f - (elapsed - (VISIBLE_MS - FADE_MS)) / (float) FADE_MS;
        }

        int alpha = (int) (220 * fade);
        if (alpha < 0) alpha = 0;
        if (alpha > 255) alpha = 255;

        boolean isTer = roundWinner.equalsIgnoreCase("ter");
        int teamColor = isTer ? 0xFFFFC800 : 0xFF4A90E2;
        int glowColor = (alpha << 24) | (teamColor & 0xFFFFFF);

        int bannerW = 340;
        int bannerH = 95;
        int bannerX = centerX - bannerW / 2;

        float slideProgress = Math.min(1.0f, elapsed / (float) SLIDE_IN_MS);
        int startY = 25;
        int slideOffset = (int) ((1.0f - slideProgress) * -70);
        int bannerY = startY + slideOffset;

        g.fill(bannerX, bannerY, bannerX + bannerW, bannerY + bannerH, (alpha << 24) | 0x1A000000);

        for (int i = 0; i < 4; i++) {
            float barA = alpha * (1.0f - i * 0.15f);
            int col = ((int) barA << 24) | (teamColor & 0xFFFFFF);
            g.fill(bannerX, bannerY + i, bannerX + bannerW, bannerY + i + 1, col);
        }
        for (int i = 0; i < 4; i++) {
            float barA = alpha * (1.0f - (3 - i) * 0.15f);
            int col = ((int) barA << 24) | (teamColor & 0xFFFFFF);
            g.fill(bannerX, bannerY + bannerH - 4 + i, bannerX + bannerW, bannerY + bannerH - 3 + i, col);
        }

        renderEmblem(g, bannerX + 14, bannerY + (bannerH - 44) / 2, 44, isTer, teamColor, alpha);

        int textX = bannerX + 66;
        int textY = bannerY + 14;

        String title = isTer ? "\u0422\u0415\u0420\u0420\u041e\u0420\u0418\u0421\u0422\u042b \u041f\u041e\u0411\u0415\u0414\u0418\u041b\u0418" : "\u041a\u041e\u041d\u0422\u0420-\u0422\u0415\u0420\u0420\u041e\u0420\u0418\u0421\u0422\u042b \u041f\u041e\u0411\u0415\u0414\u0418\u041b\u0418";
        g.drawString(font, title, textX + 1, textY + 1, 0x44000000, false);
        g.drawString(font, title, textX, textY, glowColor, true);

        String reason = getReasonText();
        g.drawString(font, reason, textX, textY + font.lineHeight + 4, (alpha << 24) | 0xCCCCCC, false);

        SyncMatchStatePacket.PlayerState mvp = findMVP(isTer ? terPlayers : konterPlayers);
        if (mvp != null) {
            int mvpY = bannerY + bannerH - 26;

            g.drawString(font, "\u2605", textX, mvpY, 0xFFFFD700, true);

            renderPlayerHead(g, mc, mvp, textX + 12, mvpY);

            String nameStr = mvp.name + " \u2014 " + mvp.kills + "K " + mvp.deaths + "D";
            g.drawString(font, nameStr, textX + 26, mvpY + 2, (alpha << 24) | 0xFFFFFF, true);
        }

        String score = terScore + " : " + konterScore;
        int scoreX = centerX - font.width(score) / 2;
        int scoreY = bannerY + bannerH + 8;
        g.drawString(font, score, scoreX, scoreY, (alpha << 24) | 0xFFFFFF, true);
    }

    private void renderEmblem(GuiGraphics g, int x, int y, int size, boolean isTer, int teamColor, int alpha) {
        int bgColor = ((int)(alpha * 0.3f) << 24) | (teamColor & 0xFFFFFF);
        g.fill(x, y, x + size, y + size, bgColor);

        String letter = isTer ? "\u0422" : "CT";
        Font font = Minecraft.getInstance().font;
        int lx = x + (size - font.width(letter)) / 2;
        int ly = y + (size - font.lineHeight) / 2;
        g.drawString(font, letter, lx + 1, ly + 1, 0x66000000, false);
        g.drawString(font, letter, lx, ly, (alpha << 24) | 0xFFFFFF, true);
    }

    private String getReasonText() {
        String icon;
        String reason = winReason.toLowerCase();

        if (reason.contains("\u0432\u0437\u043e\u0440\u0432") || reason.contains("explod")) {
            icon = "\u2622 ";
        } else if (reason.contains("\u043e\u0431\u0435\u0437\u0432\u0440") || reason.contains("defus")) {
            icon = "\u2691 ";
        } else if (reason.contains("\u0432\u0440\u0435\u043c") || reason.contains("time") || reason.contains("ran out")) {
            icon = "\u23F0 ";
        } else if (reason.contains("\u0443\u0441\u0442\u0440\u0430\u043d") || reason.contains("elim") || reason.contains("kill")) {
            icon = "\u2620 ";
        } else {
            icon = "\u25B8 ";
        }

        return icon + winReason;
    }

    private SyncMatchStatePacket.PlayerState findMVP(List<SyncMatchStatePacket.PlayerState> list) {
        SyncMatchStatePacket.PlayerState best = null;
        for (SyncMatchStatePacket.PlayerState p : list) {
            if (best == null || p.kills > best.kills) best = p;
        }
        return (best != null && best.kills > 0) ? best : null;
    }

    private void renderPlayerHead(GuiGraphics g, Minecraft mc, SyncMatchStatePacket.PlayerState state, int x, int y) {
        ResourceLocation skin = DefaultPlayerSkin.getDefaultSkin(state.uuid);
        if (mc.getConnection() != null) {
            PlayerInfo info = mc.getConnection().getPlayerInfo(state.uuid);
            if (info != null) skin = info.getSkinLocation();
        }
        g.blit(skin, x, y, 12, 12, 8.0F, 8.0F, 8, 8, 64, 64);
        g.blit(skin, x, y, 12, 12, 40.0F, 8.0F, 8, 8, 64, 64);
        if (!state.isAlive) {
            g.fill(x, y, x + 12, y + 12, 0xAA000000);
            g.drawString(mc.font, "\u2718", x + 3, y + 1, 0xFF0000, false);
        }
    }
}
