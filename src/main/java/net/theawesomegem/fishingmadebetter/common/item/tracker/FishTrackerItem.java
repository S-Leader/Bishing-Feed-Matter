package net.theawesomegem.fishingmadebetter.common.item.tracker;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.theawesomegem.fishingmadebetter.common.data.FishData;
import net.theawesomegem.fishingmadebetter.common.data.FishData.FishingLiquid;
import net.theawesomegem.fishingmadebetter.common.data.FishDataRegistry;
import net.theawesomegem.fishingmadebetter.common.data.FishPopulationSavedData;

public class FishTrackerItem extends Item {
    private final TrackingVision trackingVision;
    private final TrackingLiquid trackingLiquid;
    private final int maxDepth;

    public FishTrackerItem(TrackingVision trackingVision, TrackingLiquid trackingLiquid, int maxDepth) {
        super(new Item.Properties().stacksTo(1));
        this.trackingVision = trackingVision;
        this.trackingLiquid = trackingLiquid;
        this.maxDepth = maxDepth;
    }

    public TrackingVision getTrackingVision() {
        return trackingVision;
    }

    public TrackingLiquid getTrackingLiquid() {
        return trackingLiquid;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY);
        if (hit.getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(stack);
        }

        BlockPos pos = hit.getBlockPos();
        FluidState fluidState = level.getFluidState(pos);
        if (!canProbe(level, pos, fluidState, player)) {
            return InteractionResultHolder.fail(stack);
        }

