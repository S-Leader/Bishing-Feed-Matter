package net.theawesomegem.fishingmadebetter;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.FishingHookRenderer;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
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
import net.theawesomegem.fishingmadebetter.compat.FishingEvolvedJeiPlugin;
import net.theawesomegem.fishingmadebetter.compat.LegacyFishConfigBootstrap;
import net.theawesomegem.fishingmadebetter.registry.*;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;

@Mod(Constants.MOD_ID)
public class FishingMadeBetterNeoForge {
    private static final String NETWORK_VERSION = "4";
    private static final String YACL_MOD_ID = "yet_another_config_lib_v3";
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Constants.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Constants.MOD_ID);
    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, Constants.MOD_ID);
    private static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Constants.MOD_ID);
    private static final DeferredHolder<Block, Block> BAIT_BOX = BLOCKS.registerBlock(ModBlocks.BAIT_BOX.path(), BaitBoxBlock::new);
    private final Path fishConfigDirectory;

    static {
        CREATIVE_MODE_TABS.register("main", () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0).title(Component.translatable("itemGroup.fishingmadebetter")).icon(() -> new ItemStack(ModItems.FISHING_ROD_WOOD.get())).displayItems((parameters, output) -> {
            ModItems.ITEMS.getEntries().forEach(item -> output.accept(item.get()));
        }).build());

        ModBlockEntities.BAIT_BOX = BLOCK_ENTITY_TYPES.register(ModBlocks.BAIT_BOX.path(), () -> new BlockEntityType<>(BaitBoxBlockEntity::new, BAIT_BOX.get()));

        ModRecipeSerializers.ROD_ATTACHMENT = RECIPE_SERIALIZERS.register(ModRecipeSerializers.ROD_ATTACHMENT_ID.getPath(), ModRecipeSerializers.ROD_ATTACHMENT);
        ModRecipeSerializers.BAITED_ROD = RECIPE_SERIALIZERS.register(ModRecipeSerializers.BAITED_ROD_ID.getPath(), ModRecipeSerializers.BAITED_ROD);
        ModRecipeSerializers.BAIT_BUCKET = RECIPE_SERIALIZERS.register(ModRecipeSerializers.BAIT_BUCKET_ID.getPath(), ModRecipeSerializers.BAIT_BUCKET);
        ModRecipeSerializers.FISH_BUCKET = RECIPE_SERIALIZERS.register(ModRecipeSerializers.FISH_BUCKET_ID.getPath(), ModRecipeSerializers.FISH_BUCKET);
        ModRecipeSerializers.FISH_SLICE = RECIPE_SERIALIZERS.register(ModRecipeSerializers.FISH_SLICE_ID.getPath(), ModRecipeSerializers.FISH_SLICE);
        ModRecipeSerializers.FISH_SCALE = RECIPE_SERIALIZERS.register(ModRecipeSerializers.FISH_SCALE_ID.getPath(), ModRecipeSerializers.FISH_SCALE);
    }

    public FishingMadeBetterNeoForge(IEventBus modBus, ModContainer container) {
        fishConfigDirectory = LegacyFishConfigBootstrap.installDefaults(FMLPaths.CONFIGDIR.get());
        FmbCommonConfig.register(container);
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
        modBus.addListener(FishingMadeBetterNeoForge::registerNetworkMessages);
        NeoForge.EVENT_BUS.addListener(this::addReloadListeners);
        if (FMLLoader.getCurrent().getDist() == Dist.CLIENT) {
            ClientEvents.registerConfigScreen(container);
        }
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> FishDataRegistry.reload(Map.of(), fishConfigDirectory));
    }

    private void addReloadListeners(AddServerReloadListenersEvent event) {
        event.addListener(FishDataReloadListener.ID, new FishDataReloadListener(fishConfigDirectory));
    }

    public static Item registeredItem(String path) {
        return ModItems.get(path);
    }

    @EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
    public static final class ClientEvents {
        private ClientEvents() {
        }

        private static void registerConfigScreen(ModContainer container) {
            container.registerExtensionPoint(IConfigScreenFactory.class, (modContainer, parent) -> openConfigScreen(parent));
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
            event.registerCategory(ReelingKeyMappings.CATEGORY);
            event.register(ReelingKeyMappings.REEL_IN);
            event.register(ReelingKeyMappings.REEL_OUT);
            event.register(ReelingKeyMappings.OPEN_CONFIG);
        }

        @SubscribeEvent
        public static void registerRodProperties(RegisterRangeSelectItemModelPropertyEvent event) {
            RodModelProperties.register(event);
        }

        private static Screen openConfigScreen(Screen parent) {
            if (!ModList.get().isLoaded(YACL_MOD_ID)) {
                Constants.LOG.warn("Install YetAnotherConfigLib to open the Kitchen Made Better config" + " screen.");
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
        public static void playerTick(PlayerTickEvent.Post event) {
            if (event.getEntity().level().isClientSide() || event.getEntity().tickCount % 5 != 0) {
                return;
            }

            long currentTime = event.getEntity().level().getGameTime();
            for (int i = 0; i < event.getEntity().getInventory().getContainerSize(); i++) {
                ItemStack stack = event.getEntity().getInventory().getItem(i);
                if (stack.isEmpty() || !FishStackUtil.isBetterFish(stack)) {
                    continue;
                }
                FishStackUtil.refreshInventoryState(stack, currentTime);
            }
        }

        @SubscribeEvent
        public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
            if (event.getEntity() instanceof ServerPlayer player) {
                sendServerConfigSnapshot(player);
            }
        }

        @SubscribeEvent
        public static void chunkLoad(ChunkEvent.Load event) {
            WhaleShipwreckSpawner.onChunkLoad(event);
        }

        @SubscribeEvent
        public static void levelTick(LevelTickEvent.Post event) {
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
            event.getDispatcher().register(Commands.literal("fmb_fish").then(Commands.argument("id", StringArgumentType.greedyString()).executes(context -> FishingEvolvedJeiPlugin.openFishRequirements(StringArgumentType.getString(context, "id")) ? 1 : 0)));
        }

        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void stripDuplicateAquacultureWeight(ItemTooltipEvent event) {
            if (!FishStackUtil.isBetterFish(event.getItemStack())) {
                return;
            }
            event.getToolTip().removeIf(line -> {
                if (line.getContents() instanceof TranslatableContents translated && translated.getKey().equals("aquaculture.fishWeight.weight"))
                    return true;
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
        public static void clientTick(ClientTickEvent.Post event) {
            {
                ReelingKeyMappings.clientTick(input -> ClientPacketDistributor.sendToServer(new ReelingInputMessage(input)));
                Minecraft minecraft = Minecraft.getInstance();
                while (ReelingKeyMappings.OPEN_CONFIG.consumeClick()) {
                    minecraft.setScreen(ClientEvents.openConfigScreen(minecraft.screen));
                }
            }
        }
    }

    public static void sendServerConfigUpdate(boolean whaleBreaksBlocks, boolean whaleDropsEnabled, int whaleSpawnChancePercent) {
        ClientPacketDistributor.sendToServer(new ServerConfigUpdateMessage(whaleBreaksBlocks, whaleDropsEnabled, whaleSpawnChancePercent));
    }

    private static boolean canEditServerConfig(ServerPlayer player) {
        return player != null && (player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER) || player.level().getServer() != null && player.level().getServer().isSingleplayerOwner(new NameAndId(player.getGameProfile())));
    }

    private static void sendServerConfigSnapshot(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new ServerConfigSnapshotMessage(FmbCommonConfig.whaleBreaksBlocks(), FmbCommonConfig.whaleDropsEnabled(), FmbCommonConfig.whaleSpawnChancePercent(), canEditServerConfig(player)));
    }

    private static void registerNetworkMessages(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar(NETWORK_VERSION);
        registrar.playToServer(ReelingInputMessage.TYPE, ReelingInputMessage.CODEC, (message, context) -> ReelingInputHandler.handle(context.player(), message.input));
        registrar.playToServer(ServerConfigUpdateMessage.TYPE, ServerConfigUpdateMessage.CODEC, ServerConfigUpdateMessage::handle);
        registrar.playToClient(ServerConfigSnapshotMessage.TYPE, ServerConfigSnapshotMessage.CODEC, (message, context) -> ServerConfigClientState.update(message.whaleBreaksBlocks, message.whaleDropsEnabled, message.whaleSpawnChancePercent, message.canEdit));
    }

    private record ReelingInputMessage(ReelingInput input) implements CustomPacketPayload {
        static final Type<ReelingInputMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "reeling_input"));
        static final StreamCodec<FriendlyByteBuf, ReelingInputMessage> CODEC = StreamCodec.of((buffer, message) -> buffer.writeEnum(message.input), buffer -> new ReelingInputMessage(buffer.readEnum(ReelingInput.class)));

        @Override
        public Type<ReelingInputMessage> type() {
            return TYPE;
        }
    }

    private record ServerConfigUpdateMessage(boolean whaleBreaksBlocks, boolean whaleDropsEnabled,
                                             int whaleSpawnChancePercent) implements CustomPacketPayload {
        static final Type<ServerConfigUpdateMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "config_update"));
        static final StreamCodec<FriendlyByteBuf, ServerConfigUpdateMessage> CODEC = StreamCodec.of((buffer, message) -> {
            buffer.writeBoolean(message.whaleBreaksBlocks);
            buffer.writeBoolean(message.whaleDropsEnabled);
            buffer.writeVarInt(message.whaleSpawnChancePercent);
        }, buffer -> new ServerConfigUpdateMessage(buffer.readBoolean(), buffer.readBoolean(), buffer.readVarInt()));

        @Override
        public Type<ServerConfigUpdateMessage> type() {
            return TYPE;
        }

        private static void handle(ServerConfigUpdateMessage message, IPayloadContext context) {
            if (!(context.player() instanceof ServerPlayer sender) || !canEditServerConfig(sender)) return;
            FmbCommonConfig.applyServerEdit(message.whaleBreaksBlocks, message.whaleDropsEnabled, message.whaleSpawnChancePercent);
            for (ServerPlayer player : sender.level().getServer().getPlayerList().getPlayers())
                sendServerConfigSnapshot(player);
        }
    }

    private record ServerConfigSnapshotMessage(boolean whaleBreaksBlocks, boolean whaleDropsEnabled,
                                               int whaleSpawnChancePercent,
                                               boolean canEdit) implements CustomPacketPayload {
        static final Type<ServerConfigSnapshotMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "config_snapshot"));
        static final StreamCodec<FriendlyByteBuf, ServerConfigSnapshotMessage> CODEC = StreamCodec.of((buffer, message) -> {
            buffer.writeBoolean(message.whaleBreaksBlocks);
            buffer.writeBoolean(message.whaleDropsEnabled);
            buffer.writeVarInt(message.whaleSpawnChancePercent);
            buffer.writeBoolean(message.canEdit);
        }, buffer -> new ServerConfigSnapshotMessage(buffer.readBoolean(), buffer.readBoolean(), buffer.readVarInt(), buffer.readBoolean()));

        @Override
        public Type<ServerConfigSnapshotMessage> type() {
            return TYPE;
        }
    }
}
