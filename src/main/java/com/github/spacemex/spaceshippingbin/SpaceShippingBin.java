package com.github.spacemex.spaceshippingbin;

import com.github.spacemex.spaceshippingbin.blocks.BaseShippingBin;
import com.github.spacemex.spaceshippingbin.entities.block.BaseShippingBinEntity;
import com.github.spacemex.spaceshippingbin.items.CheckItem;
import com.github.spacemex.spaceshippingbin.items.CoinItem;
import com.github.spacemex.spaceshippingbin.menus.ShippingBinMenu;
import com.github.spacemex.spaceshippingbin.packet.ClientCurrencyData;
import com.github.spacemex.spaceshippingbin.packet.SyncCurrencyPacket;
import com.github.spacemex.spaceshippingbin.screens.ShippingBinScreen;
import com.github.spacemex.spaceshippingbin.shipping.ShippingRegistry;
import com.github.spacemex.spaceshippingbin.util.CoinParser;
import com.github.spacemex.spaceshippingbin.util.currency.Currency;
import com.github.spacemex.spaceshippingbin.util.currency.CurrencyFormatter;
import com.github.spacemex.spaceshippingbin.util.currency.CurrencyProvider;
import com.github.spacemex.spaceshippingbin.util.currency.ICurrency;
import com.github.spacemex.spaceshippingbin.packet.NetworkHandler;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@Mod(SpaceShippingBin.MODID)
public class SpaceShippingBin {

    public static double startingBalance = 0;
    public static final List<RegistryObject<Item>> coins = new ArrayList<>();

    public static final String MODID = "spaceshippingbin";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final Capability<ICurrency> BALANCE_CAP = CapabilityManager.get(new CapabilityToken<>() {});

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES,MODID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES,MODID);


    public static final RegistryObject<Block> SHIPPING_BIN = BLOCKS.register("shipping_bin", BaseShippingBin::new);

    public static final RegistryObject<Item> SHIPPING_BIN_ITEM = ITEMS.register("shipping_bin", ()->
            new BlockItem(SHIPPING_BIN.get(),new Item.Properties()));
    public static final RegistryObject<Item> CHECK = ITEMS.register("check",()->
            new CheckItem(new Item.Properties()));

    public static final RegistryObject<BlockEntityType<BaseShippingBinEntity>> SHIPPING_BIN_ENTITY = BLOCK_ENTITIES.register("shipping_bin",()->
            BlockEntityType.Builder.of(BaseShippingBinEntity::new,SHIPPING_BIN.get()).build(null));

    public static final RegistryObject<MenuType<ShippingBinMenu>> SHIPPING_BIN_MENU =
            MENUS.register("shipping_bin", ()-> IForgeMenuType.create((windowId, inv, data) -> {
                BlockPos pos = data.readBlockPos();
                Level level = inv.player.level();
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof BaseShippingBinEntity bin) {
                    return new ShippingBinMenu(windowId, inv, bin);
                }
                throw new IllegalStateException("No Container found at " + pos);
            }));

    private static RegistryObject<Item> registerCoin(String name, double value) {
        RegistryObject<Item> item = ITEMS.register(name, () -> new CoinItem( new Item.Properties(),value));
        coins.add(item);
        return item;
    }
    public SpaceShippingBin(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        Config.register(context);

        NetworkHandler.register();

        ITEMS.register(modEventBus);
        BLOCKS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        MENUS.register(modEventBus);

        MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, this::attachCapabilities);
        modEventBus.addListener(this::commonSetup);



        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        startingBalance = Config.STARTING_CURRENCY.get();

        event.enqueueWork(()-> ShippingRegistry.loadFromFile(FMLPaths.CONFIGDIR.get()));

        coins.forEach(coin -> {
            Supplier<Item> itemSupplier = coin.get().asItem().getDefaultInstance().getItemHolder();
            CoinParser.registerCoins(itemSupplier);
        });

    }

    private void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            CurrencyProvider provider = new CurrencyProvider();
            event.addCapability(ResourceLocation.fromNamespaceAndPath(MODID, "space_balance"), provider);
        }
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            MenuScreens.register(SHIPPING_BIN_MENU.get(), ShippingBinScreen::new);
        }
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class renderEvent{
        @SubscribeEvent
        public static void  onRenderOverlay(RenderGuiOverlayEvent.Post event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;
            if (!Config.ENABLE_VIRTUAL_ECONOMY.get() || !Config.ENABLE_HUD_OVERLAY.get()) return;

            GuiGraphics graphics = event.getGuiGraphics();

            double balance = ClientCurrencyData.getBalance();
            String balanceString = Config.CURRENCY_SYMBOL.get() + CurrencyFormatter.format(balance);
            int DisplayColorCode = Config.HUD_COLOR.get();
            int X = Config.HUD_OVERLAY_X.get();
            int Y = Config.HUD_OVERLAY_Y.get();

            graphics.drawString(mc.font,balanceString,X,Y, DisplayColorCode);

        }
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class JoinEvents {
        @SubscribeEvent
        public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
            Player player = event.getEntity();
            player.getCapability(SpaceShippingBin.BALANCE_CAP).ifPresent(c -> {
                if (!((Currency) c).isInitialized()) {
                    c.set(SpaceShippingBin.startingBalance);
                }

                if (player instanceof ServerPlayer serverPlayer) {
                    NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer),
                            new SyncCurrencyPacket(c.get()));
                }
            });
        }
    }
}
