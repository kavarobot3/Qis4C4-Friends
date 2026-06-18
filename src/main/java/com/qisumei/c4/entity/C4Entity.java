package com.qisumei.c4.entity;

import com.qisumei.c4.Config;
import com.qisumei.c4.ModInitializer;
import com.qisumei.c4.network.PacketHandler;
import com.qisumei.c4.network.UIMessagePacket;
import com.qisumei.c4.qis4c4;
import com.qisumei.c4.sound.ModSounds;
import com.qisumei.c4.api.CSBombAPI;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;

public class C4Entity extends Entity {
    private static final EntityDataAccessor<Integer> TICKS_LEFT = SynchedEntityData.defineId(C4Entity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_PLAYER_PLACED = SynchedEntityData.defineId(C4Entity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> DEFUSING_PLAYER_UUID = SynchedEntityData.defineId(C4Entity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Long> DEFUSE_END_TIME = SynchedEntityData.defineId(C4Entity.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Boolean> COUNTDOWN_SOUND_PLAYED = SynchedEntityData.defineId(C4Entity.class, EntityDataSerializers.BOOLEAN);

    private int nextBeepTick;
    private boolean announced20, announced4;
    private long lastProgressTime = 0L;

    public C4Entity(EntityType<? extends C4Entity> type, Level level) { super(type, level); this.noCulling = true; }

    public C4Entity(Level level, BlockPos pos, boolean playerPlaced) {
        this((EntityType<? extends C4Entity>)qis4c4.C4_ENTITY.get(), level);
        this.setPos(pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5);
        this.entityData.set(TICKS_LEFT, 800);
        this.entityData.set(IS_PLAYER_PLACED, playerPlaced);
        this.entityData.set(DEFUSING_PLAYER_UUID, "");
        this.entityData.set(DEFUSE_END_TIME, 0L);
        this.entityData.set(COUNTDOWN_SOUND_PLAYED, false);
        this.nextBeepTick = Math.max(8, (int)(40.0f * (1.0f - (0.0f))));
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            ModInitializer.updateC4State(server, 1);
            ModInitializer.updateC4Position(server, pos.getX(), pos.getY(), pos.getZ());
        }
        if (!level.isClientSide()) CSBombAPI.onBombPlanted();
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        super.remove(reason);
        MinecraftServer server = this.level().getServer();
        if (server != null) {
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withSuppressedOutput(), "stopsound @a * qis4c4:c4countdown");
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withSuppressedOutput(), "stopsound @a * qis4c4:c4def");
        }
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(TICKS_LEFT, 800); this.entityData.define(IS_PLAYER_PLACED, true);
        this.entityData.define(DEFUSING_PLAYER_UUID, ""); this.entityData.define(DEFUSE_END_TIME, 0L);
        this.entityData.define(COUNTDOWN_SOUND_PLAYED, false);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {}
    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {}

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;
        ServerLevel serverLevel = (ServerLevel)this.level();
        if (!this.entityData.get(COUNTDOWN_SOUND_PLAYED)) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), (SoundEvent)ModSounds.C4_COUNTDOWN.get(), SoundSource.BLOCKS, 30000.0f, 1.0f);
            this.entityData.set(COUNTDOWN_SOUND_PLAYED, true);
        }
        long defuseEndTime = this.entityData.get(DEFUSE_END_TIME);
        if (defuseEndTime > 0L) {
            if (System.currentTimeMillis() >= defuseEndTime) { defuse(null); return; }
        }
        int ticksLeft = this.entityData.get(TICKS_LEFT);
        if (ticksLeft <= 0) { explode(); return; }
        this.entityData.set(TICKS_LEFT, --ticksLeft);
        if (ticksLeft <= this.nextBeepTick) {
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.ALARM_SOUND(), SoundSource.BLOCKS, 100000.0f, 1.0f);
            float progress = 1.0f - (float)ticksLeft / 800.0f;
            this.nextBeepTick = ticksLeft - Math.max(8, (int)(40.0f * (1.0f - progress * 0.8f)));
        }
    }

    @Nonnull @Override public AABB getBoundingBoxForCulling() { return new AABB(this.getX() - 0.35, this.getY(), this.getZ() - 0.35, this.getX() + 0.35, this.getY() + 0.2, this.getZ() + 0.35); }
    @Override public boolean isPickable() { return true; }
    @Override public float getPickRadius() { return 0.8f; }
    @Override public boolean hurt(DamageSource source, float amount) { return false; }
    @Override public boolean isInvulnerable() { return true; }

    public void startDefusing(Player player) {
        if (!player.getItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND).is(Items.SHEARS)) return;
        if (!this.entityData.get(DEFUSING_PLAYER_UUID).isEmpty()) return;
        this.entityData.set(DEFUSING_PLAYER_UUID, player.getUUID().toString());
        this.entityData.set(DEFUSE_END_TIME, System.currentTimeMillis() + 5000L);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), (SoundEvent)ModSounds.C4_DEFUSE.get(), SoundSource.BLOCKS, 3.5f, 1.0f);
        if (player instanceof ServerPlayer) PacketHandler.sendToPlayer((ServerPlayer)player, "DEFUSE_START", 0);
    }

    public void cancelDefusing(Player player) {
        if (this.entityData.get(DEFUSING_PLAYER_UUID).isEmpty()) return;
        this.entityData.set(DEFUSING_PLAYER_UUID, ""); this.entityData.set(DEFUSE_END_TIME, 0L);
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withSuppressedOutput(), "stopsound @a * qis4c4:c4def");
        if (player instanceof ServerPlayer) PacketHandler.sendToPlayer((ServerPlayer)player, "\u00a7c Разминирование прервано! ", 2000);
    }

    private void defuse(Player player) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) { ModInitializer.updateC4State(server, 3); ModInitializer.clearC4Position(server); }
        if (!this.level().isClientSide) PacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), new UIMessagePacket("\u00a7a Бомба обезврежена!", 2000));
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), (SoundEvent)ModSounds.CTW_SOUND.get(), SoundSource.BLOCKS, 1.5f, 1.0f);
        if (!this.level().isClientSide) CSBombAPI.onBombDefused();
        this.discard();
    }

    private void explode() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) { ModInitializer.updateC4State(server, 2); ModInitializer.clearC4Position(server); }
        if (Config.playExplosionSound) this.level().playSound(null, this.getX(), this.getY(), this.getZ(), (SoundEvent)ModSounds.TW_SOUND.get(), SoundSource.BLOCKS, 5.0f, 0.8f);
        this.level().explode(null, this.getX(), this.getY(), this.getZ(), 4.0f, Level.ExplosionInteraction.NONE);
        if (!this.level().isClientSide) CSBombAPI.onBombExploded();
        this.discard();
    }

    @Nullable @Override public Packet<ClientGamePacketListener> getAddEntityPacket() { return new ClientboundAddEntityPacket(this); }
}