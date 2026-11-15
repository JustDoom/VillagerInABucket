package com.imjustdoom.villagerinabucket.mixin;

import com.imjustdoom.villagerinabucket.VillagerBucketable;
import com.imjustdoom.villagerinabucket.config.Config;
import com.imjustdoom.villagerinabucket.item.ModItems;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.village.ReputationEventType;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.antlr.v4.runtime.misc.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tallestegg.guardvillagers.entities.Guard;

@Mixin(Guard.class)
public abstract class GuardMixin extends PathfinderMob implements VillagerBucketable {
    @Shadow public abstract void onReputationEventFrom(ReputationEventType type, Entity target);

    @Shadow(remap = false) public abstract void readAdditionalSaveData(CompoundTag arg);
    @Shadow(remap = false) public abstract void addAdditionalSaveData(CompoundTag arg);

    @Unique private boolean villagerinabucket$fromBucket;

    public GuardMixin(EntityType<? extends AbstractVillager> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
    public void mobInteract(Player player, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack itemStack = player.getItemInHand(interactionHand);

        // Check if the state is valid for picking up the mob in a bucket
        if (itemStack.getItem() != Items.BUCKET || !isAlive()) {
            return;
        }

        if (level().isClientSide()) {
            cir.setReturnValue(InteractionResult.SUCCESS);
            return;
        }

        playSound(getPickupSound(), 1.0F, 1.0F);
        if (Config.HARM_REPUTATION) onReputationEventFrom(ReputationEventType.VILLAGER_HURT, player);
        player.setItemInHand(interactionHand, ItemUtils.createFilledResult(itemStack, player, createBucketStack(), false));
        CriteriaTriggers.FILLED_BUCKET.trigger((ServerPlayer) player, getBucketItemStack()); // Used for achievements tracking

        discard();
        cir.setReturnValue(InteractionResult.sidedSuccess(level().isClientSide()));
    }

    @Override
    public ItemStack createBucketStack() {
        ItemStack villagerBucket = getBucketItemStack();
        saveToBucketTag(villagerBucket);

        villagerBucket.getOrCreateTag().putString("id", EntityType.getKey(getType()).toString());

        return villagerBucket;
    }

    @Inject(at = @At("TAIL"), method = "addAdditionalSaveData")
    public void addAdditionalSaveData(CompoundTag compound, CallbackInfo ci) {
        compound.putBoolean("FromBucket", fromBucket());
    }

    @Inject(at = @At("TAIL"), method = "readAdditionalSaveData")
    public void readAdditionalSaveData(CompoundTag compound, CallbackInfo ci) {
        this.setFromBucket(compound.getBoolean("FromBucket"));
    }

    @Override
    public boolean fromBucket() {
        return this.villagerinabucket$fromBucket;
    }

    @Override
    public void setFromBucket(boolean fromBucket) {
        this.villagerinabucket$fromBucket = fromBucket;
    }

    @Override
    public void saveToBucketTag(ItemStack itemStack) {
        Bucketable.saveDefaultDataToBucketTag(this, itemStack);
        addAdditionalSaveData(itemStack.getOrCreateTag());
    }

    @Override
    public void loadFromBucketTag(CompoundTag compoundTag) {
        Bucketable.loadDefaultDataFromBucketTag(this, compoundTag);
        readAdditionalSaveData(compoundTag);
    }

    @Override
    public @NotNull ItemStack getBucketItemStack() {
        return new ItemStack(ModItems.VILLAGER_IN_A_BUCKET);
    }

    @Override
    public @NotNull SoundEvent getPickupSound() {
        return SoundEvents.VILLAGER_TRADE;
    }
}
