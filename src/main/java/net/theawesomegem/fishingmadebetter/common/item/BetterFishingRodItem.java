package net.theawesomegem.fishingmadebetter.common.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.ItemStackHandler;
import net.theawesomegem.fishingmadebetter.Constants;
import net.theawesomegem.fishingmadebetter.common.entity.LavaFishingHook;
import net.theawesomegem.fishingmadebetter.common.entity.VoidFishingHook;
import net.theawesomegem.fishingmadebetter.common.entity.WaterFishingHook;
import net.theawesomegem.fishingmadebetter.common.item.attachment.BobberItem;
import net.theawesomegem.fishingmadebetter.common.item.attachment.HookItem;
import net.theawesomegem.fishingmadebetter.common.item.attachment.ReelItem;
import net.theawesomegem.fishingmadebetter.common.util.BaitUtil;

public class BetterFishingRodItem extends FishingRodItem {
    private static final String BAIT_ITEM = "BaitItem";
    private static final String BAIT_METADATA = "BaitMetadata";
    private static final String BAIT_DISPLAY_NAME = "BaitDisplayName";
    private static final String REEL_ITEM = "ReelItem";
    private static final String REEL_DAMAGE = "ReelDamage";
    private static final String BOBBER_ITEM = "BobberItem";
    private static final String BOBBER_DAMAGE = "BobberDamage";
    private static final String HOOK_ITEM = "HookItem";
    private static final String HOOK_DAMAGE = "HookDamage";

    private static final ResourceLocation BASIC_REEL = new ResourceLocation(Constants.MOD_ID, "reel_basic");
    private static final ResourceLocation BASIC_BOBBER = new ResourceLocation(Constants.MOD_ID, "bobber_basic");
    private static final ResourceLocation BASIC_HOOK = new ResourceLocation(Constants.MOD_ID, "hook_basic");

    private final Tier tier;

    public BetterFishingRodItem(Tier tier) {
        this(tier, false);
    }

    public BetterFishingRodItem(Tier tier, boolean fireResistant) {
        super(properties(tier, fireResistant));
        this.tier = tier;
    }

    private static Item.Properties properties(Tier tier, boolean fireResistant) {
        Item.Properties properties = new Item.Properties().durability(tier.getUses());
        return fireResistant ? properties.fireResistant() : properties;
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new RodInventoryProvider(stack, nbt);
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return tier.getRepairIngredient().test(repairCandidate) || super.isValidRepairItem(stack, repairCandidate);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        // Match the vanilla fishing rod enchantability while explicitly keeping
        // the Forge ItemStack-sensitive path enabled for every FMB rod tier.
        return 1;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return isSupportedRodEnchantment(enchantment);
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return EnchantmentHelper.getEnchantments(book).keySet().stream().anyMatch(BetterFishingRodItem::isSupportedRodEnchantment);
    }

    private static boolean isSupportedRodEnchantment(Enchantment enchantment) {
        return enchantment == Enchantments.UNBREAKING
                || enchantment == Enchantments.MENDING
                || enchantment.category == EnchantmentCategory.FISHING_ROD;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        ItemStack otherHand = player.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        if (otherHand.getItem() instanceof FishingRodItem) {
            return InteractionResultHolder.fail(stack);
        }
        BobberItem bobber = getBobberItem(stack);
        return useCustomHook(level, player, hand, stack, bobber);
    }

