package com.qisumei.c4.entity;

import com.qisumei.c4.Config;
import com.qisumei.c4.ModInitializer;
import com.qisumei.c4.network.PacketHandler;
import com.qisumei.c4.network.UIMessagePacket;
import com.qisumei.c4.qis4c4;
import com.qisumei.c4.sound.ModSounds;
import com.qisumei.c4.api.CSBombAPI;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class C4Entity extends Entity {
    private static final EntityDataAccessor<Integer> TICKS_LEFT = SynchedEntityData.defineId(C4Entity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_PLAYER_PLACED = SynchedEntityData.defineId(C4Entity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> DEFUSING_PLAYER_UUID = SynchedEntityData.defineId(C4Entity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Long> DEFUSE_END_TIME = SynchedEntityData.defineId(C4Entity.class, EntityDataSerializers.LONG);
    public C4Entity(EntityType<? extends C4Entity> type, Level level) {
        super(type, level);
        this.noCulling = true;
    }

    public C4Entity(Level level, BlockPos pos, boolean playerPlaced) {
        this((EntityType<? extends C4Entity>) qis4c4.C4_ENTITY.get(), level);
        this.setPos(pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5);
        this.entityData.set(TICKS_LEFT, 800);
        this.entityData.set(IS_PLAYER_PLACED, playerPlaced);
        this.entityData.set(DEFUSING_PLAYER_UUID, "");
        this.entityData.set(DEFUSE_END_TIME, 0L);
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            ModInitializer.updateC4State(server, 1);
            ModInitializer.updateC4Position(server, pos.getX(), pos.getY(), pos.getZ());
        }
        if (!level.isClientSide()) CSBombAPI.onBombPlanted();
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(TICKS_LEFT, 800);
        this.entityData.define(IS_PLAYER_PLACED, true);
        this.entityData.define(DEFUSING_PLAYER_UUID, "");
        this.entityData.define(DEFUSE_END_TIME, 0L);
    }

    public int getTicksLeft() {
        return this.entityData.get(TICKS_LEFT);
    }

    public String getDefusingPlayerUUID() {
        return this.entityData.get(DEFUSING_PLAYER_UUID);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("TicksLeft")) this.entityData.set(TICKS_LEFT, tag.getInt("TicksLeft"));
        if (tag.contains("IsPlayerPlaced")) this.entityData.set(IS_PLAYER_PLACED, tag.getBoolean("IsPlayerPlaced"));
        if (tag.contains("DefusingPlayerUUID")) this.entityData.set(DEFUSING_PLAYER_UUID, tag.getString("DefusingPlayerUUID"));
        if (tag.contains("DefuseEndTime")) this.entityData.set(DEFUSE_END_TIME, tag.getLong("DefuseEndTime"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("TicksLeft", this.entityData.get(TICKS_LEFT));
        tag.putBoolean("IsPlayerPlaced", this.entityData.get(IS_PLAYER_PLACED));
        tag.putString("DefusingPlayerUUID", this.entityData.get(DEFUSING_PLAYER_UUID));
        tag.putLong("DefuseEndTime", this.entityData.get(DEFUSE_END_TIME));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;

        long defuseEndTick = this.entityData.get(DEFUSE_END_TIME);
        if (defuseEndTick > 0L) {
            if (this.level().getGameTime() >= defuseEndTick) {
                defuse(null);
                return;
            }
        }

        int ticksLeft = this.entityData.get(TICKS_LEFT);
        if (ticksLeft <= 0) {
            explode();
            return;
        }
        this.entityData.set(TICKS_LEFT, --ticksLeft);
    }

    public void cancelDefusing(Player player) {
        if (this.entityData.get(DEFUSING_PLAYER_UUID).isEmpty()) return;
        this.entityData.set(DEFUSING_PLAYER_UUID, "");
        this.entityData.set(DEFUSE_END_TIME, 0L);
    }

    @Nonnull
    @Override
    public AABB getBoundingBoxForCulling() {
        return new AABB(this.getX() - 0.35, this.getY(), this.getZ() - 0.35,
                this.getX() + 0.35, this.getY() + 0.2, this.getZ() + 0.35);
    }

    @Override
    public boolean isPickable() { return true; }
    @Override
    public float getPickRadius() { return 0.8f; }
    @Override
    public boolean hurt(DamageSource source, float amount) { return false; }
    @Override
    public boolean isInvulnerable() { return true; }

    public void startDefusing(Player player) {
        if (!player.getItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND).is(Items.SHEARS)) return;
        if (!this.entityData.get(DEFUSING_PLAYER_UUID).isEmpty()) return;

        this.entityData.set(DEFUSING_PLAYER_UUID, player.getUUID().toString());
        this.entityData.set(DEFUSE_END_TIME, player.level().getGameTime() + 100L);
        if (player instanceof ServerPlayer) {
            PacketHandler.sendToPlayer((ServerPlayer) player, "DEFUSE_START", 0);
        }
    }

    private void defuse(Player player) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            ModInitializer.updateC4State(server, 3);
            ModInitializer.clearC4Position(server);
        }
        if (!this.level().isClientSide) {
            PacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(),
                    new UIMessagePacket("§a Бомба обезврежена!", 2000));
        }
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                ModSounds.CTW_SOUND.get(), SoundSource.BLOCKS, 1.2f, 1.0f);
        if (!this.level().isClientSide) CSBombAPI.onBombDefused();
        this.discard();
    }

    private void explode() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            ModInitializer.updateC4State(server, 2);
            ModInitializer.clearC4Position(server);
        }
        if (Config.playExplosionSound) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    ModSounds.TW_SOUND.get(), SoundSource.BLOCKS, 3.0f, 0.8f);
        }
        this.level().explode(null, this.getX(), this.getY(), this.getZ(), 4.0f, Level.ExplosionInteraction.NONE);
        if (!this.level().isClientSide) CSBombAPI.onBombExploded();
        this.discard();
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}