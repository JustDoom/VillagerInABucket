package com.imjustdoom.villagerinabucket.forge.configuration;

import net.minecraftforge.common.ForgeConfigSpec;

public class AxifierConfig {

    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Integer> LIMIT;

    static {
        BUILDER.comment("Config for mod");
        BUILDER.comment("Shears = Scissors");

        LIMIT = BUILDER.comment("Chance to take damage while sprinting. Chance is between 0.0 and 1.0, 1.0 is 100% chance and 0.0 is 0% chance. Default is -1 (Unlimited)").define("limit", -1);
        SPEC = BUILDER.build();
    }
}
