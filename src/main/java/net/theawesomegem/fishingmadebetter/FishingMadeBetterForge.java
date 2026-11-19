package net.theawesomegem.fishingmadebetter;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.FishingHookRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.theawesomegem.fishingmadebetter.client.*;
import net.theawesomegem.fishingmadebetter.client.model.WhaleModel;
import net.theawesomegem.fishingmadebetter.client.renderer.WhaleRenderer;
import net.theawesomegem.fishingmadebetter.common.block.BaitBoxBlock;
import net.theawesomegem.fishingmadebetter.common.block.BaitBoxBlockEntity;
import net.theawesomegem.fishingmadebetter.common.config.FmbCommonConfig;
import net.theawesomegem.fishingmadebetter.common.data.FishDataRegistry;
import net.theawesomegem.fishingmadebetter.common.data.FishDataReloadListener;
import net.theawesomegem.fishingmadebetter.common.network.ReelingInput;
import net.theawesomegem.fishingmadebetter.common.network.ReelingInputHandler;
import net.theawesomegem.fishingmadebetter.common.util.FishStackUtil;
import net.theawesomegem.fishingmadebetter.common.world.WhaleShipwreckSpawner;
import net.theawesomegem.fishingmadebetter.compat.AquacultureCompat;
import net.theawesomegem.fishingmadebetter.compat.LegacyFishConfigBootstrap;
import net.theawesomegem.fishingmadebetter.registry.*;

import java.util.Locale;
import java.util.Map;

