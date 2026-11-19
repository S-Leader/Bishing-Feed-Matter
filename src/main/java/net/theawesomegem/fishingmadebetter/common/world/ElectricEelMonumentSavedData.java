package net.theawesomegem.fishingmadebetter.common.world;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

final class ElectricEelMonumentSavedData extends SavedData {
    private static final String DATA_NAME = "fishingmadebetter_monument_eels";
    private static final SavedDataType<ElectricEelMonumentSavedData> TYPE =
            new SavedDataType<>(
                    Identifier.parse(DATA_NAME),
                    ElectricEelMonumentSavedData::new,
                    CompoundTag.CODEC.xmap(
                            ElectricEelMonumentSavedData::load,
                            data -> data.save(new CompoundTag())));
    private final LongSet checkedMonuments = new LongOpenHashSet();
    private final LongSet pendingMonuments = new LongOpenHashSet();

    static ElectricEelMonumentSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    private static ElectricEelMonumentSavedData load(CompoundTag tag) {
        ElectricEelMonumentSavedData data = new ElectricEelMonumentSavedData();
        data.checkedMonuments.addAll(
                LongOpenHashSet.of(
                        tag.getLongArray("CheckedMonuments").orElseGet(() -> new long[0])));
        data.pendingMonuments.addAll(
                LongOpenHashSet.of(
                        tag.getLongArray("PendingMonuments").orElseGet(() -> new long[0])));
        return data;
    }

    boolean hasChecked(long chunk) {
        return checkedMonuments.contains(chunk);
    }

    boolean isPending(long chunk) {
        return pendingMonuments.contains(chunk);
    }

    void recordRoll(long chunk, boolean passed) {
        checkedMonuments.add(chunk);
        if (passed) {
            pendingMonuments.add(chunk);
        }
        setDirty();
    }

    void markSpawned(long chunk) {
        if (pendingMonuments.remove(chunk)) {
            setDirty();
        }
    }

    public CompoundTag save(CompoundTag tag) {
        tag.putLongArray("CheckedMonuments", checkedMonuments.toLongArray());
        tag.putLongArray("PendingMonuments", pendingMonuments.toLongArray());
        return tag;
    }
}
