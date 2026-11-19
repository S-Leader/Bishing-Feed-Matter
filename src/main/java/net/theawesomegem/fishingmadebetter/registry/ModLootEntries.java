package net.theawesomegem.fishingmadebetter.registry;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.theawesomegem.fishingmadebetter.Constants;
import net.theawesomegem.fishingmadebetter.common.loot.OptionalModLootTableEntry;

public final class ModLootEntries {
    public static final DeferredRegister<MapCodec<? extends LootPoolEntryContainer>> LOOT_ENTRIES = DeferredRegister.create(Registries.LOOT_POOL_ENTRY_TYPE, Constants.MOD_ID);
    public static final DeferredHolder<MapCodec<? extends LootPoolEntryContainer>, MapCodec<OptionalModLootTableEntry>> OPTIONAL_MOD_LOOT_TABLE = LOOT_ENTRIES.register("optional_mod_loot_table", () -> OptionalModLootTableEntry.CODEC);
}
