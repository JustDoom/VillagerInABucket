package com.imjustdoom.villagerinabucket.item.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

public class VillagerBucket extends BucketItem {
    public static final MapCodec<VillagerData> CODEC;

    private final EntityType<? extends Mob> type;
    private final SoundEvent emptySound;

    public VillagerBucket(EntityType<? extends Mob> entityType, SoundEvent soundEvent, Properties properties) {
        super(Fluids.EMPTY, properties);
        this.type = entityType;
        this.emptySound = soundEvent;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        BlockHitResult blockHitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        if (blockHitResult.getType() == HitResult.Type.MISS || blockHitResult.getType() != HitResult.Type.BLOCK) {
            return InteractionResult.PASS;
        }

        BlockPos blockPos = blockHitResult.getBlockPos();
        if (!level.mayInteract(player, blockPos)) {
            return InteractionResult.FAIL;
        }

        CustomData customData = itemStack.getOrDefault(DataComponents.BUCKET_ENTITY_DATA, CustomData.EMPTY);
        CustomData oldData = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        if (customData.isEmpty() && !oldData.isEmpty()) {
            oldData.read(CODEC).result().ifPresent(villagerData -> itemStack.set(DataComponents.BUCKET_ENTITY_DATA, oldData));
        }

        BlockPos blockPos1 = level.getBlockState(blockPos).getCollisionShape(level, blockPos).isEmpty() ? blockPos : blockPos.relative(blockHitResult.getDirection());
        if (level instanceof ServerLevel serverLevel) {
            Mob mob = this.type.create(serverLevel, EntityType.createDefaultStackConfig(serverLevel, itemStack, null), blockPos1, EntitySpawnReason.BUCKET, true, blockPos != blockPos1 && blockHitResult.getDirection() == Direction.UP);
            if (mob instanceof Bucketable bucketable) {
                bucketable.loadFromBucketTag(customData.copyTag());
                bucketable.setFromBucket(true);
            }

            if (mob != null) {
                serverLevel.addFreshEntityWithPassengers(mob);
                mob.playAmbientSound();
            }
            level.gameEvent(player, GameEvent.ENTITY_PLACE, blockPos1);
        }
        // TODO: Try make bucket with no nbt make villager type the same as biome spawned in
        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResult.SUCCESS.heldItemTransformedTo(getEmptySuccessItem(itemStack, player));

    }

    @Override
    public void appendHoverText(ItemStack itemStack, @NotNull TooltipContext tooltipContext, @NotNull TooltipDisplay tooltipDisplay, @NotNull Consumer<Component> list, @NotNull TooltipFlag tooltipFlag) {
        CustomData customData = itemStack.getOrDefault(DataComponents.BUCKET_ENTITY_DATA, CustomData.EMPTY);
        if (customData.isEmpty()) {
            customData = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
            if (customData.isEmpty()) {
                return;
            }
        }

        Optional<VillagerData> optional = customData.read(CODEC).result();
        if (optional.isPresent()) {
            VillagerData data = optional.get();
            ChatFormatting[] chatFormatting = new ChatFormatting[]{ChatFormatting.ITALIC, ChatFormatting.GRAY};

            list.accept(Component.translatable("Level: " + data.level()).withStyle(chatFormatting));

            String type = data.type().getRegisteredName().split(":")[1];
            String region = I18n.get((type.equals("snow") ? "block.minecraft.snow" : "biome.minecraft." + type));
            list.accept(Component.translatable("Region: " + region).withStyle(chatFormatting));

            String profession = I18n.get("entity.minecraft.villager." + data.profession().getRegisteredName().split(":")[1]);
            list.accept(Component.translatable("Profession: " + profession).withStyle(chatFormatting));

            if (customData.copyTag().getInt("Age").isPresent() && customData.copyTag().getInt("Age").get() < 0) {
                list.accept(Component.literal("Baby").withStyle(chatFormatting));
            }
        }
    }

    @Override
    public void checkExtraContent(@Nullable LivingEntity entity, Level level, ItemStack stack, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            Mob mob = this.type.create(serverLevel, EntityType.createDefaultStackConfig(serverLevel, stack, null), pos, EntitySpawnReason.BUCKET, true, false);
            if (mob instanceof Bucketable bucketable) {
                CustomData customData = stack.getOrDefault(DataComponents.BUCKET_ENTITY_DATA, CustomData.EMPTY);
                bucketable.loadFromBucketTag(customData.copyTag());
                bucketable.setFromBucket(true);
            }

            if (mob != null) {
                serverLevel.addFreshEntityWithPassengers(mob);
                mob.playAmbientSound();
            }
            level.gameEvent(entity, GameEvent.ENTITY_PLACE, pos);
        }
    }

    @Override
    public boolean emptyContents(@Nullable LivingEntity livingEntity, Level level, @NotNull BlockPos blockPos, @Nullable BlockHitResult blockHitResult) {
        BlockState blockstate = level.getBlockState(blockPos);
        if (blockstate.isAir() || blockstate.canBeReplaced(Fluids.EMPTY)) {
            level.playSound(livingEntity, blockPos, this.emptySound, SoundSource.NEUTRAL, 1.0F, 1.0F);
            return true;
        } else {
            return false;
        }
    }

    static {
        CODEC = VillagerData.CODEC.fieldOf("VillagerData");
    }
}
