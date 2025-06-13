package com.github.spacemex.spaceshippingbin;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod.EventBusSubscriber(modid = SpaceShippingBin.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.BooleanValue ENABLE_VIRTUAL_ECONOMY;
    public static final ForgeConfigSpec.BooleanValue ENABLE_CHECK_COMMAND;
    public static final ForgeConfigSpec.BooleanValue ENABLE_BALANCE_COMMAND;
    public static final ForgeConfigSpec.BooleanValue ENABLE_BALANCE_OTHERS_COMMAND;
    public static final ForgeConfigSpec.ConfigValue<Integer> STARTING_CURRENCY;
    public static final ForgeConfigSpec.ConfigValue<String> CURRENCY_SYMBOL;

    public static final ForgeConfigSpec.BooleanValue ENABLE_HUD_OVERLAY;
    public static final ForgeConfigSpec.ConfigValue<Integer> HUD_OVERLAY_X;
    public static final ForgeConfigSpec.ConfigValue<Integer> HUD_OVERLAY_Y;
    public static final ForgeConfigSpec.ConfigValue<Integer> HUD_COLOR;
    static final ForgeConfigSpec SPEC;

    static {
        BUILDER.push("Economy Config");
        CURRENCY_SYMBOL = BUILDER.comment("Currency Symbol").define("Currency Symbol", "$");

        BUILDER.push("Virtual Economy");
        BUILDER.comment("Virtual Economy Allows you to use the economy without a physical currency, Think `EssentialsX` Plugin");
        BUILDER.comment("Disabling Virtual Economy will disable all Virtual Economy related features such as Checks, Right-Click Deposit Of Any Coin/Check,All Commands, And HUD Overlay");
        ENABLE_VIRTUAL_ECONOMY = BUILDER.comment("Enable Virtual Economy").define("Enable Virtual Economy", true);
        STARTING_CURRENCY = BUILDER.comment("Starting Virtual Currency - The Amount The Player Receives On First Join").define("Starting Virtual Currency", 150);
        ENABLE_CHECK_COMMAND = BUILDER.comment("Enable Check Command - Allows For Players To Withdraw Virtual Currency As A Check").define("Enable Check Command", true);
        ENABLE_BALANCE_COMMAND = BUILDER.comment("Enable Balance Command - Allows For Players To View Their Virtual Currency Balance").define("Enable Balance Command", true);
        ENABLE_BALANCE_OTHERS_COMMAND = BUILDER.comment("Enable Balance Others Command - Allows For Players To View Other Players Virtual Currency Balance").define("Enable Balance Others Command", true);
        BUILDER.pop();

        BUILDER.push("UI Config");
        ENABLE_HUD_OVERLAY = BUILDER.comment("Enable HUD Overlay").define("Enable HUD Overlay", true);
        HUD_OVERLAY_X = BUILDER.comment("HUD Overlay X Position").define("HUD Overlay X Position", 10);
        HUD_OVERLAY_Y = BUILDER.comment("HUD Overlay Y Position").define("HUD Overlay Y Position", 10);
        HUD_COLOR = BUILDER.comment("Overlay Text Color (ARGB)").define("Overlay Text Color", 0xFFFFFF);
        BUILDER.pop();

        BUILDER.pop();

        SPEC = BUILDER.build();
    }


    public static void register(FMLJavaModLoadingContext context) {
        context.registerConfig(ModConfig.Type.COMMON, SPEC, "ShippingBin/ShippingBinConfig.toml");
    }

}
