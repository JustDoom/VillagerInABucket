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
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class VillagerBucket extends MobBucketItem {

    public static final MapCodec<VillagerData> CODEC;

    public VillagerBucket(EntityType<?> entityType, SoundEvent soundEvent, Properties properties) {
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
    public void appendHoverText(ItemStack itemStack, Item.TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipFlag) {
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
            ChatFormatting[] chatFormattings = new ChatFormatting[]{ChatFormatting.ITALIC, ChatFormatting.GRAY};

            list.add(Component.translatable("Level: " + data.getLevel()).withStyle(chatFormattings));

            String type = data.getType().toString(); //.toString().split(":")[1];
            String region = I18n.get((type.equals("snow") ? "block.minecraft.snow" : "biome.minecraft." + type));
            list.add(Component.translatable("Region: " + region).withStyle(chatFormattings));

            String profession = I18n.get("entity.minecraft.villager." + data.getProfession().toString());//.split(":")[1]);
            list.add(Component.translatable("Profession: " + profession).withStyle(chatFormattings));

            if (customData.contains("Age") && customData.copyTag().getInt("Age") < 0) {
                list.add(Component.literal("Baby").withStyle(chatFormattings));
            }
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

    static {
        CODEC = VillagerData.CODEC.fieldOf("VillagerData");
    }
}
