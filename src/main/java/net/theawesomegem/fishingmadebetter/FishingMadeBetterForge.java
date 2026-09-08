package net.theawesomegem.fishingmadebetter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.lang.reflect.Method;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.FishingHookRenderer;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.theawesomegem.fishingmadebetter.common.block.BaitBoxBlock;
import net.theawesomegem.fishingmadebetter.common.block.BaitBoxBlockEntity;
import net.theawesomegem.fishingmadebetter.client.FmbClientConfig;
import net.theawesomegem.fishingmadebetter.client.RodModelProperties;
import net.theawesomegem.fishingmadebetter.client.ReelingHudRenderer;
import net.theawesomegem.fishingmadebetter.client.ReelingKeyMappings;
import net.theawesomegem.fishingmadebetter.common.data.FishDataReloadListener;
import net.theawesomegem.fishingmadebetter.common.entity.LavaFishingHook;
import net.theawesomegem.fishingmadebetter.common.entity.VoidFishingHook;
import net.theawesomegem.fishingmadebetter.common.entity.WaterFishingHook;
import net.theawesomegem.fishingmadebetter.common.item.BaitBoxItem;
import net.theawesomegem.fishingmadebetter.common.network.ReelingInput;
import net.theawesomegem.fishingmadebetter.common.network.ReelingInputHandler;
import net.theawesomegem.fishingmadebetter.common.util.FishStackUtil;
import net.theawesomegem.fishingmadebetter.registry.ModBlockEntities;
import net.theawesomegem.fishingmadebetter.registry.ModBlocks;
import net.theawesomegem.fishingmadebetter.registry.ModEntities;
import net.theawesomegem.fishingmadebetter.registry.ModItems;
import net.theawesomegem.fishingmadebetter.registry.ModRecipeSerializers;
import net.theawesomegem.fishingmadebetter.compat.AquacultureCompat;
import net.theawesomegem.fishingmadebetter.compat.LegacyFishConfigBootstrap;

