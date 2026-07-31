package com.imjustdoom.villagerinabucket.mixin;

import com.imjustdoom.villagerinabucket.VillagerBucketable;
import com.imjustdoom.villagerinabucket.config.Config;
import com.imjustdoom.villagerinabucket.item.ModItems;
import com.imjustdoom.villagerinabucket.item.custom.VillagerBucket;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.GlobalPos;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.village.ReputationEventType;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.Optional;

@Mixin(Villager.class)
public abstract class VillagerMixin extends AbstractVillager implements Bucketable, VillagerBucketable {
    @Shadow
    public abstract void releasePoi(MemoryModuleType<GlobalPos> moduleType);

    @Shadow
    public abstract VillagerData getVillagerData();

    @Shadow
    public abstract void setVillagerData(VillagerData villagerData);

    @Shadow
    public abstract void onReputationEventFrom(ReputationEventType type, Entity target);

    @Unique
    private static final EntityDataAccessor<Boolean> FROM_BUCKET = SynchedEntityData.defineId(VillagerMixin.class, EntityDataSerializers.BOOLEAN);

    public VillagerMixin(EntityType<? extends AbstractVillager> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "mobInteract", at = @At("HEAD"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    public void mobInteract(Player player, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        if (level().isClientSide() || itemStack.getItem() != Items.BUCKET || !isAlive()) {
            return;
        }

        playSound(getPickupSound(), 1.0F, 1.0F);
        if (Config.HARM_REPUTATION) {
            onReputationEventFrom(ReputationEventType.VILLAGER_HURT, player);
        }
        player.setItemInHand(interactionHand, ItemUtils.createFilledResult(itemStack, player, createBucketStack(), false));
        CriteriaTriggers.FILLED_BUCKET.trigger((ServerPlayer) player, getBucketItemStack());
        discard();
        cir.setReturnValue(InteractionResult.sidedSuccess(level().isClientSide()));
    }

    @Override
    public ItemStack createBucketStack() {
        // Free the brain on pickup since it is likely it won't need to reuse it.
        // If it does, it just yoinks the braincells back
        for (MemoryModuleType<GlobalPos> poi : List.of(MemoryModuleType.HOME, MemoryModuleType.JOB_SITE, MemoryModuleType.POTENTIAL_JOB_SITE, MemoryModuleType.MEETING_POINT)) {
            releasePoi(poi);
            getBrain().eraseMemory(poi);
        }

        // Check if the villager doesn't have the profession stamped into its existence. Because if it doesn't when
        // it is placed back down it will reset and start looking for a new job block. Which can cause confusion
        // since the bucket description will say it has a profession. So just reset on pickup
        VillagerData villagerData = getVillagerData();
        if (villagerData.getProfession() != VillagerProfession.NONE
                && villagerData.getProfession() != VillagerProfession.NITWIT
                && getVillagerXp() == 0 && villagerData.getLevel() <= 1) {
            setVillagerData(villagerData.setProfession(VillagerProfession.NONE));
        }

        ItemStack villagerBucket = getBucketItemStack();
        saveToBucketTag(villagerBucket);

        CustomData customData = villagerBucket.getOrDefault(DataComponents.BUCKET_ENTITY_DATA, CustomData.EMPTY);
        if (customData.isEmpty()) {
            return villagerBucket;
        }

        Optional<VillagerData> optional = customData.read(VillagerBucket.CODEC).result();
        if (optional.isPresent()) {
            VillagerData data = optional.get();
            String type = data.getType().toString();
            switch (type) {
                case "desert" -> villagerBucket.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(1));
                case "savanna" -> villagerBucket.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(2));
                case "snow" -> villagerBucket.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(3));
                case "swamp" -> villagerBucket.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(4));
            }
        }

        return villagerBucket;
    }

    @Inject(at = @At("HEAD"), method = "defineSynchedData")
    public void defineSynchedData(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(FROM_BUCKET, false);
    }

    @Inject(at = @At("TAIL"), method = "addAdditionalSaveData")
    public void addAdditionalSaveData(CompoundTag compound, CallbackInfo ci) {
        compound.putBoolean("FromBucket", this.fromBucket());
    }

    @Inject(at = @At("TAIL"), method = "readAdditionalSaveData")
    public void readAdditionalSaveData(CompoundTag compound, CallbackInfo ci) {
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
        return new ItemStack(ModItems.VILLAGER_IN_A_BUCKET);
    }

    @Override
    public @NotNull SoundEvent getPickupSound() {
        return SoundEvents.VILLAGER_TRADE;
    }
}
