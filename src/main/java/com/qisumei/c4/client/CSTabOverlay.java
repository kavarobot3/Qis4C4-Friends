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

import java.util.List;

public class CSTabOverlay implements IGuiOverlay {
    public static boolean active = false;

    @Override
    public void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
        if (!active) return;
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;

        int panelWidth = 420;
        int startX = (screenWidth - panelWidth) / 2;
        int panelY = 50;
        int rowH = 16;
        int terCount = CSOverlay.getTerPlayers().size();
        int konterCount = CSOverlay.getKonterPlayers().size();
        int headerH = 16;
        int spacing = 6;
        int panelH = headerH + terCount * rowH + spacing + headerH + konterCount * rowH + 20;

        graphics.fill(startX, panelY, startX + panelWidth, panelY + panelH, 0xF0181818);

        int y = panelY + 8;
        y = drawTeam(graphics, mc, font, startX, y, panelWidth, "КОНТР-ТЕРРОРИСТЫ", 0x4A90E2, CSOverlay.getKonterPlayers());
        y += spacing;
        drawTeam(graphics, mc, font, startX, y, panelWidth, "ТЕРРОРИСТЫ", 0xFFC800, CSOverlay.getTerPlayers());
    }

    private int drawTeam(GuiGraphics graphics, Minecraft mc, Font font, int x, int y, int w, String title, int color, List<SyncMatchStatePacket.PlayerState> players) {
        graphics.drawString(font, title, x + 12, y, color, true);
        y += 14;

        int nameX = x + 28;
        int statsX = x + w - 80;
        int pingX = x + w - 30;

        for (SyncMatchStatePacket.PlayerState p : players) {
            renderHead(graphics, mc, p, x + 10, y);
            String statusIcon = p.isAlive ? "\u25CF" : "\u2716";
            int statusColor = p.isAlive ? 0x55FF55 : 0xFF5555;
            graphics.drawString(font, statusIcon, nameX - 2, y + 2, statusColor, false);
            graphics.drawString(font, p.name, nameX + 8, y + 2, p.isAlive ? 0xFFFFFF : 0x888888, false);

            String kd = p.kills + "-" + p.deaths;
            graphics.drawString(font, kd, statsX, y + 2, 0xCCCCCC, false);

            int ping = getPing(mc, p.uuid);
            String pingStr = ping + "ms";
            int pingColor = ping < 50 ? 0x55FF55 : ping < 100 ? 0xFFFF55 : 0xFF5555;
            graphics.drawString(font, pingStr, pingX, y + 2, pingColor, false);

            y += 14;
        }
        return y;
    }

    private void renderHead(GuiGraphics graphics, Minecraft mc, SyncMatchStatePacket.PlayerState state, int x, int y) {
        ResourceLocation skin = DefaultPlayerSkin.getDefaultSkin(state.uuid);
        if (mc.getConnection() != null) {
            PlayerInfo info = mc.getConnection().getPlayerInfo(state.uuid);
            if (info != null) skin = info.getSkinLocation();
        }
        graphics.blit(skin, x, y, 12, 12, 8.0F, 8.0F, 8, 8, 64, 64);
        graphics.blit(skin, x, y, 12, 12, 40.0F, 8.0F, 8, 8, 64, 64);
        if (!state.isAlive) {
            graphics.fill(x, y, x + 12, y + 12, 0xAA000000);
            graphics.drawString(mc.font, "x", x + 3, y + 1, 0xFF0000, false);
        }
    }

    private int getPing(Minecraft mc, java.util.UUID uuid) {
        if (mc.getConnection() != null) {
            PlayerInfo info = mc.getConnection().getPlayerInfo(uuid);
            if (info != null) return info.getLatency();
        }
        return 0;
    }
}
