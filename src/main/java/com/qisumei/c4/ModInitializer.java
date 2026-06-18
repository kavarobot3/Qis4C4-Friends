package com.qisumei.c4;

import javax.annotation.Nonnull;
import com.mojang.brigadier.CommandDispatcher;
import com.qisumei.c4.entity.C4Entity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.scores.Scoreboard; // ИСПРАВЛЕННЫЙ ИМПОРТ
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

@Mod.EventBusSubscriber(modid="qis4c4")
public class ModInitializer {
    private static final String SCOREBOARD_NAME = "c4_condition";
    private static final String SCOREBOARD_P1 = "p1score";
    private static final String SCOREBOARD_P2 = "p2score";
    private static final String SCOREBOARD_P3 = "p3score";
    private static final String FAKE_PLAYER = "#C4";
    public static final int STATE_IDLE = 0;
    public static final int STATE_PLACED = 1;
    public static final int STATE_EXPLODED = 2;
    public static final int STATE_DEFUSED = 3;

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        initScoreboard(event.getServer());
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("c4clear").requires(s -> s.hasPermission(2)).executes(context -> {
            int clearedCount = 0;
            MinecraftServer server = context.getSource().getServer();
            for (ServerLevel level : server.getAllLevels()) {
                for (Entity entity : level.getAllEntities()) {
                    if (entity instanceof C4Entity c4 && c4.isAlive()) {
                        c4.discard();
                        clearedCount++;
                    }
                }
            }
            updateC4State(server, STATE_IDLE);
            clearC4Position(server);
            return 1;
        }));
    }

    private static void initScoreboard(MinecraftServer server) {
        ServerScoreboard scoreboard = server.getScoreboard();
        if (scoreboard.getObjective(SCOREBOARD_NAME) == null) scoreboard.addObjective(SCOREBOARD_NAME, ObjectiveCriteria.DUMMY, Component.literal("C4"), ObjectiveCriteria.RenderType.INTEGER);
        if (scoreboard.getObjective(SCOREBOARD_P1) == null) scoreboard.addObjective(SCOREBOARD_P1, ObjectiveCriteria.DUMMY, Component.literal("X"), ObjectiveCriteria.RenderType.INTEGER);
        if (scoreboard.getObjective(SCOREBOARD_P2) == null) scoreboard.addObjective(SCOREBOARD_P2, ObjectiveCriteria.DUMMY, Component.literal("Y"), ObjectiveCriteria.RenderType.INTEGER);
        if (scoreboard.getObjective(SCOREBOARD_P3) == null) scoreboard.addObjective(SCOREBOARD_P3, ObjectiveCriteria.DUMMY, Component.literal("Z"), ObjectiveCriteria.RenderType.INTEGER);
    }

    public static void updateC4State(MinecraftServer server, int state) {
        if (server == null) server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        Objective obj = server.getScoreboard().getObjective(SCOREBOARD_NAME);
        if (obj != null) server.getScoreboard().getOrCreatePlayerScore(FAKE_PLAYER, obj).setScore(state);
    }

    public static void updateC4Position(MinecraftServer server, int x, int y, int z) {
        if (server == null) server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        Scoreboard sb = server.getScoreboard();
        if (sb.getObjective(SCOREBOARD_P1) != null) sb.getOrCreatePlayerScore(FAKE_PLAYER, sb.getObjective(SCOREBOARD_P1)).setScore(x);
        if (sb.getObjective(SCOREBOARD_P2) != null) sb.getOrCreatePlayerScore(FAKE_PLAYER, sb.getObjective(SCOREBOARD_P2)).setScore(y);
        if (sb.getObjective(SCOREBOARD_P3) != null) sb.getOrCreatePlayerScore(FAKE_PLAYER, sb.getObjective(SCOREBOARD_P3)).setScore(z);
    }

    public static void clearC4Position(MinecraftServer server) { updateC4Position(server, 0, 0, 0); }
}