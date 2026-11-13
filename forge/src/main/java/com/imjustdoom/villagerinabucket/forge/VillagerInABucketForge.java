package com.imjustdoom.villagerinabucket.forge;

import com.imjustdoom.villagerinabucket.VillagerInABucket;
import com.imjustdoom.villagerinabucket.item.ModItems;
import com.mojang.serialization.DataResult;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(VillagerInABucket.MOD_ID)
public class VillagerInABucketForge {

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, VillagerInABucket.MOD_ID);
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), VillagerInABucket.MOD_ID);

    public static final RegistryObject<CreativeModeTab> VILLAGERINABUCKET_TAB = TABS.register("villagerinabucket_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("category.villagerinabucket.villagerinabucket_tab"))
            .icon(() -> new ItemStack(ModItems.VILLAGER_IN_A_BUCKET))
            .displayItems((params, output) -> VillagerInABucket.registerCreativeTabItems(output))
            .build()
    );

    public VillagerInABucketForge() {
        VillagerInABucket.init();

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);

        ITEMS.register("villager_in_a_bucket", () -> ModItems.VILLAGER_IN_A_BUCKET);
        ITEMS.register("wandering_trader_in_a_bucket", () -> ModItems.WANDERING_TRADER_IN_A_BUCKET);
        ITEMS.register("zombie_villager_in_a_bucket", () -> ModItems.ZOMBIE_VILLAGER_IN_A_BUCKET);

        ITEMS.register(modEventBus);
        TABS.register(modEventBus);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        VillagerInABucket.registerDispenserBehaviours();
    }
}
