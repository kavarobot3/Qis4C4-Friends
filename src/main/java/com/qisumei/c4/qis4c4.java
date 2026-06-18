package com.qisumei.c4;

import com.mojang.logging.LogUtils;
import com.qisumei.c4.entity.C4Entity;
import com.qisumei.c4.entity.ModEntities;
import com.qisumei.c4.item.C4Item;
import com.qisumei.c4.network.PacketHandler;
import com.qisumei.c4.sound.ModSounds;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.IConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

@Mod(value="qis4c4")
public class qis4c4 {
    public static final String MODID = "qis4c4";
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "qis4c4");
    public static final RegistryObject<C4Item> QISC4_ITEM = ITEMS.register("c4", C4Item::new);
    public static final RegistryObject<EntityType<C4Entity>> C4_ENTITY = ModEntities.C4_ENTITY;

    public qis4c4() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, (IConfigSpec)Config.SPEC);
        PacketHandler.register();
        ITEMS.register(modBus);
        ModSounds.register(modBus);
        ModEntities.register(modBus);
        LOGGER.info("Loaded mod {}", MODID);
        modBus.addListener(this::onBuildCreativeModeTabContents);
    }

    private void onBuildCreativeModeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept((ItemLike)QISC4_ITEM.get());
        }
    }
}