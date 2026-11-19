package net.theawesomegem.fishingmadebetter.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.theawesomegem.fishingmadebetter.common.data.FishData;
import net.theawesomegem.fishingmadebetter.common.data.FishPopulationSavedData;
import net.theawesomegem.fishingmadebetter.common.util.BaitUtil;
import net.theawesomegem.fishingmadebetter.common.util.PlayerMessages;
import net.theawesomegem.fishingmadebetter.registry.ModBlockEntities;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BaitBoxBlockEntity extends BlockEntity {
    private static final int SLOT_COUNT = 8;
    private static final long BAIT_UPDATE_INTERVAL_TICKS = 5L * 60L * 20L;
    private final NonNullList<ItemStack> inventory = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private long nextBaitUpdateTime;

    public BaitBoxBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BAIT_BOX.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BaitBoxBlockEntity baitBox) {
        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        long gameTime = level.getGameTime();
        if (baitBox.nextBaitUpdateTime == 0) {
            baitBox.nextBaitUpdateTime = gameTime + BAIT_UPDATE_INTERVAL_TICKS;
            baitBox.setChanged();
            return;
        }

        if (gameTime < baitBox.nextBaitUpdateTime) {
            return;
        }

        baitBox.nextBaitUpdateTime = gameTime + BAIT_UPDATE_INTERVAL_TICKS;
        FishPopulationSavedData populationData = FishPopulationSavedData.get(serverLevel);
        populationData.generateInitialPopulations(serverLevel, level.getChunk(pos).getPos(), gameTime);
        boolean reproduced = populationData.tickReproduction(level.getChunk(pos).getPos(), gameTime);
        boolean fed = baitBox.consumeBaitForPopulation(populationData, serverLevel, pos);
        if (reproduced || fed) {
            baitBox.setChanged();
        }
    }

    public void handleRightClick(Player player) {
        ItemStack held = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (held.isEmpty()) {
            sendInventorySummary(player);
            return;
        }

        if (!BaitUtil.isValidBait(held)) {
            PlayerMessages.send(player, Component.translatable("notif.fishingmadebetter.baitbox.bait_not_valid"), false);
            return;
        }

        ItemStack remaining = insertBait(held.copy());
        if (remaining.getCount() == held.getCount()) {
            PlayerMessages.send(player, Component.translatable("notif.fishingmadebetter.baitbox.full"), false);
            return;
        }

        player.setItemInHand(InteractionHand.MAIN_HAND, remaining);
        setChanged();
    }

    public NonNullList<ItemStack> getDrops() {
        NonNullList<ItemStack> drops = NonNullList.create();
        for (ItemStack stack : inventory) {
            if (!stack.isEmpty()) {
                drops.add(stack.copy());
            }
        }
        return drops;
    }

    private ItemStack insertBait(ItemStack stack) {
        for (int i = 0; i < inventory.size() && !stack.isEmpty(); i++) {
            ItemStack existing = inventory.get(i);
            if (existing.isEmpty() || !ItemStack.isSameItemSameComponents(existing, stack)) {
                continue;
            }

            int move = Math.min(stack.getCount(), existing.getMaxStackSize() - existing.getCount());
            if (move > 0) {
                existing.grow(move);
                stack.shrink(move);
            }
        }

        for (int i = 0; i < inventory.size() && !stack.isEmpty(); i++) {
            if (!inventory.get(i).isEmpty()) {
                continue;
            }

            ItemStack inserted = stack.copy();
            inserted.setCount(Math.min(stack.getCount(), stack.getMaxStackSize()));
            inventory.set(i, inserted);
            stack.shrink(inserted.getCount());
        }

        return stack;
    }

    private boolean consumeBaitForPopulation(FishPopulationSavedData populationData, ServerLevel level, BlockPos pos) {
        if (isEmpty()) {
            return false;
        }

        return populationData.feedHungryPopulations(level.getChunk(pos).getPos(), level.getGameTime(), fishData -> consumeBaitForFish(requiredBaitForFish(fishData.minWeight(), fishData.maxWeight()), fishData));
    }

    private int consumeBaitForFish(int requiredBait, FishData fishData) {
        int available = 0;
        for (ItemStack stack : inventory) {
            if (!stack.isEmpty() && BaitUtil.isValidBaitForFish(stack, fishData)) {
                available += stack.getCount();
            }
            if (available >= requiredBait) {
                break;
            }
        }

        if (available < requiredBait) {
            return 0;
        }

        int remaining = requiredBait;
        for (int i = 0; i < inventory.size() && remaining > 0; i++) {
            ItemStack stack = inventory.get(i);
            if (stack.isEmpty() || !BaitUtil.isValidBaitForFish(stack, fishData)) {
                continue;
            }

            int consumed = Math.min(remaining, stack.getCount());
            stack.shrink(consumed);
            remaining -= consumed;
            if (stack.isEmpty()) {
                inventory.set(i, ItemStack.EMPTY);
            }
        }
        return requiredBait;
    }

    private static int requiredBaitForFish(int minWeight, int maxWeight) {
        int averageWeight = (minWeight + maxWeight) / 2;
        return Math.min(16, Math.max(1, averageWeight / 100));
    }

    private boolean isEmpty() {
        return inventory.stream().allMatch(ItemStack::isEmpty);
    }

    private void sendInventorySummary(Player player) {
        Map<String, Integer> baitCounts = new LinkedHashMap<>();
        for (ItemStack stack : inventory) {
            if (!stack.isEmpty()) {
                baitCounts.merge(stack.getHoverName().getString(), stack.getCount(), Integer::sum);
            }
        }

        if (baitCounts.isEmpty()) {
            PlayerMessages.send(player, Component.translatable("notif.fishingmadebetter.baitbox.empty"), false);
            return;
        }
        baitCounts.forEach((name, count) -> PlayerMessages.send(player, Component.literal(name + ": " + count), false));
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level instanceof ServerLevel serverLevel)
            for (ItemStack stack : getDrops()) Block.popResource(serverLevel, pos, stack);
        super.preRemoveSideEffects(pos, state);
    }

    @Override
    protected void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        tag.putLong("BaitUpdateTime", nextBaitUpdateTime);
        tag.store("Items", ItemStack.OPTIONAL_CODEC.listOf(), inventory);
    }

    @Override
    protected void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        nextBaitUpdateTime = tag.getLongOr("BaitUpdateTime", 0L);
        inventory.replaceAll(ignored -> ItemStack.EMPTY);
        var items = tag.read("Items", ItemStack.OPTIONAL_CODEC.listOf()).orElseGet(List::of);
        for (int i = 0; i < Math.min(items.size(), inventory.size()); i++)
            inventory.set(i, items.get(i));
    }
}
