package net.theawesomegem.fishingmadebetter.common.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.fml.ModList;
import net.theawesomegem.fishingmadebetter.registry.ModLootEntries;

import java.util.function.Consumer;

/**
 * A loot-table reference that disappears from its weighted pool when its owner
 * mod is absent. It also skips validation of the missing external table.
 */
public final class OptionalModLootTableEntry extends LootPoolSingletonContainer {
    private final String modId;
    private final ResourceLocation table;

    private OptionalModLootTableEntry(
            String modId,
            ResourceLocation table,
            int weight,
            int quality,
            LootItemCondition[] conditions,
            LootItemFunction[] functions
    ) {
        super(weight, quality, conditions, functions);
        this.modId = modId;
        this.table = table;
    }

    @Override
    public LootPoolEntryType getType() {
        return ModLootEntries.OPTIONAL_MOD_LOOT_TABLE.get();
    }

    @Override
    public boolean expand(LootContext context, Consumer<net.minecraft.world.level.storage.loot.entries.LootPoolEntry> consumer) {
        return ModList.get().isLoaded(modId) && super.expand(context, consumer);
    }

    @Override
    protected void createItemStack(Consumer<ItemStack> output, LootContext context) {
        context.getResolver().getLootTable(table).getRandomItemsRaw(context, output);
    }

    @Override
    public void validate(ValidationContext context) {
        super.validate(context);
        if (!ModList.get().isLoaded(modId)) {
            return;
        }

        LootDataId<LootTable> id = new LootDataId<>(LootDataType.TABLE, table);
        if (context.hasVisitedElement(id)) {
            context.reportProblem("Table " + table + " is recursively called");
            return;
        }
        context.resolver().getElementOptional(id).ifPresentOrElse(
                lootTable -> lootTable.validate(context.enterElement("->{" + table + "}", id)),
                () -> context.reportProblem("Unknown loot table called " + table)
        );
    }

    public static final class Serializer extends LootPoolSingletonContainer.Serializer<OptionalModLootTableEntry> {
        @Override
        public void serializeCustom(JsonObject json, OptionalModLootTableEntry entry, JsonSerializationContext context) {
            super.serializeCustom(json, entry, context);
            json.addProperty("modid", entry.modId);
            json.addProperty("name", entry.table.toString());
        }

        @Override
        protected OptionalModLootTableEntry deserialize(
                JsonObject json,
                JsonDeserializationContext context,
                int weight,
                int quality,
                LootItemCondition[] conditions,
                LootItemFunction[] functions
        ) {
            return new OptionalModLootTableEntry(
                    GsonHelper.getAsString(json, "modid"),
                    new ResourceLocation(GsonHelper.getAsString(json, "name")),
                    weight,
                    quality,
                    conditions,
                    functions
            );
        }
    }
}
