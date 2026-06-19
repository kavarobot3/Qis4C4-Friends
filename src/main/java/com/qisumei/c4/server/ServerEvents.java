package com.qisumei.c4.server;

import com.mojang.brigadier.CommandDispatcher;
import com.qisumei.c4.network.PacketHandler;
import com.qisumei.c4.network.UIMessagePacket;
import com.qisumei.c4.qis4c4;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = qis4c4.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerEvents {

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            MatchManager.getInstance().tick(event.getServer());
        }
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("cshud").requires(s -> s.hasPermission(2))
            .then(Commands.literal("start").executes(c -> {
                MatchManager.getInstance().startNewRound();
                return 1;
            }))
            .then(Commands.literal("reset").executes(c -> {
                MatchManager.getInstance().resetMatch();
                return 1;
            }))
            .then(Commands.literal("banner")
                .then(Commands.literal("toggle").executes(c -> {
                    // Переключение баннера для всех игроков
                    PacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(),
                        new UIMessagePacket("BANNER_TOGGLE", 0));
                    return 1;
                }))
                .then(Commands.literal("on").executes(c -> {
                    PacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(),
                        new UIMessagePacket("BANNER_ON", 0));
                    return 1;
                }))
                .then(Commands.literal("off").executes(c -> {
                    PacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(),
                        new UIMessagePacket("BANNER_OFF", 0));
                    return 1;
                }))
            )
        );
    }
}