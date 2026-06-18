package com.qisumei.c4;

import java.util.Arrays;
import java.util.List;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid="qis4c4", bus=Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec.BooleanValue ENABLE_EXTRA = BUILDER.comment("Включить дополнительные эффекты").define("enableExtraEffect", true);
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ALLOWED_BLOCKS = BUILDER.comment("Список ID блоков").defineList("allowed_blocks", Arrays.asList("minecraft:end_stone_bricks"), obj -> obj instanceof String);
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ALLOWED_TEAMS = BUILDER.comment("Список команд").defineList("allowed_teams", Arrays.asList(new String[0]), obj -> obj instanceof String);
    public static final ForgeConfigSpec.BooleanValue PLAY_EXPLOSION_SOUND = BUILDER.comment("Включить звук взрыва C4").define("playExplosionSound", true);
    
    public static final ForgeConfigSpec SPEC = BUILDER.build();
    public static boolean enableExtraEffect;
    public static List<? extends String> allowedBlocks;
    public static List<? extends String> allowedTeams;
    public static boolean playExplosionSound;

    @SubscribeEvent
    static void onLoad(ModConfigEvent event) {
        if (event.getConfig().getSpec() == SPEC) {
            enableExtraEffect = ENABLE_EXTRA.get();
            allowedBlocks = (List)ALLOWED_BLOCKS.get();
            allowedTeams = (List)ALLOWED_TEAMS.get();
            playExplosionSound = PLAY_EXPLOSION_SOUND.get();
            if (allowedBlocks == null || allowedBlocks.isEmpty()) allowedBlocks = Arrays.asList("minecraft:end_stone_bricks");
            if (allowedTeams == null) allowedTeams = Arrays.asList(new String[0]);
        }
    }

    public static boolean isBlockAllowed(String blockId) { return allowedBlocks != null && allowedBlocks.contains(blockId); }
    public static boolean isTeamAllowed(String teamName) {
        if (allowedTeams == null || allowedTeams.isEmpty()) return true;
        return allowedTeams.contains(teamName);
    }
}