    private InteractionResultHolder<ItemStack> useCustomHook(Level level, Player player, InteractionHand hand, ItemStack stack, BobberItem bobber) {
        if (player.fishing != null) {
            if (!level.isClientSide) {
                int damage = player.fishing.retrieve(stack);
                damageRodAndAttachments(stack, damage, player, hand);
            }

            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FISHING_BOBBER_RETRIEVE,
                    SoundSource.NEUTRAL, 1.0F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
            player.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
        } else {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FISHING_BOBBER_THROW,
                    SoundSource.NEUTRAL, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
            if (!level.isClientSide) {
                FishingHook hook = createHook(player, level, stack, bobber);
                level.addFreshEntity(hook);
            }

            player.awardStat(Stats.ITEM_USED.get(this));
            player.gameEvent(GameEvent.ITEM_INTERACT_START);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    private static FishingHook createHook(Player player, Level level, ItemStack stack, BobberItem bobber) {
        if (bobber.isVoidBobber()) {
            return new VoidFishingHook(player, level, stack);
        }
        if (bobber.isLavaBobber()) {
            return new LavaFishingHook(player, level, stack);
        }
        return new WaterFishingHook(player, level, stack);
    }

    private void damageRodAndAttachments(ItemStack stack, int damage, Player player, InteractionHand hand) {
        if (damage <= 0 || player.getAbilities().instabuild || !stack.isDamageableItem()) {
            return;
        }

        int previousRodDamage = stack.getDamageValue();
        boolean rodBroken = stack.hurt(damage, player.getRandom(), player instanceof ServerPlayer serverPlayer ? serverPlayer : null);
        int appliedDamage = stack.getDamageValue() - previousRodDamage;
        if (appliedDamage <= 0) {
            return;
        }

        damageAttachment(stack, AttachmentSlot.REEL, appliedDamage);
        damageAttachment(stack, AttachmentSlot.BOBBER, appliedDamage);
        damageAttachment(stack, AttachmentSlot.HOOK, appliedDamage);

        if (rodBroken) {
            dropStoredAttachment(player, AttachmentSlot.REEL, stack);
            dropStoredAttachment(player, AttachmentSlot.BOBBER, stack);
            dropStoredAttachment(player, AttachmentSlot.HOOK, stack);
            player.broadcastBreakEvent(hand);
            Item brokenItem = stack.getItem();
            stack.shrink(1);
            player.awardStat(Stats.ITEM_BROKEN.get(brokenItem));
            stack.setDamageValue(0);
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
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        ReelItem reel = getReelItem(stack);
        BobberItem bobber = getBobberItem(stack);
        HookItem hook = getHookItem(stack);

        tooltip.add(Component.translatable("tooltip.fishingmadebetter.fishing_rod.reel.name")
                .append(": ")
                .append(componentFor(reel))
                .withStyle(ChatFormatting.BLUE));
        tooltip.add(durabilityLine(reel, getReelDamage(stack)));
        tooltip.add(Component.translatable("tooltip.fishingmadebetter.fishing_rod.reel.range")
                .append(": " + reel.getReelRange() + "m")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.fishingmadebetter.fishing_rod.reel.speed")
                .append(": " + reel.getReelSpeed() + "m/s")
                .withStyle(ChatFormatting.GRAY));

        tooltip.add(Component.translatable("tooltip.fishingmadebetter.fishing_rod.bobber")
                .append(": ")
                .append(componentFor(bobber))
                .withStyle(ChatFormatting.BLUE));
        tooltip.add(durabilityLine(bobber, getBobberDamage(stack)));
        tooltip.add(Component.translatable("tooltip.fishingmadebetter.fishing_rod.bobber.can_fish_in")
                .append(": ")
                .append(Component.translatable(bobberLiquidKey(bobber)))
                .withStyle(ChatFormatting.GRAY));

        tooltip.add(Component.translatable("tooltip.fishingmadebetter.fishing_rod.hook")
                .append(": ")
                .append(componentFor(hook))
                .withStyle(ChatFormatting.BLUE));
        tooltip.add(durabilityLine(hook, getHookDamage(stack)));

        String baitDisplayName = getBaitDisplayName(stack);
        tooltip.add(Component.translatable("tooltip.fishingmadebetter.fishing_rod.bait")
                .append(": ")
                .append(baitDisplayName == null || baitDisplayName.isEmpty()
                        ? Component.translatable("tooltip.fishingmadebetter.fishing_rod.bait_none")
                        : Component.literal(baitDisplayName))
                .withStyle(ChatFormatting.BLUE));
    }

    private static Component componentFor(Item item) {
        return item.getDescription();
    }

    private static Component durabilityLine(Item item, int damage) {
        int maxDamage = item.getDefaultInstance().getMaxDamage();
        String value = maxDamage > 0 ? (maxDamage - damage) + "/" + maxDamage : "-/-";
        return Component.literal("  ")
                .append(Component.translatable("tooltip.fishingmadebetter.fishing_rod.durability.title"))
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

    @Nullable
    public static String getBaitItem(ItemStack stack) {
        return hasBait(stack) ? stack.getOrCreateTag().getString(BAIT_ITEM) : null;
    }

    public static void setBaitItem(ItemStack stack, String bait) {
        stack.getOrCreateTag().putString(BAIT_ITEM, bait);
        refreshCapability(stack);
    }

    public static void removeBait(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return;
        }
        tag.remove(BAIT_ITEM);
        tag.remove(BAIT_METADATA);
        tag.remove(BAIT_DISPLAY_NAME);
        clearEmptyTag(stack, tag);
        refreshCapability(stack);
    }

    public static int getBaitMetadata(ItemStack stack) {
        return hasBait(stack) ? stack.getOrCreateTag().getInt(BAIT_METADATA) : 0;
    }

    public static void setBaitMetadata(ItemStack stack, int metadata) {
        stack.getOrCreateTag().putInt(BAIT_METADATA, metadata);
        refreshCapability(stack);
    }

    @Nullable
    public static String getBaitDisplayName(ItemStack stack) {
        return hasBait(stack) ? stack.getOrCreateTag().getString(BAIT_DISPLAY_NAME) : null;
    }

    public static void setBaitDisplayName(ItemStack stack, String baitDisplayName) {
        stack.getOrCreateTag().putString(BAIT_DISPLAY_NAME, baitDisplayName);
        refreshCapability(stack);
    }

    public static boolean hasBait(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(BAIT_ITEM);
    }

    public static ReelItem getReelItem(ItemStack stack) {
        return getAttachment(stack, REEL_ITEM, BASIC_REEL, ReelItem.class);
    }

    public static void setReelItem(ItemStack stack, ReelItem item) {
        setAttachment(stack, REEL_ITEM, item);
        refreshCapability(stack);
    }

    public static void removeReelItem(ItemStack stack) {
        removeAttachment(stack, REEL_ITEM, REEL_DAMAGE);
        refreshCapability(stack);
    }

    public static int getReelDamage(ItemStack stack) {
        return hasReelItem(stack) ? stack.getOrCreateTag().getInt(REEL_DAMAGE) : 0;
    }

    public static void setReelDamage(ItemStack stack, int damage) {
        stack.getOrCreateTag().putInt(REEL_DAMAGE, hasReelItem(stack) ? damage : 0);
        refreshCapability(stack);
    }

    public static boolean hasReelItem(ItemStack stack) {
        return hasTag(stack, REEL_ITEM);
    }

    public static BobberItem getBobberItem(ItemStack stack) {
        return getAttachment(stack, BOBBER_ITEM, BASIC_BOBBER, BobberItem.class);
    }

    public static void setBobberItem(ItemStack stack, BobberItem item) {
        setAttachment(stack, BOBBER_ITEM, item);
        refreshCapability(stack);
    }

    public static void removeBobberItem(ItemStack stack) {
        removeAttachment(stack, BOBBER_ITEM, BOBBER_DAMAGE);
        refreshCapability(stack);
    }

    public static int getBobberDamage(ItemStack stack) {
        return hasBobberItem(stack) ? stack.getOrCreateTag().getInt(BOBBER_DAMAGE) : 0;
    }

    public static void setBobberDamage(ItemStack stack, int damage) {
        stack.getOrCreateTag().putInt(BOBBER_DAMAGE, hasBobberItem(stack) ? damage : 0);
        refreshCapability(stack);
    }

    public static boolean hasBobberItem(ItemStack stack) {
        return hasTag(stack, BOBBER_ITEM);
    }

    public static HookItem getHookItem(ItemStack stack) {
        return getAttachment(stack, HOOK_ITEM, BASIC_HOOK, HookItem.class);
    }

    public static void setHookItem(ItemStack stack, HookItem item) {
        setAttachment(stack, HOOK_ITEM, item);
        refreshCapability(stack);
    }

    public static void removeHookItem(ItemStack stack) {
        removeAttachment(stack, HOOK_ITEM, HOOK_DAMAGE);
        refreshCapability(stack);
    }

    public static int getHookDamage(ItemStack stack) {
        return hasHookItem(stack) ? stack.getOrCreateTag().getInt(HOOK_DAMAGE) : 0;
    }

    public static void setHookDamage(ItemStack stack, int damage) {
        stack.getOrCreateTag().putInt(HOOK_DAMAGE, hasHookItem(stack) ? damage : 0);
        refreshCapability(stack);
    }

    public static boolean hasHookItem(ItemStack stack) {
        return hasTag(stack, HOOK_ITEM);
    }

    private static boolean hasTag(ItemStack stack, String key) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(key);
    }

    private static void setAttachment(ItemStack stack, String key, Item item) {
        BuiltInRegistries.ITEM.getKey(item);
        stack.getOrCreateTag().putString(key, BuiltInRegistries.ITEM.getKey(item).toString());
    }

    private static void removeAttachment(ItemStack stack, String itemKey, String damageKey) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return;
        }
        tag.remove(itemKey);
        tag.remove(damageKey);
        clearEmptyTag(stack, tag);
    }

