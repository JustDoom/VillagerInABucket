package com.imjustdoom.villagerinabucket.forge.mixin;

import com.imjustdoom.villagerinabucket.VillagerBucketable;
import com.imjustdoom.villagerinabucket.config.Config;
import com.imjustdoom.villagerinabucket.item.ModItems;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
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
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import tallestegg.guardvillagers.entities.Guard;

import java.util.List;

@Mixin(Guard.class)
public abstract class GuardMixin extends PathfinderMob implements Bucketable, VillagerBucketable {
    @Shadow
    public abstract void onReputationEventFrom(ReputationEventType type, Entity target);

    @Shadow(remap = false) public abstract void readAdditionalSaveData(CompoundTag arg);
    @Shadow(remap = false) public abstract void addAdditionalSaveData(CompoundTag arg);

    @Shadow
    public abstract String getGuardVariant();

    private static final EntityDataAccessor<Boolean> FROM_BUCKET;

    public GuardMixin(EntityType<? extends AbstractVillager> entityType, Level level) {
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
        ItemStack villagerBucket = getBucketItemStack();
        saveToBucketTag(villagerBucket);

        CompoundTag tag = villagerBucket.getOrCreateTag();
        if (tag.getCompound("VillagerData").contains("type")) {
            String type = getGuardVariant();
            switch (type) {
                case "desert" -> tag.putInt("CustomModelData", 1);
                case "savanna" -> tag.putInt("CustomModelData", 2);
                case "snow" -> tag.putInt("CustomModelData", 3);
                case "swamp" -> tag.putInt("CustomModelData", 4);
            }
        }

        return villagerBucket;
    }

    @Override
    public boolean fromBucket() {
        return getEntityData().get(FROM_BUCKET);
    }

    @Override
    public void setFromBucket(boolean bl) {
        // this makes errors, do we need it?
//        ((Villager)(Object) this).getEntityData().set(FROM_BUCKET, bl);
    }

    @Override
    public void saveToBucketTag(ItemStack itemStack) {
        addAdditionalSaveData(itemStack.getOrCreateTag());
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

    static {
        FROM_BUCKET = SynchedEntityData.defineId(Guard.class, EntityDataSerializers.BOOLEAN);
    }
}
