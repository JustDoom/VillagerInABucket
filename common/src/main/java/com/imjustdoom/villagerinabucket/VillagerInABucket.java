package com.imjustdoom.villagerinabucket;

import com.imjustdoom.villagerinabucket.item.ModItems;
import com.mojang.serialization.DataResult;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.List;

public class VillagerInABucket {
    public static final String MOD_ID = "villagerinabucket";

    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(MOD_ID, Registries.CREATIVE_MODE_TAB);

    public static final RegistrySupplier<CreativeModeTab> VILLAGERINABUCKET_TAB = TABS.register(
            "villagerinabucket_tab",
            () -> CreativeTabRegistry.create(builder -> {
                builder.title(Component.translatable("category.villagerinabucket.villagerinabucket_tab"));
                builder.icon(() -> new ItemStack(ModItems.VILLAGER_IN_A_BUCKET.get()));
                builder.displayItems((params, output) -> {
                    List<VillagerData> villagerDataList = Arrays.asList(
                            new VillagerData(VillagerType.DESERT, VillagerProfession.NONE, 0),
                            new VillagerData(VillagerType.SAVANNA, VillagerProfession.NONE, 0),
                            new VillagerData(VillagerType.SNOW, VillagerProfession.NONE, 0),
                            new VillagerData(VillagerType.SWAMP, VillagerProfession.NONE, 0)
                    );

                    // TODO: Maybe add every type
                    for (int i = 1; i <= 4; i++) {
                        ItemStack itemStack = new ItemStack(ModItems.VILLAGER_IN_A_BUCKET.get());
                        CompoundTag compoundTag = itemStack.getOrCreateTag();
                        compoundTag.putInt("CustomModelData", i);

                        VillagerData villagerData = villagerDataList.get(i - 1);
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
}
