package net.theawesomegem.fishingmadebetter.common.world;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

final class ElectricEelMonumentSavedData extends SavedData {
    private static final String DATA_NAME = "fishingmadebetter_monument_eels";
    private final LongSet checkedMonuments = new LongOpenHashSet();
    private final LongSet pendingMonuments = new LongOpenHashSet();

    static ElectricEelMonumentSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                ElectricEelMonumentSavedData::load,
                ElectricEelMonumentSavedData::new,
                DATA_NAME
        );
    }

    private static ElectricEelMonumentSavedData load(CompoundTag tag) {
        ElectricEelMonumentSavedData data = new ElectricEelMonumentSavedData();
        data.checkedMonuments.addAll(LongOpenHashSet.of(tag.getLongArray("CheckedMonuments")));
        data.pendingMonuments.addAll(LongOpenHashSet.of(tag.getLongArray("PendingMonuments")));
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

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putLongArray("CheckedMonuments", checkedMonuments.toLongArray());
        tag.putLongArray("PendingMonuments", pendingMonuments.toLongArray());
        return tag;
    }
}
