package com.imjustdoom.villagerinabucket.fabric;

import com.imjustdoom.villagerinabucket.VillagerInABucket;
import com.imjustdoom.villagerinabucket.item.ModItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;

import java.util.List;

public class VillagerInABucketFabric implements ModInitializer {

    private static final CreativeModeTab VILLAGERINABUCKET_TAB = FabricItemGroup.builder()
            .icon(() -> new ItemStack(ModItems.VILLAGER_IN_A_BUCKET.right))
            .title(Component.translatable("category.villagerinabucket.villagerinabucket_tab"))
            .displayItems((params, output) -> {
                // TODO: Try account for modded villager types and add them to the creative menu (with our default texture)

                output.accept(ModItems.VILLAGER_IN_A_BUCKET.right);
                output.accept(ModItems.WANDERING_TRADER_IN_A_BUCKET.right);

                for (ResourceKey<VillagerType> type : BuiltInRegistries.VILLAGER_TYPE.registryKeySet()) {
                    ItemStack itemStack = new ItemStack(ModItems.VILLAGER_IN_A_BUCKET.right);

                    if (VillagerInABucket.VILLAGER_DATA_LIST.containsKey(type)) {
                        itemStack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(List.of(), List.of(), List.of(VillagerInABucket.VILLAGER_DATA_LIST.get(type)), List.of()));
                    }

                    VillagerData villagerData = new VillagerData(BuiltInRegistries.VILLAGER_TYPE.getOrThrow(type), BuiltInRegistries.VILLAGER_PROFESSION.getOrThrow(VillagerProfession.NONE), 0);
                    VillagerData.CODEC.encodeStart(NbtOps.INSTANCE, villagerData).resultOrPartial(s -> System.out.println("Oh no something happened, no idea how or what the consequences are. Contact Villager In A Bucket support though"))
                            .ifPresent(tag -> CustomData.update(DataComponents.BUCKET_ENTITY_DATA, itemStack, compoundTag -> compoundTag.put("VillagerData", tag)));
                    output.accept(itemStack);
                }

                output.accept(ModItems.ZOMBIE_VILLAGER_IN_A_BUCKET.right);
            })
            .build();

    @Override
    public void onInitialize() {
        VillagerInABucket.init();

        Registry.register(BuiltInRegistries.ITEM, ModItems.VILLAGER_IN_A_BUCKET.left, ModItems.VILLAGER_IN_A_BUCKET.right);
        Registry.register(BuiltInRegistries.ITEM, ModItems.WANDERING_TRADER_IN_A_BUCKET.left, ModItems.WANDERING_TRADER_IN_A_BUCKET.right);
        Registry.register(BuiltInRegistries.ITEM, ModItems.ZOMBIE_VILLAGER_IN_A_BUCKET.left, ModItems.ZOMBIE_VILLAGER_IN_A_BUCKET.right);

        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ResourceLocation.fromNamespaceAndPath(VillagerInABucket.MOD_ID, "villagerinabucket_tab"), VILLAGERINABUCKET_TAB);

        VillagerInABucket.registerDispenserBehaviours();
    }
}
