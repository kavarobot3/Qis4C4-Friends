package com.qisumei.c4.item;

import com.qisumei.c4.Config;
import com.qisumei.c4.client.renderer.C4FirstPersonRenderer;
import com.qisumei.c4.entity.C4Entity;
import com.qisumei.c4.network.C4PlantSoundPacket;
import com.qisumei.c4.network.PacketHandler;
import javax.annotation.Nonnull;
import net.minecraft.core.BlockPos;
import net.minecraftforge.network.PacketDistributor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class C4Item extends Item {
    private static final Map<UUID, Boolean> installComplete = new ConcurrentHashMap<>();

    public C4Item() {
        super(new Item.Properties().stacksTo(1));
    }

    @Nonnull
    @Override
    public UseAnim getUseAnimation(@Nonnull ItemStack stack) {
        return UseAnim.BLOCK;
    }

    @Nonnull
    @Override
    public InteractionResultHolder<ItemStack> use(@Nonnull Level world, @Nonnull Player player, @Nonnull InteractionHand hand) {
        Block standingBlock = world.getBlockState(player.blockPosition().below()).getBlock();
        String blockId = ForgeRegistries.BLOCKS.getKey(standingBlock).toString();
        
        if (!Config.isBlockAllowed(blockId)) {
            if (!world.isClientSide()) {
                PacketHandler.sendToPlayer((ServerPlayer) player, "§c C4 можно установить только на: " + Config.allowedBlocks, 3000);
            }
            return InteractionResultHolder.fail(player.getItemInHand(hand));
        }

        player.startUsingItem(hand);
        installComplete.put(player.getUUID(), false);

        if (!world.isClientSide()) {
            PacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), new C4PlantSoundPacket(player.blockPosition()));
            PacketHandler.sendToPlayer((ServerPlayer) player, "INSTALL_START", 0);
        }
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public void releaseUsing(@Nonnull ItemStack stack, @Nonnull Level world, @Nonnull LivingEntity user, int timeCharged) {
        if (!(user instanceof Player)) return;
        if (!world.isClientSide() && !installComplete.getOrDefault(user.getUUID(), false)) {
            PacketHandler.sendToPlayer((ServerPlayer) user, "INSTALL_STOP", 0);
        }
        installComplete.remove(user.getUUID());
    }

    @Override
    public void onUseTick(@Nonnull Level world, @Nonnull LivingEntity user, @Nonnull ItemStack stack, int rem) {
        if (!(user instanceof Player) || world.isClientSide()) return;
        if (rem == 1) {
            installComplete.put(user.getUUID(), true);
            BlockPos placePos = user.blockPosition();
            if (world instanceof ServerLevel) {
                ((ServerLevel) world).addFreshEntity(new C4Entity(world, placePos, true));
            }
            if (!((Player) user).getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
    }

    @Override
    public int getUseDuration(@Nonnull ItemStack stack) {
        return 60;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private final C4FirstPersonRenderer renderer = new C4FirstPersonRenderer();

            @Override
            public C4FirstPersonRenderer getCustomRenderer() {
                return renderer;
            }
        });
    }
}