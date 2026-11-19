package net.theawesomegem.fishingmadebetter.common.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.theawesomegem.fishingmadebetter.common.item.BetterFishingRodItem;
import net.theawesomegem.fishingmadebetter.common.item.attachment.BobberItem;
import net.theawesomegem.fishingmadebetter.common.item.attachment.HookItem;
import net.theawesomegem.fishingmadebetter.common.item.attachment.ReelItem;
import net.theawesomegem.fishingmadebetter.registry.ModRecipeSerializers;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RodAttachmentRecipe extends CustomRecipe {

    @Override
    public boolean matches(CraftingInput container, Level level) {
        return findSlots(container) != null;
    }

    @Override
    public ItemStack assemble(CraftingInput container) {
        Slots slots = findSlots(container);
        if (slots == null) {
            return ItemStack.EMPTY;
        }

        ItemStack rod = container.getItem(slots.rod()).copy();
        if (slots.reel() != -1) {
            ItemStack reel = container.getItem(slots.reel());
            BetterFishingRodItem.setReelItem(rod, (ReelItem) reel.getItem());
            BetterFishingRodItem.setReelDamage(rod, reel.getDamageValue());
        }
        if (slots.bobber() != -1) {
            ItemStack bobber = container.getItem(slots.bobber());
            BetterFishingRodItem.setBobberItem(rod, (BobberItem) bobber.getItem());
            BetterFishingRodItem.setBobberDamage(rod, bobber.getDamageValue());
        }
        if (slots.hook() != -1) {
            ItemStack hook = container.getItem(slots.hook());
            BetterFishingRodItem.setHookItem(rod, (HookItem) hook.getItem());
            BetterFishingRodItem.setHookDamage(rod, hook.getDamageValue());
        }
        if (slots.reel() == -1 && slots.bobber() == -1 && slots.hook() == -1) {
            BetterFishingRodItem.removeReelItem(rod);
            BetterFishingRodItem.removeBobberItem(rod);
            BetterFishingRodItem.removeHookItem(rod);
        }

        return rod;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput container) {
        Slots slots = findSlots(container);
        if (slots == null) {
            return NonNullList.withSize(container.size(), ItemStack.EMPTY);
        }

        ItemStack rod = container.getItem(slots.rod());
        List<ItemStack> oldAttachments = new ArrayList<>();
        addOldAttachment(
                oldAttachments,
                BetterFishingRodItem.hasReelItem(rod),
                BetterFishingRodItem.getReelItem(rod),
                BetterFishingRodItem.getReelDamage(rod));
        addOldAttachment(
                oldAttachments,
                BetterFishingRodItem.hasBobberItem(rod),
                BetterFishingRodItem.getBobberItem(rod),
                BetterFishingRodItem.getBobberDamage(rod));
        addOldAttachment(
                oldAttachments,
                BetterFishingRodItem.hasHookItem(rod),
                BetterFishingRodItem.getHookItem(rod),
                BetterFishingRodItem.getHookDamage(rod));

        if (slots.reel() != -1 || slots.bobber() != -1 || slots.hook() != -1) {
            oldAttachments.removeIf(stack -> shouldDiscardOldAttachment(stack, slots, container));
        }

        NonNullList<ItemStack> remaining = NonNullList.withSize(container.size(), ItemStack.EMPTY);
        for (int i = 0; i < oldAttachments.size() && i < remaining.size(); i++) {
            remaining.set(i, oldAttachments.get(i));
        }
        return remaining;
    }

    private static void addOldAttachment(
            List<ItemStack> oldAttachments, boolean present, Item item, int damage) {
        if (!present) {
            return;
        }
        ItemStack stack = new ItemStack(item);
        stack.setDamageValue(damage);
        oldAttachments.add(stack);
    }

    private static boolean shouldDiscardOldAttachment(
            ItemStack oldAttachment, Slots slots, CraftingInput container) {
        return (slots.reel() == -1 && oldAttachment.getItem() instanceof ReelItem)
                || (slots.bobber() == -1 && oldAttachment.getItem() instanceof BobberItem)
                || (slots.hook() == -1 && oldAttachment.getItem() instanceof HookItem)
                || sameAttachmentType(oldAttachment, slots.reel(), container)
                || sameAttachmentType(oldAttachment, slots.bobber(), container)
                || sameAttachmentType(oldAttachment, slots.hook(), container);
    }

    private static boolean sameAttachmentType(
            ItemStack oldAttachment, int slot, CraftingInput container) {
        return slot != -1
                && oldAttachment.getItem().getClass()
                == container.getItem(slot).getItem().getClass();
    }

    @Override
    public RecipeSerializer<RodAttachmentRecipe> getSerializer() {
        return ModRecipeSerializers.ROD_ATTACHMENT.get();
    }

    @Nullable
    private static Slots findSlots(CraftingInput container) {
        int rod = -1;
        int reel = -1;
        int bobber = -1;
        int hook = -1;
        int rods = 0;
        int reels = 0;
        int bobbers = 0;
        int hooks = 0;

        for (int i = 0; i < container.size(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.getItem() instanceof BetterFishingRodItem) {
                rods++;
                rod = i;
            } else if (stack.getItem() instanceof ReelItem reelItem && !reelItem.isCraftingOnly()) {
                reels++;
                reel = i;
            } else if (stack.getItem() instanceof BobberItem bobberItem
                    && !bobberItem.isCraftingOnly()) {
                bobbers++;
                bobber = i;
            } else if (stack.getItem() instanceof HookItem hookItem && !hookItem.isCraftingOnly()) {
                hooks++;
                hook = i;
            } else {
                return null;
            }
        }

        if (rods == 1 && reels < 2 && bobbers < 2 && hooks < 2) {
            return new Slots(rod, reel, bobber, hook);
        }
        return null;
    }

    private record Slots(int rod, int reel, int bobber, int hook) {
    }
}