@Mod(Constants.MOD_ID)
public class FishingMadeBetterForge {
    private static final String NETWORK_VERSION = "1";
    private static final String YACL_MOD_ID = "yet_another_config_lib_v3";
    private static final SimpleChannel NETWORK = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Constants.MOD_ID, "main"),
            () -> NETWORK_VERSION,
            NETWORK_VERSION::equals,
            NETWORK_VERSION::equals
    );
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Constants.MOD_ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Constants.MOD_ID);
    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Constants.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Constants.MOD_ID);
    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Constants.MOD_ID);
    private static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Constants.MOD_ID);
    private static final RegistryObject<Block> BAIT_BOX = BLOCKS.register(ModBlocks.BAIT_BOX.path(), BaitBoxBlock::new);
    private static final Map<String, RegistryObject<Item>> REGISTERED_ITEMS = new LinkedHashMap<>();
    private final java.nio.file.Path fishConfigDirectory;

    static {
        REGISTERED_ITEMS.put(ModBlocks.BAIT_BOX.path(), ITEMS.register(
                ModBlocks.BAIT_BOX.path(),
                () -> new BaitBoxItem(BAIT_BOX.get(), new Item.Properties())
        ));

        ModItems.ITEMS.forEach(definition -> REGISTERED_ITEMS.put(
                definition.path(),
                ITEMS.register(definition.path(), () -> ModItems.create(definition.path()))
        ));

        CREATIVE_MODE_TABS.register("main", () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                .title(Component.translatable("itemGroup.fishingmadebetter"))
                .icon(() -> new ItemStack(REGISTERED_ITEMS.get("fishing_rod_wood").get()))
                .displayItems((parameters, output) -> REGISTERED_ITEMS.values().forEach(item -> output.accept(item.get())))
                .build());

        ModBlockEntities.BAIT_BOX = BLOCK_ENTITY_TYPES.register(
                ModBlocks.BAIT_BOX.path(),
                () -> BlockEntityType.Builder.of(BaitBoxBlockEntity::new, BAIT_BOX.get()).build(null)
        );

        ModEntities.WATER_FISHING_HOOK = ENTITY_TYPES.register(
                "water_fishing_hook",
                () -> EntityType.Builder.<WaterFishingHook>of(WaterFishingHook::new, MobCategory.MISC)
                        .noSave()
                        .noSummon()
                        .sized(0.25F, 0.25F)
                        .clientTrackingRange(4)
                        .updateInterval(5)
                        .build("water_fishing_hook")
        );
        ModEntities.LAVA_FISHING_HOOK = ENTITY_TYPES.register(
                "lava_fishing_hook",
                () -> EntityType.Builder.<LavaFishingHook>of(LavaFishingHook::new, MobCategory.MISC)
                        .noSave()
                        .noSummon()
                        .sized(0.25F, 0.25F)
                        .clientTrackingRange(4)
                        .updateInterval(5)
                        .build("lava_fishing_hook")
        );
        ModEntities.VOID_FISHING_HOOK = ENTITY_TYPES.register(
                "void_fishing_hook",
                () -> EntityType.Builder.<VoidFishingHook>of(VoidFishingHook::new, MobCategory.MISC)
                        .noSave()
                        .noSummon()
                        .sized(0.25F, 0.25F)
                        .clientTrackingRange(4)
                        .updateInterval(5)
                        .build("void_fishing_hook")
        );

        ModRecipeSerializers.ROD_ATTACHMENT = RECIPE_SERIALIZERS.register(
                ModRecipeSerializers.ROD_ATTACHMENT_ID.getPath(),
                ModRecipeSerializers.ROD_ATTACHMENT
        );
        ModRecipeSerializers.BAITED_ROD = RECIPE_SERIALIZERS.register(
                ModRecipeSerializers.BAITED_ROD_ID.getPath(),
                ModRecipeSerializers.BAITED_ROD
        );
        ModRecipeSerializers.BAIT_BUCKET = RECIPE_SERIALIZERS.register(
                ModRecipeSerializers.BAIT_BUCKET_ID.getPath(),
                ModRecipeSerializers.BAIT_BUCKET
        );
        ModRecipeSerializers.FISH_BUCKET = RECIPE_SERIALIZERS.register(
                ModRecipeSerializers.FISH_BUCKET_ID.getPath(),
                ModRecipeSerializers.FISH_BUCKET
        );
        ModRecipeSerializers.FISH_SLICE = RECIPE_SERIALIZERS.register(
                ModRecipeSerializers.FISH_SLICE_ID.getPath(),
                ModRecipeSerializers.FISH_SLICE
        );
        ModRecipeSerializers.FISH_SCALE = RECIPE_SERIALIZERS.register(
                ModRecipeSerializers.FISH_SCALE_ID.getPath(),
                ModRecipeSerializers.FISH_SCALE
        );
    }

    public FishingMadeBetterForge() {
        fishConfigDirectory = LegacyFishConfigBootstrap.installDefaults(FMLPaths.CONFIGDIR.get());
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        ENTITY_TYPES.register(modBus);
        BLOCK_ENTITY_TYPES.register(modBus);
        RECIPE_SERIALIZERS.register(modBus);
        CREATIVE_MODE_TABS.register(modBus);
        modBus.addListener(AquacultureCompat::hideCreativeItems);
        FishingMadeBetter.init();
        registerNetworkMessages();
        MinecraftForge.EVENT_BUS.addListener(this::addReloadListeners);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientEvents.registerConfigScreen();
        }
    }

    private void addReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new FishDataReloadListener(fishConfigDirectory));
    }

    @EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class ClientEvents {
        private ClientEvents() {
        }

        private static void registerConfigScreen() {
            ModLoadingContext.get().registerExtensionPoint(
                    ConfigScreenHandler.ConfigScreenFactory.class,
                    () -> new ConfigScreenHandler.ConfigScreenFactory((minecraft, parent) -> openConfigScreen(parent))
            );
        }

        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            FmbClientConfig.init(FMLPaths.CONFIGDIR.get());
            event.registerEntityRenderer(ModEntities.WATER_FISHING_HOOK.get(), FishingHookRenderer::new);
            event.registerEntityRenderer(ModEntities.LAVA_FISHING_HOOK.get(), FishingHookRenderer::new);
            event.registerEntityRenderer(ModEntities.VOID_FISHING_HOOK.get(), FishingHookRenderer::new);
        }

        @SubscribeEvent
        public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
            event.register(ReelingKeyMappings.REEL_IN);
            event.register(ReelingKeyMappings.REEL_OUT);
            event.register(ReelingKeyMappings.OPEN_CONFIG);
            registerRodModelProperties();
        }

        private static void registerRodModelProperties() {
            RodModelProperties.rodItems().forEach(item -> {
                registerItemProperty(item, RodModelProperties.REEL, (stack, level, entity, seed) -> RodModelProperties.reel(stack));
                registerItemProperty(item, RodModelProperties.BOBBER, (stack, level, entity, seed) -> RodModelProperties.bobber(stack));
                registerItemProperty(item, RodModelProperties.HOOK, (stack, level, entity, seed) -> RodModelProperties.hook(stack));
                registerItemProperty(item, RodModelProperties.CAST, (stack, level, entity, seed) -> RodModelProperties.cast(stack, entity));
            });
        }

        private static void registerItemProperty(Item item, ResourceLocation id, ClampedItemPropertyFunction function) {
            try {
                Method register = ItemProperties.class.getDeclaredMethod("register", Item.class, ResourceLocation.class, ClampedItemPropertyFunction.class);
                register.setAccessible(true);
                register.invoke(null, item, id, function);
            } catch (ReflectiveOperationException exception) {
                Constants.LOG.error("Failed to register rod item model property {}", id, exception);
            }
        }

        private static net.minecraft.client.gui.screens.Screen openConfigScreen(net.minecraft.client.gui.screens.Screen parent) {
            if (!ModList.get().isLoaded(YACL_MOD_ID)) {
                Constants.LOG.warn("Install YetAnotherConfigLib to open the Fishing Evolved config screen.");
                return parent;
            }
            return FishingMadeBetterConfigScreen.create(parent);
        }
    }

    @EventBusSubscriber(modid = Constants.MOD_ID)
    public static final class ForgeCommonEvents {
        private ForgeCommonEvents() {
        }

        @SubscribeEvent
        public static void playerTick(TickEvent.PlayerTickEvent event) {
            if (event.phase == TickEvent.Phase.END || event.player.level().isClientSide || event.player.tickCount % 5 != 0) {
                return;
            }

            long currentTime = event.player.level().getGameTime();
            for (int i = 0; i < event.player.getInventory().getContainerSize(); i++) {
                ItemStack stack = event.player.getInventory().getItem(i);
                if (stack.isEmpty() || !FishStackUtil.isBetterFish(stack)) {
                    continue;
                }
                FishStackUtil.refreshInventoryState(stack, currentTime);
            }
        }
    }

    @EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
    public static final class ForgeClientEvents {
        private ForgeClientEvents() {
        }

        @SubscribeEvent
        public static void renderHud(RenderGuiEvent.Post event) {
            ReelingHudRenderer.render(event.getGuiGraphics());
        }

        @SubscribeEvent
        public static void clientTick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                ReelingKeyMappings.clientTick(input -> NETWORK.sendToServer(new ReelingInputMessage(input)));
                Minecraft minecraft = Minecraft.getInstance();
                while (ReelingKeyMappings.OPEN_CONFIG.consumeClick()) {
                    minecraft.setScreen(ClientEvents.openConfigScreen(minecraft.screen));
                }
            }
        }
    }

    private static void registerNetworkMessages() {
        NETWORK.registerMessage(
                0,
                ReelingInputMessage.class,
                ReelingInputMessage::encode,
                ReelingInputMessage::decode,
                ReelingInputMessage::handle
        );
    }

    private record ReelingInputMessage(ReelingInput input) {
        private static void encode(ReelingInputMessage message, FriendlyByteBuf buffer) {
            buffer.writeEnum(message.input);
        }

        private static ReelingInputMessage decode(FriendlyByteBuf buffer) {
            return new ReelingInputMessage(buffer.readEnum(ReelingInput.class));
        }

        private static void handle(ReelingInputMessage message, java.util.function.Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> ReelingInputHandler.handle(context.getSender(), message.input));
            context.setPacketHandled(true);
        }
    }
}
