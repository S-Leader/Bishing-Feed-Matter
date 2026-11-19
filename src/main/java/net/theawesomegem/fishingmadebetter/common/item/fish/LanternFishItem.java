package net.theawesomegem.fishingmadebetter.common.item.fish;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.theawesomegem.fishingmadebetter.Constants;
import net.theawesomegem.fishingmadebetter.client.model.fish.LanternFishHelmetModel;

import java.util.UUID;
import java.util.function.Consumer;

public class LanternFishItem extends ArmorItem {
    private final Multimap<Attribute, AttributeModifier> attributes;

    public LanternFishItem() {
        super(Material.INSTANCE, Type.HELMET, new Properties().food(FishFoods.RAW_FISH));
        attributes = ImmutableMultimap.<Attribute, AttributeModifier>builder()
                .putAll(super.getDefaultAttributeModifiers(EquipmentSlot.HEAD))
                .put(Attributes.MAX_HEALTH, new AttributeModifier(UUID.fromString("0a3c5d3e-38ad-4e51-a5c4-fae07e3e2822"), "Lantern fish bite", -0.1, AttributeModifier.Operation.MULTIPLY_TOTAL))
                .build();
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.HEAD ? attributes : super.getDefaultAttributeModifiers(slot);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!player.isCrouching()) return super.use(level, player, hand);
        ItemStack stack = player.getItemInHand(hand);
        if (!player.canEat(false)) return InteractionResultHolder.fail(stack);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return Constants.MOD_ID + ":textures/entity/fish/lantern_fish_helmet.png";
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private HumanoidModel<?> model;

            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
                if (model == null) model = new LanternFishHelmetModel();
                return model;
            }
        });
    }

    private enum Material implements ArmorMaterial {
        INSTANCE;

        public int getDurabilityForType(Type type) {
            return 130;
        }

        public int getDefenseForType(Type type) {
            return 1;
        }

        public int getEnchantmentValue() {
            return 5;
        }

        public SoundEvent getEquipSound() {
            return SoundEvents.PUFFER_FISH_HURT;
        }

        public Ingredient getRepairIngredient() {
            return Ingredient.of(Items.IRON_INGOT);
        }

        public String getName() {
            return Constants.MOD_ID + ":lantern_fish";
        }

        public float getToughness() {
            return 1;
        }

        public float getKnockbackResistance() {
            return 0;
        }
    }
}