        player.displayClientMessage(Component.translatable("notif.fishingmadebetter.fish_tracker.tracking_start"), false);
        if (level instanceof ServerLevel serverLevel) {
            reportPopulations(serverLevel, pos, player);
        }
        return InteractionResultHolder.success(stack);
    }

    private void reportPopulations(ServerLevel level, BlockPos pos, Player player) {
        boolean limitInfo = !player.isShiftKeyDown();
        boolean creative = player.isCreative();
        boolean fishFound = false;
        int underYCount = 0;
        int trackerQualityCount = 0;
        int hibernatingCount = 0;

        player.displayClientMessage(Component.literal("-----"), false);
        List<FishPopulationSavedData.PopulationData> populations = FishPopulationSavedData.get(level)
                .getOrCreatePopulations(level, level.getChunk(pos).getPos(), level.getGameTime())
                .stream()
                .toList();

        for (FishPopulationSavedData.PopulationData population : populations) {
            FishData fishData = FishDataRegistry.get(population.fishId());
            if (fishData == null) {
                continue;
            }

            if (creative) {
                player.displayClientMessage(Component.literal(String.format(
                        "%s %s in %s at Y%s-%s",
                        population.quantity(),
                        fishData.fishId(),
                        fishData.liquid(),
                        fishData.minYLevel(),
                        fishData.maxYLevel()
                )), false);
                player.displayClientMessage(Component.literal(String.format(
                        "MinLine %sm, Time %s, MaxLight %s, Rain %s, Thunder %s",
                        fishData.minDeepLevel(),
                        fishData.timeToFish(),
                        fishData.maxLightLevel(),
                        fishData.rainRequired(),
                        fishData.thunderRequired()
                )), false);
                continue;
            }

            if (!fishData.trackable() || !canTrackLiquid(fishData.liquid()) || fishData.minYLevel() > pos.getY()) {
                continue;
            }

            fishFound = true;
            if (fishData.maxYLevel() < pos.getY()) {
                underYCount++;
                continue;
            }
            if (fishData.minDeepLevel() > maxDepth || fishData.rarity() < trackingVision.minRarity()) {
                trackerQualityCount++;
                continue;
            }
            if (!isFeedingNow(level, pos, fishData)) {
                hibernatingCount++;
                continue;
            }

            MutableComponent detected = Component.translatable("notif.fishingmadebetter.fish_tracker.detected")
                    .append(" ")
                    .append(Component.literal(population.fishId()));
            if (!limitInfo) {
                detected = detected.append(", ")
                        .append(Component.literal(fishData.description()))
                        .append(" in ")
                        .append(Component.translatable(quantityKey(population.quantity())));
            }
            player.displayClientMessage(detected.append("."), false);
        }

        if (creative) {
            player.displayClientMessage(Component.literal("-----"), false);
            return;
        }
        if (!fishFound) {
            player.displayClientMessage(Component.translatable("notif.fishingmadebetter.fish_tracker.found_none"), false);
            player.displayClientMessage(Component.literal("-----"), false);
            return;
        }

        if (underYCount > 0) {
            player.displayClientMessage(Component.translatable("notif.fishingmadebetter.fish_tracker.found_under_y")
                    .append(": " + underYCount + "."), false);
        }
        if (trackerQualityCount > 0) {
            player.displayClientMessage(Component.translatable("notif.fishingmadebetter.fish_tracker.found_outside_quality")
                    .append(": " + trackerQualityCount + "."), false);
        }
        if (hibernatingCount > 0) {
            player.displayClientMessage(Component.translatable("notif.fishingmadebetter.fish_tracker.found_hibernating")
                    .append(": " + hibernatingCount + "."), false);
        }
        player.displayClientMessage(Component.literal("-----"), false);
    }

    private static String quantityKey(int quantity) {
        if (quantity > 50) {
            return "notif.fishingmadebetter.fish_tracker.quantity_immense";
        }
        if (quantity > 40) {
            return "notif.fishingmadebetter.fish_tracker.quantity_abundant";
        }
        if (quantity > 30) {
            return "notif.fishingmadebetter.fish_tracker.quantity_ample";
        }
        if (quantity > 20) {
            return "notif.fishingmadebetter.fish_tracker.quantity_substantial";
        }
        if (quantity > 10) {
            return "notif.fishingmadebetter.fish_tracker.quantity_numerous";
        }
        if (quantity > 3) {
            return "notif.fishingmadebetter.fish_tracker.quantity_light";
        }
        if (quantity > 1) {
            return "notif.fishingmadebetter.fish_tracker.quantity_sparse";
        }
        return "notif.fishingmadebetter.fish_tracker.quantity_meager";
    }

    private boolean canTrackLiquid(FishingLiquid fishLiquid) {
        return fishLiquid == FishingLiquid.ANY
                || (fishLiquid == FishingLiquid.WATER && trackingLiquid == TrackingLiquid.WATER)
                || (fishLiquid == FishingLiquid.LAVA && trackingLiquid == TrackingLiquid.LAVA)
                || (fishLiquid == FishingLiquid.VOID && trackingLiquid == TrackingLiquid.VOID);
    }

    private static boolean isFeedingNow(ServerLevel level, BlockPos pos, FishData fishData) {
        long dayTime = level.getDayTime() % 24000L;
        if (fishData.timeToFish() == FishData.TimeToFish.DAY && dayTime >= 12000L) {
            return false;
        }
        if (fishData.timeToFish() == FishData.TimeToFish.NIGHT && dayTime < 12000L) {
            return false;
        }
        if (level.getMaxLocalRawBrightness(pos) > fishData.maxLightLevel()) {
            return false;
        }
        if (fishData.rainRequired() && !level.isRaining()) {
            return false;
        }
        if (fishData.thunderRequired() && !level.isThundering()) {
            return false;
        }
        if (!isDimensionValid(level, fishData)) {
            return false;
        }
        return isBiomeValid(level, pos, fishData);
    }

    private static boolean isDimensionValid(ServerLevel level, FishData fishData) {
        if (fishData.dimensionList().isEmpty()) {
            return true;
        }
        boolean contains = fishData.dimensionList().stream().anyMatch(entry -> FishData.matchesDimension(level, entry));
        return fishData.dimensionListBlacklist() != contains;
    }

    private static boolean isBiomeValid(ServerLevel level, BlockPos pos, FishData fishData) {
        if (fishData.biomeList().isEmpty()) {
            return true;
        }
        boolean contains = fishData.biomeList().stream().anyMatch(entry -> FishData.matchesBiome(level.getBiome(pos), entry));
        return fishData.biomeListBlacklist() != contains;
    }

    private boolean canProbe(Level level, BlockPos pos, FluidState fluidState, Player player) {
        if (trackingLiquid == TrackingLiquid.VOID) {
            if (pos.getY() > level.getMinBuildHeight() + 3) {
                player.displayClientMessage(Component.translatable("notif.fishingmadebetter.fish_tracker.only_void"), false);
                return false;
            }
            return true;
        }

        Fluid expectedFluid = trackingLiquid == TrackingLiquid.LAVA ? Fluids.LAVA : Fluids.WATER;
        if (!fluidState.is(expectedFluid)) {
            player.displayClientMessage(Component.translatable(trackingLiquid == TrackingLiquid.LAVA
                    ? "notif.fishingmadebetter.fish_tracker.only_lava"
                    : "notif.fishingmadebetter.fish_tracker.only_water"), false);
            return false;
        }

        if (countNearbyFluid(level, pos, expectedFluid) < 25) {
            player.displayClientMessage(Component.translatable("notif.fishingmadebetter.fish_tracker.too_shallow"), false);
            return false;
        }

        return true;
    }

    private static int countNearbyFluid(Level level, BlockPos origin, Fluid fluid) {
        int count = 0;
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-2, -3, -2), origin.offset(2, 0, 2))) {
            if (level.getFluidState(pos).is(fluid)) {
                count++;
                if (count >= 25) {
                    return count;
                }
            }
        }
        return count;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.fishingmadebetter.fish_tracker.tooltip.max_depth")
                .append(": " + maxDepth)
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable(trackingVision.tooltipKey()).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable(trackingLiquid.tooltipKey()).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.fishingmadebetter.fish_tracker.tooltip.right_click").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.fishingmadebetter.fish_tracker.tooltip.info.1")
                .append(" Shift ")
                .append(Component.translatable("item.fishingmadebetter.fish_tracker.tooltip.info.2"))
                .withStyle(ChatFormatting.GRAY));
    }

    public enum TrackingVision {
        BAD("item.fishingmadebetter.fish_tracker.tooltip.vision_bad", 40),
        NORMAL("item.fishingmadebetter.fish_tracker.tooltip.vision_normal", 20),
        BEST("item.fishingmadebetter.fish_tracker.tooltip.vision_best", 0);

        private final String tooltipKey;
        private final int minRarity;

        TrackingVision(String tooltipKey, int minRarity) {
            this.tooltipKey = tooltipKey;
            this.minRarity = minRarity;
        }

        public String tooltipKey() {
            return tooltipKey;
        }

        public int minRarity() {
            return minRarity;
        }
    }

    public enum TrackingLiquid {
        WATER("item.fishingmadebetter.fish_tracker.tooltip.probe_water"),
        LAVA("item.fishingmadebetter.fish_tracker.tooltip.probe_lava"),
        VOID("item.fishingmadebetter.fish_tracker.tooltip.probe_void");

        private final String tooltipKey;

        TrackingLiquid(String tooltipKey) {
            this.tooltipKey = tooltipKey;
        }

        public String tooltipKey() {
            return tooltipKey;
        }
    }
}