    private static <T extends Item> T getAttachment(ItemStack stack, String key, ResourceLocation fallback, Class<T> attachmentType) {
        ResourceLocation id = fallback;
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(key)) {
            ResourceLocation parsed = ResourceLocation.tryParse(tag.getString(key));
            if (parsed != null) {
                id = parsed;
            }
        }

        Item item = BuiltInRegistries.ITEM.get(id);
        if (attachmentType.isInstance(item)) {
            return attachmentType.cast(item);
        }
        return attachmentType.cast(BuiltInRegistries.ITEM.get(fallback));
    }

    private static void clearEmptyTag(ItemStack stack, CompoundTag tag) {
        if (tag.isEmpty()) {
            stack.setTag(null);
        }
    }

    private static void refreshCapability(ItemStack stack) {
        stack.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            if (handler instanceof RodItemStackHandler rodHandler) {
                rodHandler.refreshFromRod();
            }
        });
    }

    /** Four-slot view used by Aquaculture's tackle box: hook, bait, reel, bobber. */
    private static final class RodItemStackHandler extends ItemStackHandler {
        private final ItemStack rod;
        private boolean synchronizing;

        private RodItemStackHandler(ItemStack rod) {
            super(4);
            this.rod = rod;
            refreshFromRod();
        }

        private void refreshFromRod() {
            if (synchronizing) {
                return;
            }
            synchronizing = true;
            setStackInSlot(0, explicitAttachment(rod, HOOK_ITEM, HOOK_DAMAGE));
            setStackInSlot(1, baitStack(rod));
            setStackInSlot(2, explicitAttachment(rod, REEL_ITEM, REEL_DAMAGE));
            setStackInSlot(3, explicitAttachment(rod, BOBBER_ITEM, BOBBER_DAMAGE));
            synchronizing = false;
            saveInventoryTag();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return switch (slot) {
                case 0 -> stack.getItem() instanceof HookItem;
                case 1 -> BaitUtil.isPotentialBait(stack);
                case 2 -> stack.getItem() instanceof ReelItem;
                case 3 -> stack.getItem() instanceof BobberItem;
                default -> false;
            };
        }

        @Override
        protected int getStackLimit(int slot, ItemStack stack) {
            return 1;
        }

        @Override
        protected void onContentsChanged(int slot) {
            if (synchronizing) {
                return;
            }
            synchronizing = true;
            syncToRod();
            saveInventoryTag();
            synchronizing = false;
        }

        private void syncToRod() {
            ItemStack hook = getStackInSlot(0);
            if (hook.getItem() instanceof HookItem hookItem) {
                setHookItem(rod, hookItem);
                setHookDamage(rod, hook.getDamageValue());
            } else {
                removeHookItem(rod);
            }

            ItemStack bait = getStackInSlot(1);
            if (BaitUtil.isPotentialBait(bait)) {
                setBaitItem(rod, BaitUtil.getBaitId(bait));
                setBaitMetadata(rod, 0);
                setBaitDisplayName(rod, bait.getHoverName().getString());
            } else {
                removeBait(rod);
            }

            ItemStack reel = getStackInSlot(2);
            if (reel.getItem() instanceof ReelItem reelItem) {
                setReelItem(rod, reelItem);
                setReelDamage(rod, reel.getDamageValue());
            } else {
                removeReelItem(rod);
            }

            ItemStack bobber = getStackInSlot(3);
            if (bobber.getItem() instanceof BobberItem bobberItem) {
                setBobberItem(rod, bobberItem);
                setBobberDamage(rod, bobber.getDamageValue());
            } else {
                removeBobberItem(rod);
            }
        }

        private void saveInventoryTag() {
            rod.getOrCreateTag().put("Inventory", serializeNBT());
        }

        private static ItemStack explicitAttachment(ItemStack rod, String itemKey, String damageKey) {
            CompoundTag tag = rod.getTag();
            if (tag == null || !tag.contains(itemKey)) {
                return ItemStack.EMPTY;
            }
            ResourceLocation id = ResourceLocation.tryParse(tag.getString(itemKey));
            if (id == null) {
                return ItemStack.EMPTY;
            }
            ItemStack result = BuiltInRegistries.ITEM.get(id).getDefaultInstance();
            result.setDamageValue(tag.getInt(damageKey));
            return result;
        }

        private static ItemStack baitStack(ItemStack rod) {
            String baitId = getBaitItem(rod);
            ResourceLocation id = ResourceLocation.tryParse(baitId == null ? "" : baitId);
            return id == null ? ItemStack.EMPTY : BuiltInRegistries.ITEM.get(id).getDefaultInstance();
        }
    }

    private static final class RodInventoryProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
        private final RodItemStackHandler handler;
        private final LazyOptional<ItemStackHandler> capability;

        private RodInventoryProvider(ItemStack rod, @Nullable CompoundTag nbt) {
            this.handler = new RodItemStackHandler(rod);
            if (nbt != null && nbt.contains("Items")) {
                this.handler.deserializeNBT(nbt);
            }
            this.capability = LazyOptional.of(() -> handler);
        }

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
            return cap == ForgeCapabilities.ITEM_HANDLER ? capability.cast() : LazyOptional.empty();
        }

        @Override
        public CompoundTag serializeNBT() {
            return handler.serializeNBT();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            handler.deserializeNBT(nbt);
        }
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
