package com.imjustdoom.villagerinabucket.mixin;

import com.imjustdoom.villagerinabucket.VillagerBucketable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(targets = "net/minecraft/core/dispenser/DispenseItemBehavior$6")
public abstract class DispenseBucketBehaviorMixin {

    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private void execute(BlockSource blockSource, ItemStack itemStack, CallbackInfoReturnable<ItemStack> cir) {
        ServerLevel serverLevel = blockSource.level();
        if (!serverLevel.isClientSide()) {
            BlockPos blockPos = blockSource.pos().relative(blockSource.state().getValue(DispenserBlock.FACING));
            List<LivingEntity> list = serverLevel.getEntitiesOfClass(LivingEntity.class, new AABB(blockPos), EntitySelector.NO_SPECTATORS);

            for (LivingEntity livingEntity : list) {
                if (!livingEntity.isAlive()) continue;
                if (livingEntity instanceof VillagerBucketable villager) {
                    ItemStack stack = villager.createBucketStack();
                    livingEntity.discard();

                    cir.setReturnValue(villagerinabucket$consume(blockSource, itemStack, new ItemStack(stack.getItem())));
                    return;
                }
            }
        }
    }

    @Unique
    private ItemStack villagerinabucket$consume(BlockSource blockSource, ItemStack stack, ItemStack remainder) {
        stack.shrink(1);
        if (stack.isEmpty()) {
            return remainder;
        } else {
            ItemStack itemStack = blockSource.blockEntity().insertItem(remainder);
            if (!itemStack.isEmpty()) {
                Direction direction = blockSource.state().getValue(DispenserBlock.FACING);
                DefaultDispenseItemBehavior.spawnItem(blockSource.level(), itemStack, 6, direction, DispenserBlock.getDispensePosition(blockSource));
                blockSource.level().levelEvent(1000, blockSource.pos(), 0);
                blockSource.level().levelEvent(2000, blockSource.pos(), direction.get3DDataValue());
            }
            return stack;
        }
    }
}
