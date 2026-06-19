package com.qisumei.c4.client.resource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.tacz.guns.GunMod;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.qisumei.c4.qis4c4;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT, modid = qis4c4.MODID)
public enum C4AssetsManager {
    INSTANCE;

    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
            .registerTypeAdapter(ItemTransforms.class, new ItemTransforms.Deserializer())
            .registerTypeAdapter(ItemTransform.class, new ItemTransform.Deserializer())
            .create();

    private final Map<ResourceLocation, C4DisplayInstance> displayCache = new ConcurrentHashMap<>();
    private boolean loaded = false;

    @Nullable
    public C4DisplayInstance getDisplay(ResourceLocation id) {
        return displayCache.get(id);
    }

    public boolean isLoaded() {
        return loaded;
    }

    private void reload(ResourceManager manager) {
        displayCache.clear();
        loaded = false;

        var basePath = "display";
        var resources = manager.listResources(basePath, loc -> {
            var path = loc.getPath();
            return loc.getNamespace().equals(qis4c4.MODID) && path.startsWith(basePath + "/") && path.endsWith(".json");
        });

        GunMod.LOGGER.info("C4AssetsManager: found {} display files to load", resources.size());

        for (var entry : resources.entrySet()) {
            var loc = entry.getKey();
            var idStr = loc.getPath().replaceFirst("^" + basePath + "/", "").replace(".json", "");
            var id = new ResourceLocation(loc.getNamespace(), idStr);

            try (BufferedReader reader = entry.getValue().openAsReader()) {
                JsonElement element = JsonParser.parseReader(reader);
                var pojo = GSON.fromJson(element, C4DisplayInstance.C4Display.class);
                var display = C4DisplayInstance.create(pojo, id, manager);
                displayCache.put(id, display);
            } catch (Exception e) {
                GunMod.LOGGER.error("Failed to load C4 display {}", id, e);
            }
        }

        loaded = true;
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onClientResourceReload(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new PreparableReloadListener() {
            @Override
            public CompletableFuture<Void> reload(PreparationBarrier barrier, ResourceManager manager,
                                                   ProfilerFiller preProfiler, ProfilerFiller postProfiler,
                                                   Executor backgroundExecutor, Executor gameExecutor) {
                return CompletableFuture.runAsync(() -> {
                    INSTANCE.reload(manager);
                }, backgroundExecutor).thenCompose(barrier::wait);
            }
        });


    }
    
}
