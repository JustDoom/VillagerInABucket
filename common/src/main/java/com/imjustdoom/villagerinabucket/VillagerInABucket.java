package com.imjustdoom.villagerinabucket;

import com.imjustdoom.villagerinabucket.item.ModItems;
import com.mojang.serialization.DataResult;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DispensibleContainerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.BlockHitResult;

import java.util.HashMap;
import java.util.Map;

public class VillagerInABucket {
    public static final String MOD_ID = "villagerinabucket";

    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(MOD_ID, Registries.CREATIVE_MODE_TAB);

    private static final Map<VillagerType, Integer> VILLAGER_DATA_LIST = new HashMap<>() {
        {
            put(VillagerType.DESERT, 1);
            put(VillagerType.SAVANNA, 2);
            put(VillagerType.SNOW, 3);
            put(VillagerType.SWAMP, 4);
        }
    };

    public static final RegistrySupplier<CreativeModeTab> VILLAGERINABUCKET_TAB = TABS.register(
            "villagerinabucket_tab",
            () -> CreativeTabRegistry.create(builder -> {
                builder.title(Component.translatable("category.villagerinabucket.villagerinabucket_tab"));
                builder.icon(() -> new ItemStack(ModItems.VILLAGER_IN_A_BUCKET.get()));
                builder.displayItems((params, output) -> {
                    // TODO: Try account for modded villager types and add them to the creative menu (with our default texture)
                    for (VillagerType type : BuiltInRegistries.VILLAGER_TYPE) {
                        ItemStack itemStack = new ItemStack(ModItems.VILLAGER_IN_A_BUCKET.get());
                        CompoundTag compoundTag = itemStack.getOrCreateTag();

                        if (VILLAGER_DATA_LIST.containsKey(type)) {
                            compoundTag.putInt("CustomModelData", VILLAGER_DATA_LIST.get(type));
                        }

                        VillagerData villagerData = new VillagerData(type, VillagerProfession.NONE, 0);
                        DataResult<Tag> data = VillagerData.CODEC.encodeStart(NbtOps.INSTANCE, villagerData);

                        data.resultOrPartial(s -> System.out.println("Oh no something happened, no idea how or what the consequences are. Contact Villager In A Bucket support though")).ifPresent(tag -> compoundTag.put("VillagerData", tag));
                        output.accept(itemStack);
                    }
                });
            })
    );

    public static void init() {
        TABS.register();

        ModItems.init();
    }

    public static void registerDispenserBehaviours() {
        DispenseItemBehavior dispenseItemBehavior = new DefaultDispenseItemBehavior() {
            private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();

            public ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
                DispensibleContainerItem dispensibleContainerItem = (DispensibleContainerItem) itemStack.getItem();
                BlockPos blockPos = blockSource.getPos().relative(blockSource.getBlockState().getValue(DispenserBlock.FACING));
                Level level = blockSource.getLevel();
                if (dispensibleContainerItem.emptyContents(null, level, blockPos, null)) {
                    dispensibleContainerItem.checkExtraContent(null, level, itemStack, blockPos);
                    return new ItemStack(Items.BUCKET);
                } else {
                    return this.defaultDispenseItemBehavior.dispense(blockSource, itemStack);
                }
            }
        };

        DispenserBlock.registerBehavior(ModItems.VILLAGER_IN_A_BUCKET.get(), dispenseItemBehavior);
        DispenserBlock.registerBehavior(ModItems.WANDERING_TRADER_IN_A_BUCKET.get(), dispenseItemBehavior);
    }
}
