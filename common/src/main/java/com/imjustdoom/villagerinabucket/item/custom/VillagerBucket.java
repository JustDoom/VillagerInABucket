package com.imjustdoom.villagerinabucket.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class VillagerBucket extends MobBucketItem {

    public VillagerBucket(EntityType<?> entityType, SoundEvent soundEvent, Properties properties) {
        super(entityType, Fluids.EMPTY, soundEvent, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        BlockHitResult blockHitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        if (blockHitResult.getType() == HitResult.Type.MISS || blockHitResult.getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(itemStack);
        }
        BlockPos blockPos = blockHitResult.getBlockPos();
        if (!level.mayInteract(player, blockPos)) {
            return InteractionResultHolder.fail(itemStack);
        }

        if (level instanceof ServerLevel serverLevel) {
            CompoundTag compoundTag = itemStack.getTag();
            if (compoundTag == null) {
                checkExtraContent(player, serverLevel, itemStack, blockPos);
            } else {
                Optional<EntityType<?>> optionalEntityType = EntityType.byString(itemStack.getTag().getString("type"));

                if (optionalEntityType.isPresent()) {
                    BlockPos blockPos1 = level.getBlockState(blockPos).getCollisionShape(level, blockPos).isEmpty() ? blockPos : blockPos.relative(blockHitResult.getDirection());
                    Entity entity = optionalEntityType.get().spawn(serverLevel, itemStack, null, blockPos, MobSpawnType.BUCKET, true, blockPos != blockPos1 && blockHitResult.getDirection() == Direction.UP);
                    if (entity instanceof Bucketable bucketable) {
                        bucketable.loadFromBucketTag(itemStack.getOrCreateTag());
                        bucketable.setFromBucket(true);
                    }
                } else {
                    checkExtraContent(player, serverLevel, itemStack, blockPos);
                }
                serverLevel.gameEvent(player, GameEvent.ENTITY_PLACE, blockPos);
            }
        }

        // TODO: Try make bucket with no nbt make villager type the same as biome spawned in
        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResultHolder.sidedSuccess(getEmptySuccessItem(itemStack, player), level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
        CompoundTag compoundTag = itemStack.getTag();
        if (compoundTag == null) {
            return;
        }

        ChatFormatting[] chatFormattings = new ChatFormatting[]{ChatFormatting.ITALIC, ChatFormatting.GRAY};

        CompoundTag data = compoundTag.getCompound("VillagerData");

        Optional<EntityType<?>> optionalEntityType = EntityType.byString(compoundTag.getString("type"));
        optionalEntityType.ifPresent(entityType -> {
            if (entityType != EntityType.VILLAGER) {
                list.add(Component.translatable("Type: " + I18n.get(entityType.getDescriptionId())).withStyle(chatFormattings));
            }
            ;
        });

        if (data.contains("level")) {
            list.add(Component.translatable("Level: " + data.getInt("level")).withStyle(chatFormattings));
        }
        if (data.contains("type")) {
            String type = data.getString("type").split(":")[1];
            String region = I18n.get((type.equals("snow") ? "block.minecraft.snow" : "biome.minecraft." + type));
            list.add(Component.translatable("Region: " + region).withStyle(chatFormattings));
        }
        if (data.contains("profession")) {
            String profession = I18n.get("entity.minecraft.villager." + data.getString("profession").split(":")[1]);
            list.add(Component.translatable("Profession: " + profession).withStyle(chatFormattings));
        }
        if (compoundTag.contains("Age") && compoundTag.getInt("Age") < 0) {
            list.add(Component.literal("Baby").withStyle(chatFormattings));
        }
    }

    @Override
    public boolean emptyContents(@Nullable Player player, Level level, BlockPos blockPos, @Nullable BlockHitResult blockHitResult) {
        BlockState blockstate = level.getBlockState(blockPos);
        if (blockstate.isAir() || blockstate.canBeReplaced(Fluids.EMPTY)) {
            this.playEmptySound(player, level, blockPos);
            return true;
        } else {
            return false;
        }
    }
}
