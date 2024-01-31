package com.imjustdoom.villagerinabucket.mixin;

import com.imjustdoom.villagerinabucket.item.ModItems;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Villager.class)
public abstract class VillagerMixin implements Bucketable {

    private static final EntityDataAccessor<Boolean> FROM_BUCKET;

    // TODO: clean code up bro
    @Inject(method = "mobInteract", at = @At("HEAD"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    public void mobInteract(Player player, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        LivingEntity livingEntity = (Villager) (Object) this;
        if (itemStack.getItem() == Items.BUCKET && livingEntity.isAlive()) {
            livingEntity.playSound(((Bucketable) livingEntity).getPickupSound(), 1.0F, 1.0F);
            ItemStack itemStack2 = ((Bucketable) livingEntity).getBucketItemStack();
            ((Bucketable) livingEntity).saveToBucketTag(itemStack2);
            ItemStack itemStack3 = ItemUtils.createFilledResult(itemStack, player, itemStack2, false);
            player.setItemInHand(interactionHand, itemStack3);
            Level level = livingEntity.level();
            if (!level.isClientSide) {
                CriteriaTriggers.FILLED_BUCKET.trigger((ServerPlayer) player, getBucketItemStack());
            }

            livingEntity.discard();
            cir.setReturnValue(InteractionResult.sidedSuccess(level.isClientSide));
        }
    }

    @Override
    public boolean fromBucket() {
        return ((Villager) (Object) this).getEntityData().get(FROM_BUCKET);
    }

    @Override
    public void setFromBucket(boolean bl) {
        // this makes errors, do we need it?
//        ((Villager)(Object) this).getEntityData().set(FROM_BUCKET, bl);
    }

    @Override
    public void saveToBucketTag(ItemStack itemStack) {
        Villager entity = (Villager) (Object) this;

        entity.addAdditionalSaveData(itemStack.getOrCreateTag());
        Bucketable.saveDefaultDataToBucketTag(entity, itemStack);
    }

    @Override
    public void loadFromBucketTag(CompoundTag compoundTag) {
        Villager entity = (Villager) (Object) this;

        entity.readAdditionalSaveData(compoundTag);
        Bucketable.loadDefaultDataFromBucketTag(entity, compoundTag);
    }

    @Override
    public @NotNull ItemStack getBucketItemStack() {
        return new ItemStack(ModItems.VILLAGER_IN_A_BUCKET.get());
    }

    @Override
    public @NotNull SoundEvent getPickupSound() {
        return SoundEvents.VILLAGER_TRADE;
    }

    static {
        FROM_BUCKET = SynchedEntityData.defineId(Villager.class, EntityDataSerializers.BOOLEAN);
    }
}
