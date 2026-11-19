package net.theawesomegem.fishingmadebetter.common.item;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.theawesomegem.fishingmadebetter.Constants;
import net.theawesomegem.fishingmadebetter.common.entity.LavaFishingHook;
import net.theawesomegem.fishingmadebetter.common.entity.VoidFishingHook;
import net.theawesomegem.fishingmadebetter.common.entity.WaterFishingHook;
import net.theawesomegem.fishingmadebetter.common.item.attachment.BobberItem;
import net.theawesomegem.fishingmadebetter.common.item.attachment.HookItem;
import net.theawesomegem.fishingmadebetter.common.item.attachment.ReelItem;
import net.theawesomegem.fishingmadebetter.common.util.StackData;
import net.theawesomegem.fishingmadebetter.compat.AquacultureCompat;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BetterFishingRodItem extends FishingRodItem {
    private final ToolMaterial tier;

    public BetterFishingRodItem(ToolMaterial tier, Item.Properties properties) {
        super(
                AquacultureCompat.rodProperties(
                        properties
                                .durability(tier.durability())
                                .repairable(tier.repairItems())
                                .enchantable(1)));
        this.tier = tier;
    }

    public ToolMaterial getTier() {
        return tier;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        ItemStack otherHand =
                player.getItemInHand(
                        hand == InteractionHand.MAIN_HAND
                                ? InteractionHand.OFF_HAND
                                : InteractionHand.MAIN_HAND);
        if (otherHand.getItem() instanceof FishingRodItem) return InteractionResult.FAIL;
        if (player.fishing != null) {
            if (level instanceof ServerLevel) {
                int damage = player.fishing.retrieve(stack);
                damageRodAndAttachments(stack, damage, player, hand);
            }
            level.playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundEvents.FISHING_BOBBER_RETRIEVE,
                    SoundSource.NEUTRAL,
                    1,
                    0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
            stack.causeUseVibration(player, GameEvent.ITEM_INTERACT_FINISH);
        } else {
            level.playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundEvents.FISHING_BOBBER_THROW,
                    SoundSource.NEUTRAL,
                    0.5F,
                    0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
            if (level instanceof ServerLevel) {
                BobberItem bobber = getBobberItem(stack);
                FishingHook hook =
                        bobber.isVoidBobber()
                                ? new VoidFishingHook(player, level, stack)
                                : bobber.isLavaBobber()
                                ? new LavaFishingHook(player, level, stack)
                                : new WaterFishingHook(player, level, stack);
                level.addFreshEntity(hook);
            }
            player.awardStat(Stats.ITEM_USED.get(this));
            stack.causeUseVibration(player, GameEvent.ITEM_INTERACT_START);
        }
        return InteractionResult.SUCCESS;
    }

    public void damageRodAndAttachments(
            ItemStack stack, int damage, Player player, InteractionHand hand) {
        if (damage <= 0 || player.getAbilities().instabuild || !stack.isDamageableItem()) return;
        ItemStack before = stack.copy();
        int previousDamage = stack.getDamageValue();
        stack.hurtAndBreak(damage, player, hand);
        int applied =
                stack.isEmpty()
                        ? before.getMaxDamage() - previousDamage
                        : stack.getDamageValue() - previousDamage;
        if (applied <= 0) return;
        ItemStack attachments = stack.isEmpty() ? before : stack;
        damageAttachment(attachments, AttachmentSlot.REEL, applied);
        damageAttachment(attachments, AttachmentSlot.BOBBER, applied);
        damageAttachment(attachments, AttachmentSlot.HOOK, applied);
        if (stack.isEmpty()) {
            dropStoredAttachment(player, AttachmentSlot.REEL, attachments);
            dropStoredAttachment(player, AttachmentSlot.BOBBER, attachments);
            dropStoredAttachment(player, AttachmentSlot.HOOK, attachments);
        }
    }

    private static void damageAttachment(ItemStack rod, AttachmentSlot slot, int damage) {
        if (!slot.has(rod)) {
            return;
        }

        Item attachment = slot.item(rod);
        int maxDamage = attachment.getDefaultInstance().getMaxDamage();
        if (maxDamage <= 0) {
            return;
        }

        int newDamage = slot.damage(rod) + damage;
        if (newDamage >= maxDamage) {
            slot.remove(rod);
        } else {
            slot.setDamage(rod, newDamage);
        }
    }

    private static void dropStoredAttachment(Player player, AttachmentSlot slot, ItemStack rod) {
        if (!slot.has(rod)) {
            return;
        }

        ItemStack attachmentStack = new ItemStack(slot.item(rod));
        attachmentStack.setDamageValue(slot.damage(rod));
        player.drop(attachmentStack, true);
        slot.remove(rod);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            TooltipDisplay display,
            Consumer<Component> output,
            TooltipFlag flag) {
        List<Component> tooltip = new ArrayList<>();
        ReelItem reel = getReelItem(stack);
        BobberItem bobber = getBobberItem(stack);
        HookItem hook = getHookItem(stack);

        tooltip.add(
                Component.translatable("tooltip.fishingmadebetter.fishing_rod.reel.name")
                        .append(": ")
                        .append(componentFor(reel))
                        .withStyle(ChatFormatting.BLUE));
        tooltip.add(durabilityLine(reel, getReelDamage(stack)));
        tooltip.add(
                Component.translatable("tooltip.fishingmadebetter.fishing_rod.reel.range")
                        .append(": " + reel.getReelRange() + "m")
                        .withStyle(ChatFormatting.GRAY));
        tooltip.add(
                Component.translatable("tooltip.fishingmadebetter.fishing_rod.reel.speed")
                        .append(": " + reel.getReelSpeed() + "m/s")
                        .withStyle(ChatFormatting.GRAY));

        tooltip.add(
                Component.translatable("tooltip.fishingmadebetter.fishing_rod.bobber")
                        .append(": ")
                        .append(componentFor(bobber))
                        .withStyle(ChatFormatting.BLUE));
        tooltip.add(durabilityLine(bobber, getBobberDamage(stack)));
        if (InputConstants.isKeyDown(
                Minecraft.getInstance().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT)) {
            tooltip.add(
                    Component.translatable("item.fishingmadebetter.bobber_heavy.tooltip")
                            .append(": +" + bobber.getVarianceModifier())
                            .withStyle(ChatFormatting.GRAY));
            tooltip.add(
                    Component.translatable("item.fishingmadebetter.bobber_lightweight.tooltip")
                            .append(": +" + bobber.getTensioningModifier())
                            .withStyle(ChatFormatting.GRAY));
        }
        tooltip.add(
                Component.translatable("tooltip.fishingmadebetter.fishing_rod.bobber.can_fish_in")
                        .append(": ")
                        .append(Component.translatable(bobberLiquidKey(bobber)))
                        .withStyle(ChatFormatting.GRAY));

        tooltip.add(
                Component.translatable("tooltip.fishingmadebetter.fishing_rod.hook")
                        .append(": ")
                        .append(componentFor(hook))
                        .withStyle(ChatFormatting.BLUE));
        tooltip.add(durabilityLine(hook, getHookDamage(stack)));
        if (InputConstants.isKeyDown(
                Minecraft.getInstance().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT)) {
            tooltip.add(
                    Component.translatable("item.fishingmadebetter.hook_barbed.tooltip")
                            .append(": " + hook.getTuggingReduction())
                            .withStyle(ChatFormatting.GRAY));
            tooltip.add(
                    Component.translatable("item.fishingmadebetter.hook_magnetic.tooltip")
                            .append(": +" + hook.getTreasureModifier() + "%")
                            .withStyle(ChatFormatting.GRAY));
            tooltip.add(
                    Component.translatable("item.fishingmadebetter.hook_shiny.tooltip")
                            .append(": +" + hook.getBiteRateModifier() + "%")
                            .withStyle(ChatFormatting.GRAY));
            tooltip.add(
                    Component.translatable("item.fishingmadebetter.hook_fatty.tooltip")
                            .append(": +" + hook.getWeightModifier() + "%")
                            .withStyle(ChatFormatting.GRAY));
        }

        String baitDisplayName = getBaitDisplayName(stack);
        tooltip.add(
                Component.translatable("tooltip.fishingmadebetter.fishing_rod.bait")
                        .append(": ")
                        .append(
                                baitDisplayName == null || baitDisplayName.isEmpty()
                                        ? Component.translatable(
                                        "tooltip.fishingmadebetter.fishing_rod.bait_none")
                                        : Component.literal(baitDisplayName))
                        .withStyle(ChatFormatting.BLUE));
        tooltip.forEach(output);
    }

    private static Component componentFor(Item item) {
        return item.getDefaultInstance().getHoverName();
    }

    private static Component durabilityLine(Item item, int damage) {
        int maxDamage = item.getDefaultInstance().getMaxDamage();
        String value = maxDamage > 0 ? (maxDamage - damage) + "/" + maxDamage : "-/-";
        return Component.literal("  ")
                .append(
                        Component.translatable(
                                "tooltip.fishingmadebetter.fishing_rod.durability.title"))
                .append(": " + value)
                .withStyle(ChatFormatting.GRAY);
    }

    private static String bobberLiquidKey(BobberItem bobber) {
        if (bobber.isLavaBobber()) {
            return "tooltip.fishingmadebetter.fishing_rod.bobber.can_fish_in.lava";
        }
        if (bobber.isVoidBobber()) {
            return "tooltip.fishingmadebetter.fishing_rod.bobber.can_fish_in.void";
        }
        return "tooltip.fishingmadebetter.fishing_rod.bobber.can_fish_in.water";
    }

    public static ItemContainerContents inventory(ItemStack rod) {
        ItemContainerContents aqua = AquacultureCompat.rodInventory(rod);
        return aqua != null
                ? aqua
                : rod.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
    }

    public static void setInventory(ItemStack rod, ItemContainerContents contents) {
        rod.set(DataComponents.CONTAINER, contents);
        AquacultureCompat.setRodInventory(rod, contents);
    }

    public static ItemStack attachment(ItemStack rod, int slot) {
        ItemContainerContents contents = inventory(rod);
        return slot < contents.getSlots() ? contents.getStackInSlot(slot) : ItemStack.EMPTY;
    }

    private static void setAttachment(ItemStack rod, int slot, ItemStack value) {
        List<ItemStack> values = new ArrayList<>(4);
        for (int i = 0; i < 4; i++)
            values.add(i == slot ? value.copyWithCount(1) : attachment(rod, i));
        setInventory(rod, ItemContainerContents.fromItems(values));
    }

    public static @Nullable String getBaitItem(ItemStack stack) {
        ItemStack bait = attachment(stack, 1);
        return bait.isEmpty() ? null : BuiltInRegistries.ITEM.getKey(bait.getItem()).toString();
    }

    public static void setBaitItem(ItemStack stack, String id) {
        Identifier location = Identifier.tryParse(id);
        Item item = location == null ? null : BuiltInRegistries.ITEM.getValue(location);
        setAttachment(stack, 1, item == null ? ItemStack.EMPTY : item.getDefaultInstance());
    }

    public static void removeBait(ItemStack stack) {
        setAttachment(stack, 1, ItemStack.EMPTY);
    }

    public static boolean hasBait(ItemStack stack) {
        return !attachment(stack, 1).isEmpty();
    }

    public static int getBaitMetadata(ItemStack stack) {
        return StackData.copy(stack).getIntOr("BaitMetadata", 0);
    }

    public static void setBaitMetadata(ItemStack stack, int metadata) {
        StackData.update(stack, tag -> tag.putInt("BaitMetadata", metadata));
    }

    public static @Nullable String getBaitDisplayName(ItemStack stack) {
        return hasBait(stack) ? attachment(stack, 1).getHoverName().getString() : null;
    }

    public static void setBaitDisplayName(ItemStack stack, String name) {
        ItemStack bait = attachment(stack, 1);
        if (!bait.isEmpty()) {
            bait.set(DataComponents.CUSTOM_NAME, Component.literal(name));
            setAttachment(stack, 1, bait);
        }
    }

    private static Item fallback(String path) {
        return BuiltInRegistries.ITEM.getValue(
                Identifier.fromNamespaceAndPath(Constants.MOD_ID, path));
    }

    public static ReelItem getReelItem(ItemStack stack) {
        return attachment(stack, 2).getItem() instanceof ReelItem item
                ? item
                : (ReelItem) fallback("reel_basic");
    }

    public static BobberItem getBobberItem(ItemStack stack) {
        return attachment(stack, 3).getItem() instanceof BobberItem item
                ? item
                : (BobberItem) fallback("bobber_basic");
    }

    public static HookItem getHookItem(ItemStack stack) {
        return attachment(stack, 0).getItem() instanceof HookItem item
                ? item
                : (HookItem) fallback("hook_basic");
    }

    public static boolean hasReelItem(ItemStack stack) {
        return attachment(stack, 2).getItem() instanceof ReelItem;
    }

    public static boolean hasBobberItem(ItemStack stack) {
        return attachment(stack, 3).getItem() instanceof BobberItem;
    }

    public static boolean hasHookItem(ItemStack stack) {
        return attachment(stack, 0).getItem() instanceof HookItem;
    }

    public static void setReelItem(ItemStack stack, ReelItem item) {
        setAttachment(stack, 2, item.getDefaultInstance());
    }

    public static void setBobberItem(ItemStack stack, BobberItem item) {
        setAttachment(stack, 3, item.getDefaultInstance());
    }

    public static void setHookItem(ItemStack stack, HookItem item) {
        setAttachment(stack, 0, item.getDefaultInstance());
    }

    public static void removeReelItem(ItemStack stack) {
        setAttachment(stack, 2, ItemStack.EMPTY);
    }

    public static void removeBobberItem(ItemStack stack) {
        setAttachment(stack, 3, ItemStack.EMPTY);
    }

    public static void removeHookItem(ItemStack stack) {
        setAttachment(stack, 0, ItemStack.EMPTY);
    }

    public static int getReelDamage(ItemStack stack) {
        return attachment(stack, 2).getDamageValue();
    }

    public static int getBobberDamage(ItemStack stack) {
        return attachment(stack, 3).getDamageValue();
    }

    public static int getHookDamage(ItemStack stack) {
        return attachment(stack, 0).getDamageValue();
    }

    private static void setAttachmentDamage(ItemStack stack, int slot, int damage) {
        ItemStack item = attachment(stack, slot);
        if (!item.isEmpty()) {
            item.setDamageValue(damage);
            setAttachment(stack, slot, item);
        }
    }

    public static void setReelDamage(ItemStack stack, int damage) {
        setAttachmentDamage(stack, 2, damage);
    }

    public static void setBobberDamage(ItemStack stack, int damage) {
        setAttachmentDamage(stack, 3, damage);
    }

    public static void setHookDamage(ItemStack stack, int damage) {
        setAttachmentDamage(stack, 0, damage);
    }

    private enum AttachmentSlot {
        REEL {
            @Override
            boolean has(ItemStack rod) {
                return hasReelItem(rod);
            }

            @Override
            Item item(ItemStack rod) {
                return getReelItem(rod);
            }

            @Override
            int damage(ItemStack rod) {
                return getReelDamage(rod);
            }

            @Override
            void setDamage(ItemStack rod, int damage) {
                setReelDamage(rod, damage);
            }

            @Override
            void remove(ItemStack rod) {
                removeReelItem(rod);
            }
        },
        BOBBER {
            @Override
            boolean has(ItemStack rod) {
                return hasBobberItem(rod);
            }

            @Override
            Item item(ItemStack rod) {
                return getBobberItem(rod);
            }

            @Override
            int damage(ItemStack rod) {
                return getBobberDamage(rod);
            }

            @Override
            void setDamage(ItemStack rod, int damage) {
                setBobberDamage(rod, damage);
            }

            @Override
            void remove(ItemStack rod) {
                removeBobberItem(rod);
            }
        },
        HOOK {
            @Override
            boolean has(ItemStack rod) {
                return hasHookItem(rod);
            }

            @Override
            Item item(ItemStack rod) {
                return getHookItem(rod);
            }

            @Override
            int damage(ItemStack rod) {
                return getHookDamage(rod);
            }

            @Override
            void setDamage(ItemStack rod, int damage) {
                setHookDamage(rod, damage);
            }

            @Override
            void remove(ItemStack rod) {
                removeHookItem(rod);
            }
        };

        abstract boolean has(ItemStack rod);

        abstract Item item(ItemStack rod);

        abstract int damage(ItemStack rod);

        abstract void setDamage(ItemStack rod, int damage);

        abstract void remove(ItemStack rod);
    }
}