@Mod(Constants.MOD_ID)
public class FishingMadeBetterForge {
    private static final String NETWORK_VERSION = "4";
    private static final String YACL_MOD_ID = "yet_another_config_lib_v3";
    private static final SimpleChannel NETWORK = NetworkRegistry.newSimpleChannel(new ResourceLocation(Constants.MOD_ID, "main"), () -> NETWORK_VERSION, NETWORK_VERSION::equals, NETWORK_VERSION::equals);
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Constants.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Constants.MOD_ID);
    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Constants.MOD_ID);
    private static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Constants.MOD_ID);
    private static final RegistryObject<Block> BAIT_BOX = BLOCKS.register(ModBlocks.BAIT_BOX.path(), BaitBoxBlock::new);
    private final java.nio.file.Path fishConfigDirectory;

    static {
        CREATIVE_MODE_TABS.register("main", () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0).title(Component.translatable("itemGroup.fishingmadebetter")).icon(() -> new ItemStack(ModItems.FISHING_ROD_WOOD.get())).displayItems((parameters, output) -> {
            ModItems.ITEMS.getEntries().forEach(item -> output.accept(item.get()));
        }).build());

        ModBlockEntities.BAIT_BOX = BLOCK_ENTITY_TYPES.register(ModBlocks.BAIT_BOX.path(), () -> BlockEntityType.Builder.of(BaitBoxBlockEntity::new, BAIT_BOX.get()).build(null));

        ModRecipeSerializers.ROD_ATTACHMENT = RECIPE_SERIALIZERS.register(ModRecipeSerializers.ROD_ATTACHMENT_ID.getPath(), ModRecipeSerializers.ROD_ATTACHMENT);
        ModRecipeSerializers.BAITED_ROD = RECIPE_SERIALIZERS.register(ModRecipeSerializers.BAITED_ROD_ID.getPath(), ModRecipeSerializers.BAITED_ROD);
        ModRecipeSerializers.BAIT_BUCKET = RECIPE_SERIALIZERS.register(ModRecipeSerializers.BAIT_BUCKET_ID.getPath(), ModRecipeSerializers.BAIT_BUCKET);
        ModRecipeSerializers.FISH_BUCKET = RECIPE_SERIALIZERS.register(ModRecipeSerializers.FISH_BUCKET_ID.getPath(), ModRecipeSerializers.FISH_BUCKET);
        ModRecipeSerializers.FISH_SLICE = RECIPE_SERIALIZERS.register(ModRecipeSerializers.FISH_SLICE_ID.getPath(), ModRecipeSerializers.FISH_SLICE);
        ModRecipeSerializers.FISH_SCALE = RECIPE_SERIALIZERS.register(ModRecipeSerializers.FISH_SCALE_ID.getPath(), ModRecipeSerializers.FISH_SCALE);
    }

    public FishingMadeBetterForge() {
        fishConfigDirectory = LegacyFishConfigBootstrap.installDefaults(FMLPaths.CONFIGDIR.get());
        FmbCommonConfig.register();
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        BLOCKS.register(modBus);
        ModItems.register(modBus);
        ModEntities.register(modBus);
        BLOCK_ENTITY_TYPES.register(modBus);
        RECIPE_SERIALIZERS.register(modBus);
        ModSounds.register(modBus);
        CREATIVE_MODE_TABS.register(modBus);
        ModLootEntries.LOOT_ENTRIES.register(modBus);
        ModEffects.register(modBus);
        ModParticles.register(modBus);
        modBus.addListener(AquacultureCompat::hideCreativeItems);
        modBus.addListener(this::commonSetup);
        FishDataRegistry.init();
        registerNetworkMessages();
        MinecraftForge.EVENT_BUS.addListener(this::addReloadListeners);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientEvents.registerConfigScreen();
        }
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> FishDataRegistry.reload(Map.of(), fishConfigDirectory));
    }

    private void addReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new FishDataReloadListener(fishConfigDirectory));
    }

    public static Item registeredItem(String path) {
        return ModItems.get(path);
    }

    @EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class ClientEvents {
        private ClientEvents() {
        }

        private static void registerConfigScreen() {
            ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory((minecraft, parent) -> openConfigScreen(parent)));
        }

        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            FmbClientConfig.init(FMLPaths.CONFIGDIR.get());
            event.registerEntityRenderer(ModEntities.WATER_FISHING_HOOK.get(), FishingHookRenderer::new);
            event.registerEntityRenderer(ModEntities.LAVA_FISHING_HOOK.get(), FishingHookRenderer::new);
            event.registerEntityRenderer(ModEntities.VOID_FISHING_HOOK.get(), FishingHookRenderer::new);
            event.registerEntityRenderer(ModEntities.WHALE.get(), WhaleRenderer::new);
        }

        @SubscribeEvent
        public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
            event.registerLayerDefinition(WhaleModel.LAYER_LOCATION, WhaleModel::createBodyLayer);
        }

        @SubscribeEvent
        public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
            event.register(ReelingKeyMappings.REEL_IN);
            event.register(ReelingKeyMappings.REEL_OUT);
            event.register(ReelingKeyMappings.OPEN_CONFIG);
        }

        @SubscribeEvent
        public static void clientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(ClientEvents::registerRodModelProperties);
        }

        private static void registerRodModelProperties() {
            RodModelProperties.rodItems().forEach(item -> {
                registerItemProperty(item, RodModelProperties.REEL, (stack, level, entity, seed) -> RodModelProperties.reel(stack));
                registerItemProperty(item, RodModelProperties.BOBBER, (stack, level, entity, seed) -> RodModelProperties.bobber(stack));
                registerItemProperty(item, RodModelProperties.HOOK, (stack, level, entity, seed) -> RodModelProperties.hook(stack));
                registerItemProperty(item, RodModelProperties.CAST, (stack, level, entity, seed) -> RodModelProperties.cast(stack, entity));
            });
        }

        private static void registerItemProperty(Item item, ResourceLocation id, ItemPropertyFunction function) {
            ItemProperties.register(item, id, function);
        }

        private static Screen openConfigScreen(Screen parent) {
            if (!ModList.get().isLoaded(YACL_MOD_ID)) {
                Constants.LOG.warn("Install YetAnotherConfigLib to open the Kitchen Made Better config screen.");
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

        @SubscribeEvent
        public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
            if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
                sendServerConfigSnapshot(player);
            }
        }

        @SubscribeEvent
        public static void chunkLoad(ChunkEvent.Load event) {
            WhaleShipwreckSpawner.onChunkLoad(event);
        }

        @SubscribeEvent
        public static void levelTick(TickEvent.LevelTickEvent event) {
            WhaleShipwreckSpawner.onLevelTick(event);
        }
    }

    @EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
    public static final class ForgeClientEvents {
        private ForgeClientEvents() {
        }

        @SubscribeEvent
        public static void registerFishRequirementsCommand(RegisterClientCommandsEvent event) {
            if (!ModList.get().isLoaded("jei")) {
                return;
            }
            event.getDispatcher().register(Commands.literal("fmb_fish").then(Commands.argument("id", StringArgumentType.greedyString()).executes(context -> net.theawesomegem.fishingmadebetter.compat.FishingEvolvedJeiPlugin.openFishRequirements(StringArgumentType.getString(context, "id")) ? 1 : 0)));
        }

        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void stripDuplicateAquacultureWeight(ItemTooltipEvent event) {
            if (!FishStackUtil.isBetterFish(event.getItemStack())) {
                return;
            }
            // The fish already has its original, unitless Weight lore line.
            // Aquaculture's lowercase fishWeight compatibility tag can add a
            // duplicate line in imperial units; retain that tag for integrations.
            event.getToolTip().removeIf(line -> {
                String value = line.getString().toLowerCase(Locale.ROOT);
                return value.matches(".*\\b(?:lbs?|pounds?)\\b.*") && (value.contains("weight") || value.contains("重量"));
            });
        }

        @SubscribeEvent
        public static void clientLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
            ServerConfigClientState.clear();
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

    public static void sendServerConfigUpdate(boolean whaleBreaksBlocks, boolean whaleDropsEnabled, int whaleSpawnChancePercent) {
        NETWORK.sendToServer(new ServerConfigUpdateMessage(whaleBreaksBlocks, whaleDropsEnabled, whaleSpawnChancePercent));
    }

    private static boolean canEditServerConfig(ServerPlayer player) {
        return player != null && (player.hasPermissions(2) || player.getServer() != null && player.getServer().isSingleplayerOwner(player.getGameProfile()));
    }

    private static void sendServerConfigSnapshot(ServerPlayer player) {
        NETWORK.send(PacketDistributor.PLAYER.with(() -> player), new ServerConfigSnapshotMessage(FmbCommonConfig.whaleBreaksBlocks(), FmbCommonConfig.whaleDropsEnabled(), FmbCommonConfig.whaleSpawnChancePercent(), canEditServerConfig(player)));
    }

    private static void registerNetworkMessages() {
        NETWORK.registerMessage(0, ReelingInputMessage.class, ReelingInputMessage::encode, ReelingInputMessage::decode, ReelingInputMessage::handle);
        NETWORK.registerMessage(1, ServerConfigUpdateMessage.class, ServerConfigUpdateMessage::encode, ServerConfigUpdateMessage::decode, ServerConfigUpdateMessage::handle);
        NETWORK.registerMessage(2, ServerConfigSnapshotMessage.class, ServerConfigSnapshotMessage::encode, ServerConfigSnapshotMessage::decode, ServerConfigSnapshotMessage::handle);
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

    private record ServerConfigUpdateMessage(boolean whaleBreaksBlocks, boolean whaleDropsEnabled,
                                             int whaleSpawnChancePercent) {
        private static void encode(ServerConfigUpdateMessage message, FriendlyByteBuf buffer) {
            buffer.writeBoolean(message.whaleBreaksBlocks);
            buffer.writeBoolean(message.whaleDropsEnabled);
            buffer.writeVarInt(message.whaleSpawnChancePercent);
        }

        private static ServerConfigUpdateMessage decode(FriendlyByteBuf buffer) {
            return new ServerConfigUpdateMessage(buffer.readBoolean(), buffer.readBoolean(), buffer.readVarInt());
        }

        private static void handle(ServerConfigUpdateMessage message, java.util.function.Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> {
                ServerPlayer sender = context.getSender();
                if (!canEditServerConfig(sender)) {
                    Constants.LOG.warn("Player {} tried to edit Kitchen Made Better server config without permission", sender == null ? "<unknown>" : sender.getGameProfile().getName());
                    return;
                }

                FmbCommonConfig.applyServerEdit(message.whaleBreaksBlocks, message.whaleDropsEnabled, message.whaleSpawnChancePercent);
                if (sender != null && sender.getServer() != null) {
                    for (ServerPlayer player : sender.getServer().getPlayerList().getPlayers()) {
                        sendServerConfigSnapshot(player);
                    }
                }
            });
            context.setPacketHandled(true);
        }
    }

    private record ServerConfigSnapshotMessage(boolean whaleBreaksBlocks, boolean whaleDropsEnabled,
                                               int whaleSpawnChancePercent, boolean canEdit) {
        private static void encode(ServerConfigSnapshotMessage message, FriendlyByteBuf buffer) {
            buffer.writeBoolean(message.whaleBreaksBlocks);
            buffer.writeBoolean(message.whaleDropsEnabled);
            buffer.writeVarInt(message.whaleSpawnChancePercent);
            buffer.writeBoolean(message.canEdit);
        }

        private static ServerConfigSnapshotMessage decode(FriendlyByteBuf buffer) {
            return new ServerConfigSnapshotMessage(buffer.readBoolean(), buffer.readBoolean(), buffer.readVarInt(), buffer.readBoolean());
        }

        private static void handle(ServerConfigSnapshotMessage message, java.util.function.Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ServerConfigClientState.update(message.whaleBreaksBlocks, message.whaleDropsEnabled, message.whaleSpawnChancePercent, message.canEdit)));
            context.setPacketHandled(true);
        }
    }

}
