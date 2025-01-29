package com.imjustdoom.villagerinabucket.mixin;

import com.imjustdoom.villagerinabucket.VillagerBucketable;
import com.imjustdoom.villagerinabucket.config.Config;
import com.imjustdoom.villagerinabucket.item.ModItems;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ZombieVillager.class)
public abstract class ZombieVillagerMixin extends Zombie implements Bucketable, VillagerBucketable {

    @Unique
    private static final EntityDataAccessor<Boolean> FROM_BUCKET = SynchedEntityData.defineId(ZombieVillagerMixin.class, EntityDataSerializers.BOOLEAN);

    public ZombieVillagerMixin(EntityType<? extends Zombie> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "mobInteract", at = @At("HEAD"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    public void mobInteract(Player player, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack itemStack = player.getItemInHand(interactionHand);

        if (itemStack.getItem() != Items.BUCKET || !isAlive() || !Config.ZOMBIE_VILLAGER) {
            return;
        }

        playSound(getPickupSound(), 1.0F, 1.0F);
        player.setItemInHand(interactionHand, ItemUtils.createFilledResult(itemStack, player, createBucketStack(), false));

        if (!level().isClientSide()) {
            CriteriaTriggers.FILLED_BUCKET.trigger((ServerPlayer) player, getBucketItemStack());
        }

        discard();
        cir.setReturnValue(InteractionResult.SUCCESS);
    }

    @Override
    public ItemStack createBucketStack() {
        ItemStack villagerBucket = getBucketItemStack();
        saveToBucketTag(villagerBucket);
        return villagerBucket;
    }

    @Inject(at = @At("HEAD"), method = "defineSynchedData")
    public void defineSynchedData(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(FROM_BUCKET, false);
    }

    @Inject(at = @At("HEAD"), method = "addAdditionalSaveData")
    public void addAdditionalSaveData(CompoundTag compound, CallbackInfo ci) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("FromBucket", this.fromBucket());
    }

    @Inject(at = @At("HEAD"), method = "readAdditionalSaveData")
    public void readAdditionalSaveData(CompoundTag compound, CallbackInfo ci) {
        super.readAdditionalSaveData(compound);
        this.setFromBucket(compound.getBoolean("FromBucket"));
    }

    @Override
    public boolean fromBucket() {
        return getEntityData().get(FROM_BUCKET);
    }

    @Override
    public void setFromBucket(boolean fromBucket) {
        getEntityData().set(FROM_BUCKET, fromBucket);
    }

    @Override
    public void saveToBucketTag(ItemStack itemStack) {
        CustomData.update(DataComponents.BUCKET_ENTITY_DATA, itemStack, this::addAdditionalSaveData);
        Bucketable.saveDefaultDataToBucketTag(this, itemStack);
    }

    @Override
    public void loadFromBucketTag(CompoundTag compoundTag) {
        readAdditionalSaveData(compoundTag);
        Bucketable.loadDefaultDataFromBucketTag(this, compoundTag);
    }

    @Override
    public @NotNull ItemStack getBucketItemStack() {
        return new ItemStack(ModItems.ZOMBIE_VILLAGER_IN_A_BUCKET.right);
    }

    @Override
    public @NotNull SoundEvent getPickupSound() {
        return SoundEvents.ZOMBIE_VILLAGER_AMBIENT;
    }
}