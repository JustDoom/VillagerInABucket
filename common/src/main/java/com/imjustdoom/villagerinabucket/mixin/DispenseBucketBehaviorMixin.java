package com.imjustdoom.villagerinabucket.mixin;

import com.imjustdoom.villagerinabucket.VillagerBucketable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(targets = "net/minecraft/core/dispenser/DispenseItemBehavior$7")
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

                    cir.setReturnValue(stack);
                    return;
                }
            }
        }
    }
}
