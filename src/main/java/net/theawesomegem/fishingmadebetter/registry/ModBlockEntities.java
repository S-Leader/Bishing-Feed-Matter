package net.theawesomegem.fishingmadebetter.registry;

import java.util.function.Supplier;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.theawesomegem.fishingmadebetter.common.block.BaitBoxBlockEntity;

public final class ModBlockEntities {
    public static Supplier<BlockEntityType<BaitBoxBlockEntity>> BAIT_BOX;

    private ModBlockEntities() {
    }
}
