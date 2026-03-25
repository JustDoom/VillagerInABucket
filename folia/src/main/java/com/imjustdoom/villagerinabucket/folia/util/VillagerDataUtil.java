package com.imjustdoom.villagerinabucket.folia.util;

import com.imjustdoom.villagerinabucket.folia.VillagerInABucketPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public final class VillagerDataUtil {

    private static final Map<String, Integer> VILLAGER_TYPE_MODEL_DATA = Map.of(
            "plains", 1,
            "desert", 2,
            "savanna", 3,
            "snow", 4,
            "swamp", 5
    );

    private VillagerDataUtil() {}

    public static ItemStack createVillagerBucket(Entity entity) {
        ItemStack bucket = new ItemStack(Material.BUCKET);
        ItemMeta meta = bucket.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        pdc.set(VillagerInABucketPlugin.KEY_VILLAGER_BUCKET, PersistentDataType.BOOLEAN, true);
        pdc.set(VillagerInABucketPlugin.KEY_ENTITY_TYPE, PersistentDataType.STRING, entity.getType().key().value());

        String serialized = serializeEntityData(entity);
        pdc.set(VillagerInABucketPlugin.KEY_ENTITY_DATA, PersistentDataType.STRING, serialized);

        meta.displayName(getDisplayName(entity));

        List<Component> lore = buildLore(entity);
        if (!lore.isEmpty()) {
            meta.lore(lore);
        }

        meta.setCustomModelData(getCustomModelData(entity));

        bucket.setItemMeta(meta);
        return bucket;
    }

    public static boolean isVillagerBucket(ItemStack item) {
        if (item == null || item.getType() != Material.BUCKET) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(VillagerInABucketPlugin.KEY_VILLAGER_BUCKET, PersistentDataType.BOOLEAN);
    }

    public static Entity spawnFromBucket(ItemStack bucket, org.bukkit.Location location) {
        ItemMeta meta = bucket.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        String entityTypeName = pdc.get(VillagerInABucketPlugin.KEY_ENTITY_TYPE, PersistentDataType.STRING);
        String data = pdc.get(VillagerInABucketPlugin.KEY_ENTITY_DATA, PersistentDataType.STRING);

        if (entityTypeName == null || data == null) return null;

        EntityType entityType = Registry.ENTITY_TYPE.get(org.bukkit.NamespacedKey.minecraft(entityTypeName));
        if (entityType == null) return null;

        Entity entity = location.getWorld().spawnEntity(location, entityType);
        deserializeEntityData(entity, data);

        return entity;
    }

    private static String getTypeKey(Villager.Type type) {
        return type.key().value();
    }

    private static String getProfessionKey(Villager.Profession profession) {
        return profession.key().value();
    }

    private static Villager.Type typeFromKey(String key) {
        return Registry.VILLAGER_TYPE.get(org.bukkit.NamespacedKey.minecraft(key));
    }

    private static Villager.Profession professionFromKey(String key) {
        return Registry.VILLAGER_PROFESSION.get(org.bukkit.NamespacedKey.minecraft(key));
    }

    private static String serializeEntityData(Entity entity) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

            if (entity instanceof Villager villager) {
                dos.writeUTF("VILLAGER");
                dos.writeUTF(getProfessionKey(villager.getProfession()));
                dos.writeUTF(getTypeKey(villager.getVillagerType()));
                dos.writeInt(villager.getVillagerLevel());
                dos.writeInt(villager.getVillagerExperience());
                dos.writeDouble(villager.getHealth());
                dos.writeBoolean(!villager.isAdult());
                dos.writeBoolean(villager.customName() != null);
                if (villager.customName() != null) {
                    dos.writeUTF(net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson().serialize(villager.customName()));
                }

                List<MerchantRecipe> recipes = villager.getRecipes();
                dos.writeInt(recipes.size());
                for (MerchantRecipe recipe : recipes) {
                    serializeRecipe(dos, recipe);
                }
            } else if (entity instanceof WanderingTrader trader) {
                dos.writeUTF("WANDERING_TRADER");
                dos.writeDouble(trader.getHealth());
                dos.writeBoolean(trader.customName() != null);
                if (trader.customName() != null) {
                    dos.writeUTF(net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson().serialize(trader.customName()));
                }

                List<MerchantRecipe> recipes = trader.getRecipes();
                dos.writeInt(recipes.size());
                for (MerchantRecipe recipe : recipes) {
                    serializeRecipe(dos, recipe);
                }
            } else if (entity instanceof ZombieVillager zombieVillager) {
                dos.writeUTF("ZOMBIE_VILLAGER");
                dos.writeUTF(getProfessionKey(zombieVillager.getVillagerProfession()));
                dos.writeUTF(getTypeKey(zombieVillager.getVillagerType()));
                dos.writeDouble(zombieVillager.getHealth());
                dos.writeBoolean(!zombieVillager.isAdult());
                dos.writeBoolean(zombieVillager.customName() != null);
                if (zombieVillager.customName() != null) {
                    dos.writeUTF(net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson().serialize(zombieVillager.customName()));
                }
            }

            dos.flush();
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void deserializeEntityData(Entity entity, String base64) {
        try {
            byte[] bytes = Base64.getDecoder().decode(base64);
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes));

            String type = dis.readUTF();

            switch (type) {
                case "VILLAGER" -> {
                    Villager villager = (Villager) entity;
                    villager.setProfession(professionFromKey(dis.readUTF()));
                    villager.setVillagerType(typeFromKey(dis.readUTF()));
                    villager.setVillagerLevel(dis.readInt());
                    villager.setVillagerExperience(dis.readInt());
                    villager.setHealth(dis.readDouble());
                    if (dis.readBoolean()) villager.setBaby();
                    if (dis.readBoolean()) {
                        villager.customName(net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson().deserialize(dis.readUTF()));
                    }

                    int recipeCount = dis.readInt();
                    List<MerchantRecipe> recipes = new ArrayList<>();
                    for (int i = 0; i < recipeCount; i++) {
                        recipes.add(deserializeRecipe(dis));
                    }
                    villager.setRecipes(recipes);
                }
                case "WANDERING_TRADER" -> {
                    WanderingTrader trader = (WanderingTrader) entity;
                    trader.setHealth(dis.readDouble());
                    if (dis.readBoolean()) {
                        trader.customName(net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson().deserialize(dis.readUTF()));
                    }

                    int recipeCount = dis.readInt();
                    List<MerchantRecipe> recipes = new ArrayList<>();
                    for (int i = 0; i < recipeCount; i++) {
                        recipes.add(deserializeRecipe(dis));
                    }
                    trader.setRecipes(recipes);
                }
                case "ZOMBIE_VILLAGER" -> {
                    ZombieVillager zombieVillager = (ZombieVillager) entity;
                    zombieVillager.setVillagerProfession(professionFromKey(dis.readUTF()));
                    zombieVillager.setVillagerType(typeFromKey(dis.readUTF()));
                    zombieVillager.setHealth(dis.readDouble());
                    if (dis.readBoolean()) zombieVillager.setBaby(true);
                    if (dis.readBoolean()) {
                        zombieVillager.customName(net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson().deserialize(dis.readUTF()));
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void serializeRecipe(DataOutputStream dos, MerchantRecipe recipe) throws IOException {
        dos.writeUTF(serializeItemStack(recipe.getResult()));

        List<ItemStack> ingredients = recipe.getIngredients();
        dos.writeInt(ingredients.size());
        for (ItemStack ingredient : ingredients) {
            dos.writeUTF(serializeItemStack(ingredient));
        }

        dos.writeInt(recipe.getUses());
        dos.writeInt(recipe.getMaxUses());
        dos.writeBoolean(recipe.hasExperienceReward());
        dos.writeInt(recipe.getVillagerExperience());
        dos.writeFloat(recipe.getPriceMultiplier());
        dos.writeInt(recipe.getDemand());
        dos.writeInt(recipe.getSpecialPrice());
    }

    private static MerchantRecipe deserializeRecipe(DataInputStream dis) throws IOException {
        ItemStack result = deserializeItemStack(dis.readUTF());

        int ingredientCount = dis.readInt();
        List<ItemStack> ingredients = new ArrayList<>();
        for (int i = 0; i < ingredientCount; i++) {
            ingredients.add(deserializeItemStack(dis.readUTF()));
        }

        MerchantRecipe recipe = new MerchantRecipe(
                result, dis.readInt(), dis.readInt(), dis.readBoolean(),
                dis.readInt(), dis.readFloat(), dis.readInt(), dis.readInt()
        );
        recipe.setIngredients(ingredients);
        return recipe;
    }

    private static String serializeItemStack(ItemStack item) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (org.bukkit.util.io.BukkitObjectOutputStream boos = new org.bukkit.util.io.BukkitObjectOutputStream(baos)) {
                boos.writeObject(item);
            }
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static ItemStack deserializeItemStack(String base64) {
        try {
            byte[] bytes = Base64.getDecoder().decode(base64);
            try (org.bukkit.util.io.BukkitObjectInputStream bois = new org.bukkit.util.io.BukkitObjectInputStream(new ByteArrayInputStream(bytes))) {
                return (ItemStack) bois.readObject();
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static Component getDisplayName(Entity entity) {
        if (entity instanceof Villager) {
            return Component.text("Villager In A Bucket").decoration(TextDecoration.ITALIC, false);
        } else if (entity instanceof WanderingTrader) {
            return Component.text("Wandering Trader In A Bucket").decoration(TextDecoration.ITALIC, false);
        } else if (entity instanceof ZombieVillager) {
            return Component.text("Zombie Villager In A Bucket").decoration(TextDecoration.ITALIC, false);
        }
        return Component.text("Mob In A Bucket").decoration(TextDecoration.ITALIC, false);
    }

    private static List<Component> buildLore(Entity entity) {
        List<Component> lore = new ArrayList<>();

        if (entity instanceof Villager villager) {
            lore.add(Component.text("Level: " + villager.getVillagerLevel())
                    .color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, true));
            lore.add(Component.text("Region: " + formatEnum(getTypeKey(villager.getVillagerType())))
                    .color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, true));
            lore.add(Component.text("Profession: " + formatEnum(getProfessionKey(villager.getProfession())))
                    .color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, true));
            if (!villager.isAdult()) {
                lore.add(Component.text("Baby")
                        .color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, true));
            }
        } else if (entity instanceof ZombieVillager zombieVillager) {
            lore.add(Component.text("Region: " + formatEnum(getTypeKey(zombieVillager.getVillagerType())))
                    .color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, true));
            lore.add(Component.text("Profession: " + formatEnum(getProfessionKey(zombieVillager.getVillagerProfession())))
                    .color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, true));
            if (!zombieVillager.isAdult()) {
                lore.add(Component.text("Baby")
                        .color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, true));
            }
        }

        return lore;
    }

    private static int getCustomModelData(Entity entity) {
        if (entity instanceof Villager villager) {
            return VILLAGER_TYPE_MODEL_DATA.getOrDefault(getTypeKey(villager.getVillagerType()), 1);
        }
        if (entity instanceof WanderingTrader) return 6;
        if (entity instanceof ZombieVillager) return 7;
        return 0;
    }

    private static String formatEnum(String enumName) {
        if (enumName == null || enumName.isEmpty()) return enumName;
        String[] parts = enumName.toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return sb.toString();
    }
}
