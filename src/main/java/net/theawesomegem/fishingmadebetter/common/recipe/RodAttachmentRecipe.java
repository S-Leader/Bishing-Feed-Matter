package net.theawesomegem.fishingmadebetter.common.recipe;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.theawesomegem.fishingmadebetter.common.item.BetterFishingRodItem;
import net.theawesomegem.fishingmadebetter.common.item.attachment.BobberItem;
import net.theawesomegem.fishingmadebetter.common.item.attachment.HookItem;
import net.theawesomegem.fishingmadebetter.common.item.attachment.ReelItem;
import net.theawesomegem.fishingmadebetter.registry.ModRecipeSerializers;

public class RodAttachmentRecipe extends CustomRecipe {
    public RodAttachmentRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        return findSlots(container) != null;
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
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
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer container) {
        Slots slots = findSlots(container);
        if (slots == null) {
            return NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);
        }

        ItemStack rod = container.getItem(slots.rod());
        List<ItemStack> oldAttachments = new ArrayList<>();
        addOldAttachment(oldAttachments, BetterFishingRodItem.hasReelItem(rod), BetterFishingRodItem.getReelItem(rod), BetterFishingRodItem.getReelDamage(rod));
        addOldAttachment(oldAttachments, BetterFishingRodItem.hasBobberItem(rod), BetterFishingRodItem.getBobberItem(rod), BetterFishingRodItem.getBobberDamage(rod));
        addOldAttachment(oldAttachments, BetterFishingRodItem.hasHookItem(rod), BetterFishingRodItem.getHookItem(rod), BetterFishingRodItem.getHookDamage(rod));

        if (slots.reel() != -1 || slots.bobber() != -1 || slots.hook() != -1) {
            oldAttachments.removeIf(stack -> shouldDiscardOldAttachment(stack, slots, container));
        }

        NonNullList<ItemStack> remaining = NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);
        for (int i = 0; i < oldAttachments.size() && i < remaining.size(); i++) {
            remaining.set(i, oldAttachments.get(i));
        }
        return remaining;
    }

    private static void addOldAttachment(List<ItemStack> oldAttachments, boolean present, net.minecraft.world.item.Item item, int damage) {
        if (!present) {
            return;
        }
        ItemStack stack = new ItemStack(item);
        stack.setDamageValue(damage);
        oldAttachments.add(stack);
    }

    private static boolean shouldDiscardOldAttachment(ItemStack oldAttachment, Slots slots, CraftingContainer container) {
        return (slots.reel() == -1 && oldAttachment.getItem() instanceof ReelItem)
                || (slots.bobber() == -1 && oldAttachment.getItem() instanceof BobberItem)
                || (slots.hook() == -1 && oldAttachment.getItem() instanceof HookItem)
                || sameAttachmentType(oldAttachment, slots.reel(), container)
                || sameAttachmentType(oldAttachment, slots.bobber(), container)
                || sameAttachmentType(oldAttachment, slots.hook(), container);
    }

    private static boolean sameAttachmentType(ItemStack oldAttachment, int slot, CraftingContainer container) {
        return slot != -1 && oldAttachment.getItem().getClass() == container.getItem(slot).getItem().getClass();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 4;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.ROD_ATTACHMENT.get();
    }

    @Nullable
    private static Slots findSlots(CraftingContainer container) {
        int rod = -1;
        int reel = -1;
        int bobber = -1;
        int hook = -1;
        int rods = 0;
        int reels = 0;
        int bobbers = 0;
        int hooks = 0;

        for (int i = 0; i < container.getContainerSize(); i++) {
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
            } else if (stack.getItem() instanceof BobberItem bobberItem && !bobberItem.isCraftingOnly()) {
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
