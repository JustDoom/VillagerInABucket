package com.imjustdoom.villagerinabucket.mixin;

import com.imjustdoom.villagerinabucket.VillagerBucketable;
import com.imjustdoom.villagerinabucket.item.ModItems;
import net.minecraft.advancements.CriteriaTriggers;
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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.WanderingTrader;
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

@Mixin({Villager.class, WanderingTrader.class})
public abstract class VillagerMixin extends AbstractVillager implements Bucketable, VillagerBucketable {

    @Shadow(remap = false) public abstract void readAdditionalSaveData(CompoundTag arg);
    @Shadow(remap = false) public abstract void addAdditionalSaveData(CompoundTag arg);

    private static final EntityDataAccessor<Boolean> FROM_BUCKET;

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
        ResourceLocation resourceLocation = EntityType.getKey(getType());
        tag.putString("type", resourceLocation.getNamespace() + ":" + resourceLocation.getPath());
        if (tag.getCompound("VillagerData").contains("type")) {
            String type = tag.getCompound("VillagerData").getString("type").split(":")[1];
            if (type.equals("desert")) {
                tag.putInt("CustomModelData", 1);
            } else if (type.equals("savanna")) {
                tag.putInt("CustomModelData", 2);
            } else if (type.equals("snow")) {
                tag.putInt("CustomModelData", 3);
            } else if (type.equals("swamp")) {
                tag.putInt("CustomModelData", 4);
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
        if (((AbstractVillager) this) instanceof Villager) {
            return new ItemStack(ModItems.VILLAGER_IN_A_BUCKET);
        } else if (((AbstractVillager) this) instanceof WanderingTrader) {
            return new ItemStack(ModItems.WANDERING_TRADER_IN_A_BUCKET);
        }

        return new ItemStack(ModItems.VILLAGER_IN_A_BUCKET);
    }

    @Override
    public @NotNull SoundEvent getPickupSound() {
        if (((AbstractVillager) this) instanceof Villager) {
            return SoundEvents.VILLAGER_TRADE;
        } else if (((AbstractVillager) this) instanceof WanderingTrader) {
            return SoundEvents.WANDERING_TRADER_NO;
        }

        return SoundEvents.VILLAGER_TRADE;
    }

    static {
        FROM_BUCKET = SynchedEntityData.defineId(Villager.class, EntityDataSerializers.BOOLEAN);
    }
}
