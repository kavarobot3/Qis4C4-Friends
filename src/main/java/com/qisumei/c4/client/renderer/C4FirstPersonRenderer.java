package com.qisumei.c4.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.tacz.guns.GunMod;
import com.tacz.guns.api.client.animation.statemachine.LuaAnimationStateMachine;
import com.tacz.guns.client.model.BedrockAnimatedModel;
import com.tacz.guns.client.model.SlotModel;
import com.tacz.guns.client.renderer.item.AnimateGeoItemRenderer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import com.qisumei.c4.client.resource.C4AnimationStateContext;
import com.qisumei.c4.client.resource.C4AssetsManager;
import com.qisumei.c4.client.resource.C4DisplayInstance;
import com.qisumei.c4.qis4c4;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.world.item.ItemDisplayContext.GUI;

public class C4FirstPersonRenderer extends AnimateGeoItemRenderer<BedrockAnimatedModel, C4AnimationStateContext> {
    private static final SlotModel SLOT_MODEL = new SlotModel();
    private static final ResourceLocation DISPLAY_ID = new ResourceLocation(qis4c4.MODID, "c4");
    private static final long PUT_AWAY_TIME_MS = 670L;

    private boolean initialized = false;

    public void resetInit() {
        this.initialized = false;
    }

    @Override
    public C4AnimationStateContext initContext(ItemStack stack, Player player, float partialTick) {
        C4AnimationStateContext context = new C4AnimationStateContext();
        this.updateContext(context, stack, player, partialTick);
        return context;
    }

    @Override
    public void updateContext(C4AnimationStateContext context, ItemStack stack, Player player, float partialTick) {
        context.setCurrentItem(stack);
        context.setUsing(player.isUsingItem());
        context.setUsingTick(player.getTicksUsingItem());
        context.setPartialTicks(partialTick);
    }

    public void ensureInit(ItemStack stack, LocalPlayer player, float partialTick) {
        var sm = getStateMachine(stack);
        if (sm == null) return;
        if (!initialized) {
            tryInit(stack, player, partialTick);
            initialized = true;
        }
    }

    @Override
    public void renderFirstPerson(LocalPlayer player, ItemStack stack, ItemDisplayContext ctx, PoseStack poseStack,
                                  MultiBufferSource bufferSource, int light, float partialTick) {
        ensureInit(stack, player, partialTick);
        super.renderFirstPerson(player, stack, ctx, poseStack, bufferSource, light, partialTick);
    }

    @Override
    public void doExtraTransforms(PoseStack poseStack, BedrockAnimatedModel model, ItemStack stack) {
        // Base class: translate(0,1.5,0) + rotate(ZP,180°).
        // After idle animation (root rotation 15°X), C4 center ≈ (0, 2.7, 1.47) model space.
        // px = -vx - dx = 0 - (-0.1) = 0.1
        // py = -vy - dy + 1.5 = -2.7 - 0 + 1.5 = -1.2
        // pz = vz + dz = 1.47 + (-2.8) = -1.33
        poseStack.translate(0.0F, 0.5F, -1.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(ItemStack stack) {
        var display = C4AssetsManager.INSTANCE.getDisplay(DISPLAY_ID);
        return display != null ? display.getTexture() : null;
    }

    @Override
    @Nullable
    public LuaAnimationStateMachine<C4AnimationStateContext> getStateMachine(ItemStack stack) {
        var display = C4AssetsManager.INSTANCE.getDisplay(DISPLAY_ID);
        if (display == null) {
            GunMod.LOGGER.error("C4Renderer: display is null for {}", DISPLAY_ID);
            return null;
        }
        var sm = display.getStateMachine();
        if (sm == null) {
            GunMod.LOGGER.error("C4Renderer: state machine is null for {}", DISPLAY_ID);
        }
        return sm;
    }

    @Override
    public BedrockAnimatedModel getModel(ItemStack stack) {
        var display = C4AssetsManager.INSTANCE.getDisplay(DISPLAY_ID);
        if (display == null) {
            GunMod.LOGGER.error("C4Renderer: display is null for {}", DISPLAY_ID);
            return null;
        }
        var model = display.getModel();
        if (model == null) {
            GunMod.LOGGER.error("C4Renderer: model is null for {}", DISPLAY_ID);
        }
        return model;
    }

    @Override
    public long getPutAwayTime(ItemStack stack) {
        return PUT_AWAY_TIME_MS;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext ctx, PoseStack poseStack,
                             MultiBufferSource bufferSource, int light, int overlay) {
        if (ctx.firstPerson()) return;

        var display = C4AssetsManager.INSTANCE.getDisplay(DISPLAY_ID);
        if (display == null) {
            poseStack.translate(0.5, 1.5, 0.5);
            poseStack.mulPose(Axis.ZN.rotationDegrees(180));
            VertexConsumer buf = bufferSource.getBuffer(RenderType.entityTranslucent(MissingTextureAtlasSprite.getLocation()));
            SLOT_MODEL.renderToBuffer(poseStack, buf, light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
            return;
        }

        BedrockAnimatedModel model = display.getModel();
        if (ctx == GUI && display.getTexture() != null) {
            poseStack.translate(0.5, 1.5, 0.5);
            poseStack.mulPose(Axis.ZN.rotationDegrees(180));
            VertexConsumer buf = bufferSource.getBuffer(RenderType.entityTranslucent(display.getTexture()));
            SLOT_MODEL.renderToBuffer(poseStack, buf, light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
            return;
        }

        poseStack.pushPose();
        ItemTransforms transforms = display.getTransforms();
        if (transforms != null) {
            poseStack.translate(0.5F, 0.5F, 0.5F);
            ItemTransform transform = transforms.getTransform(ctx);
            transform.apply(false, poseStack);
            poseStack.translate(-0.5F, -0.5F, -0.5F);
        }

        poseStack.translate(0.5, 1.5f, 0.5);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180f));
        if (model != null) {
            model.render(poseStack, ctx, RenderType.entityCutout(getTextureLocation(stack)), light, overlay);
        }
        poseStack.popPose();
    }
}
