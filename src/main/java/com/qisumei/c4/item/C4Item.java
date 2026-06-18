package com.qisumei.c4.item;

import com.qisumei.c4.Config;
import com.qisumei.c4.entity.C4Entity;
import com.qisumei.c4.network.PacketHandler;
import com.qisumei.c4.sound.ModSounds;
import javax.annotation.Nonnull;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
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

public class C4Item extends Item {
    private static final ThreadLocal<Boolean> isCompleted = ThreadLocal.withInitial(() -> false);

    public C4Item() { super(new Item.Properties().stacksTo(1)); }

    @Nonnull @Override public UseAnim getUseAnimation(@Nonnull ItemStack stack) { return UseAnim.BLOCK; }

    @Nonnull @Override public InteractionResultHolder<ItemStack> use(@Nonnull Level world, @Nonnull Player player, @Nonnull InteractionHand hand) {
        player.startUsingItem(hand);
        isCompleted.set(false);
        world.playSound(null, player.blockPosition(), (SoundEvent)ModSounds.C4_PLANT.get(), SoundSource.BLOCKS, 30000.0f, 1.0f);
        if (!world.isClientSide()) PacketHandler.sendToPlayer((ServerPlayer)player, "INSTALL_START", 0);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override public void releaseUsing(@Nonnull ItemStack stack, @Nonnull Level world, @Nonnull LivingEntity user, int timeCharged) {
        if (!(user instanceof Player)) return;
        if (isCompleted.get()) { isCompleted.remove(); return; }
        if (!world.isClientSide() && world.getServer() != null) world.getServer().getCommands().performPrefixedCommand(world.getServer().createCommandSourceStack().withSuppressedOutput(), "stopsound @a * qis4c4:c4plant");
        isCompleted.remove();
    }

    @Override public void onUseTick(@Nonnull Level world, @Nonnull LivingEntity user, @Nonnull ItemStack stack, int rem) {
        if (!(user instanceof Player) || world.isClientSide()) return;
        if (rem == 1) {
            isCompleted.set(true);
            BlockPos pos = user.blockPosition();
            if (world instanceof ServerLevel) ((ServerLevel)world).addFreshEntity(new C4Entity(world, pos, true));
            if (!((Player)user).getAbilities().instabuild) stack.shrink(1);
        }
    }
    @Override public int getUseDuration(@Nonnull ItemStack stack) { return 70; }
}