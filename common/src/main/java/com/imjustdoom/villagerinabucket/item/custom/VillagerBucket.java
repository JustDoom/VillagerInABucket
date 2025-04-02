package com.imjustdoom.villagerinabucket.item.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

public class VillagerBucket extends MobBucketItem {

    public static final MapCodec<VillagerData> CODEC;

    public VillagerBucket(EntityType<? extends Mob> entityType, SoundEvent soundEvent, Properties properties) {
        super(entityType, Fluids.EMPTY, soundEvent, properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        BlockHitResult blockHitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        if (blockHitResult.getType() == HitResult.Type.MISS) {
            return InteractionResult.PASS;
        } else if (blockHitResult.getType() != HitResult.Type.BLOCK) {
            return InteractionResult.PASS;
        } else {
            BlockPos blockPos = blockHitResult.getBlockPos();
            if (level.mayInteract(player, blockPos)) {
                if (itemStack.getOrDefault(DataComponents.BUCKET_ENTITY_DATA, CustomData.EMPTY).isEmpty()) {
                    CustomData oldData = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                    if (!oldData.isEmpty()) {
                        oldData.read(CODEC).result().ifPresent(villagerData -> itemStack.set(DataComponents.BUCKET_ENTITY_DATA, oldData));
                    }
                }

                checkExtraContent(player, level, itemStack, blockPos);
                if (player instanceof ServerPlayer) {
                    CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer) player, blockPos, itemStack);
                }
                // TODO: Try make bucket with no nbt make villager type the same as biome spawned in
                player.awardStat(Stats.ITEM_USED.get(this));
                return InteractionResult.SUCCESS.heldItemTransformedTo(getEmptySuccessItem(itemStack, player));
            } else {
                return InteractionResult.FAIL;
            }
        }
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
    public boolean emptyContents(@Nullable LivingEntity livingEntity, Level level, @NotNull BlockPos blockPos, @Nullable BlockHitResult blockHitResult) {
        BlockState blockstate = level.getBlockState(blockPos);
        if (blockstate.isAir() || blockstate.canBeReplaced(Fluids.EMPTY)) {
            this.playEmptySound(livingEntity, level, blockPos);
            return true;
        } else {
            return false;
        }
    }

    static {
        CODEC = VillagerData.CODEC.fieldOf("VillagerData");
    }
}
