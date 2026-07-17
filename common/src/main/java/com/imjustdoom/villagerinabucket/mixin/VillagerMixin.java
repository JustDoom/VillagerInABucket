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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.AcquirePoi;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.village.ReputationEventType;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
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

    @Unique
    private static final EntityDataAccessor<Boolean> FROM_BUCKET = SynchedEntityData.defineId(VillagerMixin.class, EntityDataSerializers.BOOLEAN);

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

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    public void defineSynchedData(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(FROM_BUCKET, false);
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
        return getEntityData().get(FROM_BUCKET);
    }

    @Override
    public void setFromBucket(boolean fromBucket) {
        getEntityData().set(FROM_BUCKET, fromBucket);
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

        // Clear home, job location etc when moved far away to avoid the villager trying to walk all the way back home
        // or stop job reassignment issues
        forgetDistantPoi(MemoryModuleType.HOME);
        forgetDistantPoi(MemoryModuleType.JOB_SITE);
        forgetDistantPoi(MemoryModuleType.POTENTIAL_JOB_SITE);
        forgetDistantPoi(MemoryModuleType.MEETING_POINT);
    }

    /**
     * Forget POI info if the distance is greater than the configured forget value.
     * This is used to solve Villagers not always forgetting their home location or job data.
     * It sometimes works but if the villager thinks it is able to return home it will keep attempting it
     */
    @Unique
    private void forgetDistantPoi(MemoryModuleType<GlobalPos> memory) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        Brain<?> brain = this.getBrain();
        GlobalPos pos = brain.getMemory(memory).orElse(null);
        if (pos == null) {
            return;
        }

        // Check if the villager in within keep poi distance
        if (pos.dimension().equals(serverLevel.dimension()) && pos.pos().closerThan(this.blockPosition(), Config.RESET_POI_DISTANCE)) {
            return;
        }

        // Check if the poi is claimed by another villager just in case. Since while in the bucket one can take over
        // so we don't want to blindly clear it
        if (!poiClaimedByOtherVillager(serverLevel, memory, pos)) {
            this.releasePoi(memory);
        }
        brain.eraseMemory(memory);
    }

    /**
     * Check if the poi is actually claimed by another villager or not
     */
    @Unique
    private boolean poiClaimedByOtherVillager(ServerLevel level, MemoryModuleType<GlobalPos> memory, GlobalPos pos) {
        ServerLevel poiLevel = level.getServer().getLevel(pos.dimension());
        if (poiLevel == null) {
            return false;
        }
        return !poiLevel.getEntitiesOfClass(Villager.class, new AABB(pos.pos()).inflate(AcquirePoi.SCAN_RANGE),
                other -> other != (Object) this && pos.equals(other.getBrain().getMemory(memory).orElse(null))).isEmpty();
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
