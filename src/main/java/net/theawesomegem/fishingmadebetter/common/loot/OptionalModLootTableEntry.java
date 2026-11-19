package net.theawesomegem.fishingmadebetter.common.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.Validatable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntry;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.fml.ModList;

import java.util.List;
import java.util.function.Consumer;

public final class OptionalModLootTableEntry extends LootPoolSingletonContainer {
    public static final MapCodec<OptionalModLootTableEntry> CODEC =
            RecordCodecBuilder.mapCodec(
                    i ->
                            i.group(
                                            Codec.STRING.fieldOf("modid").forGetter(e -> e.modId),
                                            LootTable.KEY_CODEC
                                                    .fieldOf("name")
                                                    .forGetter(e -> e.table))
                                    .and(singletonFields(i))
                                    .apply(i, OptionalModLootTableEntry::new));
    private final String modId;
    private final ResourceKey<LootTable> table;

    private OptionalModLootTableEntry(
            String modId,
            ResourceKey<LootTable> table,
            int weight,
            int quality,
            List<LootItemCondition> conditions,
            List<LootItemFunction> functions) {
        super(weight, quality, conditions, functions);
        this.modId = modId;
        this.table = table;
    }

    @Override
    public MapCodec<OptionalModLootTableEntry> codec() {
        return CODEC;
    }

    @Override
    public boolean expand(LootContext context, Consumer<LootPoolEntry> output) {
        return ModList.get().isLoaded(modId) && super.expand(context, output);
    }

    @Override
    protected void createItemStack(Consumer<ItemStack> output, LootContext context) {
        context.getResolver()
                .get(table)
                .map(Holder::value)
                .orElse(LootTable.EMPTY)
                .getRandomItemsRaw(context, output);
    }

    @Override
    public void validate(ValidationContext context) {
        super.validate(context);
        if (ModList.get().isLoaded(modId)) Validatable.validateReference(context, table);
    }
}
