package com.imjustdoom.villagerinabucket.forge;

import com.imjustdoom.villagerinabucket.VillagerInABucket;
import com.imjustdoom.villagerinabucket.item.ModItems;
import com.mojang.serialization.DataResult;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.function.Supplier;

@Mod(VillagerInABucket.MOD_ID)
public class VillagerInABucketForge {

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, VillagerInABucket.MOD_ID);
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), VillagerInABucket.MOD_ID);

    public static final Supplier<CreativeModeTab> VILLAGERINABUCKET_TAB = TABS.register("villagerinabucket_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("category.villagerinabucket.villagerinabucket_tab"))
            .icon(() -> new ItemStack(ModItems.VILLAGER_IN_A_BUCKET.right))
            .displayItems((params, output) -> {
                // TODO: Try account for modded villager types and add them to the creative menu (with our default texture)

                output.accept(ModItems.VILLAGER_IN_A_BUCKET.right);
                output.accept(ModItems.WANDERING_TRADER_IN_A_BUCKET.right);

                for (VillagerType type : BuiltInRegistries.VILLAGER_TYPE) {
                    ItemStack itemStack = new ItemStack(ModItems.VILLAGER_IN_A_BUCKET.right);

                    if (VillagerInABucket.VILLAGER_DATA_LIST.containsKey(type)) {
                        itemStack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(List.of(), List.of(), List.of(VillagerInABucket.VILLAGER_DATA_LIST.get(type)), List.of()));
                    }

                    VillagerData villagerData = new VillagerData(type, VillagerProfession.NONE, 0);
                    VillagerData.CODEC.encodeStart(NbtOps.INSTANCE, villagerData).resultOrPartial(s -> System.out.println("Oh no something happened, no idea how or what the consequences are. Contact Villager In A Bucket support though"))
                            .ifPresent(tag -> CustomData.update(DataComponents.BUCKET_ENTITY_DATA, itemStack, compoundTag -> compoundTag.put("VillagerData", tag)));
                    output.accept(itemStack);
                }

                output.accept(ModItems.ZOMBIE_VILLAGER_IN_A_BUCKET.right);
            })
            .build()
    );

    public VillagerInABucketForge(FMLJavaModLoadingContext context) {
        VillagerInABucket.init();

        IEventBus modEventBus = context.getModEventBus();
        modEventBus.addListener(this::commonSetup);

        ITEMS.register("villager_in_a_bucket", () -> ModItems.VILLAGER_IN_A_BUCKET.right);
        ITEMS.register("wandering_trader_in_a_bucket", () -> ModItems.WANDERING_TRADER_IN_A_BUCKET.right);
        ITEMS.register("zombie_villager_in_a_bucket", () -> ModItems.ZOMBIE_VILLAGER_IN_A_BUCKET.right);

        ITEMS.register(modEventBus);
        TABS.register(modEventBus);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        VillagerInABucket.registerDispenserBehaviours();
    }
}
