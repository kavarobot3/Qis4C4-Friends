package com.qisumei.c4.client.resource;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import com.tacz.guns.GunMod;
import com.tacz.guns.api.vmlib.LuaAnimationConstant;
import com.tacz.guns.api.vmlib.LuaGunAnimationConstant;
import com.tacz.guns.client.resource.ClientAssetsManager;
import com.tacz.guns.api.client.animation.AnimationController;
import com.tacz.guns.api.client.animation.Animations;
import com.tacz.guns.api.client.animation.statemachine.LuaAnimationStateMachine;
import com.tacz.guns.api.client.animation.statemachine.LuaStateMachineFactory;
import com.tacz.guns.client.model.BedrockAnimatedModel;
import com.tacz.guns.client.model.functional.LeftHandRender;
import com.tacz.guns.client.model.functional.RightHandRender;
import com.tacz.guns.client.resource.pojo.animation.bedrock.BedrockAnimationFile;
import com.tacz.guns.client.resource.pojo.model.BedrockModelPOJO;
import com.tacz.guns.client.resource.pojo.model.BedrockVersion;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.lib.jse.JsePlatform;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Objects;

public class C4DisplayInstance {
    private ResourceLocation id;
    private BedrockAnimatedModel model;
    private LuaAnimationStateMachine<C4AnimationStateContext> stateMachine;
    private ResourceLocation texture;
    private ItemTransforms transforms;

    private C4DisplayInstance() {}

    public ResourceLocation getId() { return id; }
    public BedrockAnimatedModel getModel() { return model; }
    public LuaAnimationStateMachine<C4AnimationStateContext> getStateMachine() { return stateMachine; }
    public ResourceLocation getTexture() { return texture; }
    public ItemTransforms getTransforms() { return transforms; }

    public static C4DisplayInstance create(C4Display pojo, ResourceLocation id, ResourceManager manager) {
        C4DisplayInstance display = new C4DisplayInstance();
        display.id = id;

        Preconditions.checkArgument(pojo.modelLocation != null, "display missing model field");
        Preconditions.checkArgument(pojo.stateMachineLocation != null, "display missing stateMachine field");
        Preconditions.checkArgument(pojo.textureLocation != null, "display missing texture field");
        Preconditions.checkArgument(pojo.animationLocation != null, "display missing animation field");

        BedrockModelPOJO modelPOJO = loadModel(manager, pojo.modelLocation);
        Preconditions.checkArgument(modelPOJO != null, "no model found for " + pojo.modelLocation);

        if (BedrockVersion.isLegacyVersion(modelPOJO)) {
            display.model = new BedrockAnimatedModel(modelPOJO, BedrockVersion.LEGACY);
        } else {
            display.model = new BedrockAnimatedModel(modelPOJO, BedrockVersion.NEW);
        }
        display.model.setFunctionalRenderer("righthand_pos", part -> new RightHandRender(display.model));
        display.model.setFunctionalRenderer("lefthand_pos", part -> new LeftHandRender(display.model));

        BedrockAnimationFile animFile = loadAnimation(manager, pojo.animationLocation);
        Preconditions.checkArgument(animFile != null, "no animation found for " + pojo.animationLocation);
        AnimationController controller = Animations.createControllerFromBedrock(animFile, display.model);

        LuaTable script = loadScript(manager, pojo.stateMachineLocation);
        Preconditions.checkArgument(script != null, "no state machine found for " + pojo.stateMachineLocation);

        display.stateMachine = new LuaStateMachineFactory<C4AnimationStateContext>()
                .setController(controller)
                .setLuaScripts(script)
                .build();
        display.texture = new ResourceLocation(pojo.textureLocation.getNamespace(), "textures/" + pojo.textureLocation.getPath() + ".png");
        display.transforms = Objects.requireNonNullElse(pojo.transforms, ItemTransforms.NO_TRANSFORMS);

        return display;
    }

    @Nullable
    private static BedrockModelPOJO loadModel(ResourceManager manager, ResourceLocation loc) {
        var resourceLoc = new ResourceLocation(loc.getNamespace(), "geo_models/" + loc.getPath() + ".json");
        try {
            var resource = manager.getResource(resourceLoc);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.get().open()))) {
                JsonElement element = JsonParser.parseReader(reader);
                return ClientAssetsManager.GSON.fromJson(element, BedrockModelPOJO.class);
            }
        } catch (Exception e) {
            GunMod.LOGGER.error("C4AssetsManager: failed to load model {}", resourceLoc, e);
            return null;
        }
    }

    @Nullable
    private static BedrockAnimationFile loadAnimation(ResourceManager manager, ResourceLocation loc) {
        var resourceLoc = new ResourceLocation(loc.getNamespace(), "animations/" + loc.getPath() + ".json");
        try {
            var resource = manager.getResource(resourceLoc);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.get().open()))) {
                JsonElement element = JsonParser.parseReader(reader);
                return ClientAssetsManager.GSON.fromJson(element, BedrockAnimationFile.class);
            }
        } catch (Exception e) {
            GunMod.LOGGER.error("C4AssetsManager: failed to load animation {}", resourceLoc, e);
            return null;
        }
    }

    @Nullable
    private static LuaTable loadScript(ResourceManager manager, ResourceLocation loc) {
        var resourceLoc = new ResourceLocation(loc.getNamespace(), "scripts/" + loc.getPath() + ".lua");
        try {
            var resource = manager.getResource(resourceLoc);
            Globals globals = JsePlatform.standardGlobals();
            installAnimationConstants(globals);
            return globals.load(new InputStreamReader(resource.get().open()), loc.toString()).call().checktable();
        } catch (Exception e) {
            GunMod.LOGGER.error("C4AssetsManager: failed to load script {}", resourceLoc, e);
            return null;
        }
    }

    private static void installAnimationConstants(Globals globals) {
        try {
            new LuaAnimationConstant().install(globals);
        } catch (Exception e) {
            GunMod.LOGGER.error("C4AssetsManager: failed to install animation constants", e);
        }
        try {
            new LuaGunAnimationConstant().install(globals);
        } catch (Exception e) {
            GunMod.LOGGER.error("C4AssetsManager: failed to install gun animation constants", e);
        }
    }

    public record C4Display(
            @SerializedName("model") ResourceLocation modelLocation,
            @SerializedName("animation") ResourceLocation animationLocation,
            @SerializedName("state_machine") ResourceLocation stateMachineLocation,
            @SerializedName("texture") ResourceLocation textureLocation,
            @SerializedName("transforms") ItemTransforms transforms
    ) {}
}
