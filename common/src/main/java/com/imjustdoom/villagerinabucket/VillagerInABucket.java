package com.imjustdoom.villagerinabucket;

import com.imjustdoom.villagerinabucket.item.ModItems;
import com.mojang.serialization.DataResult;
import dev.architectury.registry.CreativeTabRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DispensibleContainerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class VillagerInABucket {
    public static final String MOD_ID = "villagerinabucket";

    private static final Map<VillagerType, Integer> VILLAGER_DATA_LIST = new HashMap<>() {
        {
            put(VillagerType.DESERT, 1);
            put(VillagerType.SAVANNA, 2);
            put(VillagerType.SNOW, 3);
            put(VillagerType.SWAMP, 4);
        }
    };

//    public static final RegistrySupplier<CreativeModeTab> VILLAGERINABUCKET_TAB = TABS.register(
//            "villagerinabucket_tab",
//            () -> CreativeTabRegistry.create(builder -> {
//                builder.title(Component.translatable("category.villagerinabucket.villagerinabucket_tab"));
//                builder.icon(() -> new ItemStack(ModItems.VILLAGER_IN_A_BUCKET.get()));
//                builder.displayItems((params, output) -> {
//                    // TODO: Try account for modded villager types and add them to the creative menu (with our default texture)
//                    for (VillagerType type : BuiltInRegistries.VILLAGER_TYPE) {
//                        ItemStack itemStack = new ItemStack(ModItems.VILLAGER_IN_A_BUCKET.get());
//                        CompoundTag compoundTag = itemStack.getOrCreateTag();
//
//                        if (VILLAGER_DATA_LIST.containsKey(type)) {
//                            compoundTag.putInt("CustomModelData", VILLAGER_DATA_LIST.get(type));
//                        }
//
//                        VillagerData villagerData = new VillagerData(type, VillagerProfession.NONE, 0);
//                        DataResult<Tag> data = VillagerData.CODEC.encodeStart(NbtOps.INSTANCE, villagerData);
//
//                        data.resultOrPartial(s -> System.out.println("Oh no something happened, no idea how or what the consequences are. Contact Villager In A Bucket support though")).ifPresent(tag -> compoundTag.put("VillagerData", tag));
//                        output.accept(itemStack);
//                    }
//                });
//            })
//    );

    public static final CreativeModeTab VILLAGERINABUCKET_TAB = CreativeTabRegistry.create(
            new ResourceLocation(MOD_ID, "villagerinabucket_tab"), // Tab ID
            () -> new ItemStack(ModItems.VILLAGER_IN_A_BUCKET.get()) // Icon
    );

    public static void init() {
        ModItems.init();

        NonNullList<ItemStack> itemStacks = NonNullList.create();
        for (VillagerType type : Registry.VILLAGER_TYPE) {
            ItemStack itemStack = new ItemStack(ModItems.VILLAGER_IN_A_BUCKET.get());
            CompoundTag compoundTag = itemStack.getOrCreateTag();

            if (VILLAGER_DATA_LIST.containsKey(type)) {
                compoundTag.putInt("CustomModelData", VILLAGER_DATA_LIST.get(type));
            }

            VillagerData villagerData = new VillagerData(type, VillagerProfession.NONE, 0);
            DataResult<Tag> data = VillagerData.CODEC.encodeStart(NbtOps.INSTANCE, villagerData);

            data.resultOrPartial(s -> System.out.println("Oh no something happened, no idea how or what the consequences are. Contact Villager In A Bucket support though")).ifPresent(tag -> compoundTag.put("VillagerData", tag));
            itemStacks.add(itemStack);
        }

        VILLAGERINABUCKET_TAB.fillItemList(itemStacks);
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
