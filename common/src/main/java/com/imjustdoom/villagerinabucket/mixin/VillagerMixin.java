package com.imjustdoom.villagerinabucket.mixin;

import com.imjustdoom.villagerinabucket.VillagerBucketable;
import com.imjustdoom.villagerinabucket.config.Config;
import com.imjustdoom.villagerinabucket.item.ModItems;
import com.imjustdoom.villagerinabucket.item.custom.VillagerBucket;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Bucketable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.village.ReputationEventType;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

@Mixin(Villager.class)
public abstract class VillagerMixin extends AbstractVillager implements Bucketable, VillagerBucketable {
    @Shadow
    public abstract void onReputationEventFrom(ReputationEventType type, Entity target);

    @Shadow
    public abstract void releasePoi(MemoryModuleType<GlobalPos> moduleType);

    @Shadow
    public abstract VillagerData getVillagerData();

    @Shadow
    public abstract void setVillagerData(VillagerData villagerData);

    @Unique
    private boolean villagerinabucket$fromBucket;

    public VillagerMixin(EntityType<? extends AbstractVillager> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
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
        cir.setReturnValue(InteractionResult.SUCCESS);
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
        if (!villagerData.profession().is(VillagerProfession.NONE)
                && !villagerData.profession().is(VillagerProfession.NITWIT)
                && getVillagerXp() == 0 && villagerData.level() <= 1) {
            setVillagerData(villagerData.withProfession(registryAccess(), VillagerProfession.NONE));
        }

        ItemStack villagerBucket = getBucketItemStack();
        saveToBucketTag(villagerBucket);

        CustomData customData = villagerBucket.getOrDefault(DataComponents.BUCKET_ENTITY_DATA, CustomData.EMPTY);
        if (customData.isEmpty()) {
            return villagerBucket;
        }

        Optional<VillagerData> optional = customData.copyTag().read(VillagerBucket.CODEC);
        if (optional.isPresent()) {
            VillagerData data = optional.get();
            String type = data.type().getRegisteredName().split(":")[1];
            switch (type) {
                case "desert" -> villagerBucket.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(List.of(), List.of(), List.of("desert"), List.of()));
                case "savanna" -> villagerBucket.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(List.of(), List.of(), List.of("savanna"), List.of()));
                case "snow" -> villagerBucket.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(List.of(), List.of(), List.of("snow"), List.of()));
                case "swamp" -> villagerBucket.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(List.of(), List.of(), List.of("swamp"), List.of()));
            }
        }

        return villagerBucket;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    public void addAdditionalSaveData(ValueOutput valueOutput, CallbackInfo ci) {
        valueOutput.putBoolean("FromBucket", this.fromBucket());
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    public void readAdditionalSaveData(ValueInput valueInput, CallbackInfo ci) {
        this.setFromBucket(valueInput.getBooleanOr("FromBucket", false));
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
    public void saveToBucketTag(@NotNull ItemStack itemStack) {
        Bucketable.saveDefaultDataToBucketTag(this, itemStack);
        CustomData.update(DataComponents.BUCKET_ENTITY_DATA, itemStack, tag -> {
            TagValueOutput tagValueOutput = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, this.registryAccess());
            addAdditionalSaveData(tagValueOutput);
            tag.merge(tagValueOutput.buildResult());
        });
    }

    @Override
    public void loadFromBucketTag(@NotNull CompoundTag compoundTag) {
        Bucketable.loadDefaultDataFromBucketTag(this, compoundTag);
        readAdditionalSaveData(TagValueInput.create(ProblemReporter.DISCARDING, this.registryAccess(), compoundTag));
    }

    @Override
    public @NotNull ItemStack getBucketItemStack() {
        return new ItemStack(ModItems.VILLAGER_IN_A_BUCKET.right);
    }

    @Override
    public @NotNull SoundEvent getPickupSound() {
        return SoundEvents.VILLAGER_TRADE;
    }
}
