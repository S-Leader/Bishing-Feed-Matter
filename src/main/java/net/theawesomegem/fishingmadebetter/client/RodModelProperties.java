package net.theawesomegem.fishingmadebetter.client;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RegisterRangeSelectItemModelPropertyEvent;
import net.theawesomegem.fishingmadebetter.Constants;
import net.theawesomegem.fishingmadebetter.common.item.BetterFishingRodItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class RodModelProperties {
    private static final Map<String, MapCodec<AttachmentProperty>> CODECS = new HashMap<>();

    public static void register(RegisterRangeSelectItemModelPropertyEvent event) {
        for (String kind : List.of("hook", "reel", "bobber", "cast")) {
            var id = Identifier.fromNamespaceAndPath(Constants.MOD_ID, kind);
            var codec = MapCodec.unit(new AttachmentProperty(kind));
            CODECS.put(kind, codec);
            event.register(id, codec);
        }
    }

    private record AttachmentProperty(String kind) implements RangeSelectItemModelProperty {
        @Override
        public float get(ItemStack stack, ClientLevel level, ItemOwner owner, int seed) {
            return switch (kind) {
                case "hook" -> hook(stack);
                case "reel" -> reel(stack);
                case "bobber" -> bobber(stack);
                default -> cast(stack, owner == null ? null : owner.asLivingEntity());
            };
        }

        @Override
        public MapCodec<AttachmentProperty> type() {
            return CODECS.get(kind);
        }
    }

    public static final Identifier REEL = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "reel");
    public static final Identifier BOBBER = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "bobber");
    public static final Identifier HOOK = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "hook");
    public static final Identifier CAST = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "cast");
    public static final List<Identifier> ROD_IDS = List.of(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "fishing_rod_wood"), Identifier.fromNamespaceAndPath(Constants.MOD_ID, "fishing_rod_iron"), Identifier.fromNamespaceAndPath(Constants.MOD_ID, "fishing_rod_diamond"), Identifier.fromNamespaceAndPath(Constants.MOD_ID, "fishing_rod_steel"), Identifier.fromNamespaceAndPath(Constants.MOD_ID, "fishing_rod_netherite"));

    public static float reel(ItemStack stack) {
        return valueFor(BetterFishingRodItem.getReelItem(stack));
    }

    public static float bobber(ItemStack stack) {
        return valueFor(BetterFishingRodItem.getBobberItem(stack));
    }

    public static float hook(ItemStack stack) {
        return valueFor(BetterFishingRodItem.getHookItem(stack));
    }

    public static float cast(ItemStack stack, LivingEntity entity) {
        if (entity == null) {
            return 0.0F;
        }

        boolean mainHandRod = entity.getMainHandItem() == stack;
        boolean offHandRod = entity.getOffhandItem() == stack;
        if (entity.getMainHandItem().getItem() instanceof FishingRodItem) {
            offHandRod = false;
        }

        return (mainHandRod || offHandRod) && entity instanceof Player player && player.fishing != null ? 1.0F : 0.0F;
    }

    public static List<Item> rodItems() {
        return ROD_IDS.stream().map(BuiltInRegistries.ITEM::getValue).toList();
    }

    private static float valueFor(Item item) {
        Identifier id = BuiltInRegistries.ITEM.getKey(item);
        if (id == null || !Constants.MOD_ID.equals(id.getNamespace())) {
            return 0.0F;
        }

        return switch (id.getPath()) {
            case "reel_basic", "bobber_basic", "hook_basic" -> 0.1F;
            case "reel_fast", "bobber_obsidian", "hook_barbed" -> 0.2F;
            case "reel_long", "bobber_void", "hook_fatty" -> 0.3F;
            case "bobber_heavy", "hook_shiny" -> 0.4F;
            case "bobber_lightweight", "hook_magnetic" -> 0.5F;
            default -> 0.0F;
        };
    }
}
