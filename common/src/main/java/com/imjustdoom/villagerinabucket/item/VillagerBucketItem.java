package com.imjustdoom.villagerinabucket.item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
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

public class VillagerBucketItem extends BucketItem {
    // For legacy item types only
    private final EntityType<? extends Mob> entityType;

    public VillagerBucketItem(EntityType<? extends Mob> entityType, Properties properties) {
        super(Fluids.EMPTY, properties);
        this.entityType = entityType;
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
            BlockPos blockPos1 = level.getBlockState(blockPos).getCollisionShape(level, blockPos).isEmpty() ? blockPos : blockPos.relative(blockHitResult.getDirection());
            // orElse handles if the "id" tag does not exist due to it using old data type
            EntityType<?> entityType = EntityType.by(itemStack.getOrCreateTag()).orElse(this.entityType);
            CompoundTag tag = itemStack.getOrCreateTag();

            // If it somehow ends up not a mob type. That is no longer my problem ¯\_(ツ)_/¯
            Mob mob = (Mob) entityType.create(serverLevel, tag, EntityType.createDefaultStackConfig(serverLevel, itemStack, null), blockPos1, MobSpawnType.BUCKET, true, blockPos != blockPos1 && blockHitResult.getDirection() == Direction.UP);
            if (mob instanceof Bucketable bucketable) {
                bucketable.loadFromBucketTag(itemStack.getTag());
                bucketable.setFromBucket(true);

                // If the bucket has no data it will read the biome of the place location like a spawn egg does
                if (mob instanceof Villager villager && !tag.contains("VillagerData", 10)) {
                    villager.setVillagerData(villager.getVillagerData().setType(VillagerType.byBiome(serverLevel.getBiome(blockPos1))));
                }

                serverLevel.addFreshEntityWithPassengers(mob);
            }
            level.gameEvent(player, GameEvent.ENTITY_PLACE, blockPos1);
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResultHolder.sidedSuccess(getEmptySuccessItem(itemStack, player), level.isClientSide());
    }

    @Override
    public Component getName(ItemStack itemStack) {
        if (itemStack.getTag() == null) return super.getName(itemStack);

        Optional<EntityType<?>> optionalEntityType = EntityType.by(itemStack.getTag());
        if (optionalEntityType.isEmpty()) return super.getName(itemStack);

        return Component.translatable(optionalEntityType.get().getDescriptionId()).append(Component.translatable("item.villagerinabucket.x_in_a_bucket"));
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
        CompoundTag compoundTag = itemStack.getTag();
        if (compoundTag == null) return;

        ChatFormatting[] chatFormattings = new ChatFormatting[]{ChatFormatting.ITALIC, ChatFormatting.GRAY};

        if (!compoundTag.contains("VillagerData")) return;

        CompoundTag data = compoundTag.getCompound("VillagerData");
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
