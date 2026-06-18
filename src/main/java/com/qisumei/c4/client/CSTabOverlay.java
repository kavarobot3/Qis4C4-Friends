package com.qisumei.c4.client;

import com.qisumei.c4.network.SyncMatchStatePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class CSTabOverlay implements IGuiOverlay {
    public static boolean active = false;

    @Override
    public void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
        if (!active) return;
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;

        int panelWidth = 350;
        int startX = (screenWidth - panelWidth) / 2;
        int startY = 60;

        // Фон таблицы с градиентом или плотным темным цветом
        graphics.fill(startX, startY, startX + panelWidth, startY + 220, 0xF0181818);
        
        // Разделительная линия
        graphics.fill(startX, startY + 110, startX + panelWidth, startY + 112, 0xFF333333);

        // --- Отрисовка команд ---
        drawTeam(graphics, font, startX, startY + 5, "COUNTER-TERRORISTS", 0x4A90E2, CSOverlay.getKonterPlayers());
        drawTeam(graphics, font, startX, startY + 115, "TERRORISTS", 0xFFC800, CSOverlay.getTerPlayers());
    }

    private void drawTeam(GuiGraphics graphics, Font font, int x, int y, String name, int color, java.util.List<SyncMatchStatePacket.PlayerState> players) {
        graphics.drawString(font, name, x + 10, y, color, true);
        int currentY = y + 20;
        for (SyncMatchStatePacket.PlayerState p : players) {
            int textColor = p.isAlive ? 0xFFFFFF : 0x666666;
            String status = p.isAlive ? "●" : "×";
            int statusColor = p.isAlive ? 0x00FF00 : 0xFF0000;
            
            graphics.drawString(font, status, x + 10, currentY, statusColor, false);
            graphics.drawString(font, p.name, x + 25, currentY, textColor, false);
            currentY += 15;
        }
    }
}