package net.theawesomegem.fishingmadebetter.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.theawesomegem.fishingmadebetter.Constants;
import net.theawesomegem.fishingmadebetter.registry.ModItems;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public final class LanternFishLighting {
    private static volatile List<LightSource> sources = List.of();

    private LanternFishLighting() {
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            sources = List.of();
            return;
        }
        if (minecraft.level.getGameTime() % 4 != 0) return;
        List<LightSource> updated = new ArrayList<>();
        for (var player : minecraft.level.players()) {
            if (player.isAlive() && !player.isSpectator() && player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.LANTERN_FISH_ITEM.get())) {
                updated.add(new LightSource(BlockPos.containing(player.getEyePosition()), player.isUnderWater() ? 15 : 10));
            }
        }
        List<LightSource> previous = sources;
        if (previous.equals(updated)) return;
        sources = List.copyOf(updated);
        for (LightSource source : previous) if (!updated.contains(source)) dirty(minecraft, source);
        for (LightSource source : updated) if (!previous.contains(source)) dirty(minecraft, source);
    }

    private static void dirty(Minecraft minecraft, LightSource source) {
        BlockPos pos = source.pos;
        int radius = source.brightness;
        for (int x = SectionPos.blockToSectionCoord(pos.getX() - radius); x <= SectionPos.blockToSectionCoord(pos.getX() + radius); x++) {
            for (int y = SectionPos.blockToSectionCoord(pos.getY() - radius); y <= SectionPos.blockToSectionCoord(pos.getY() + radius); y++) {
                for (int z = SectionPos.blockToSectionCoord(pos.getZ() - radius); z <= SectionPos.blockToSectionCoord(pos.getZ() + radius); z++) {
                    minecraft.levelRenderer.setSectionDirty(x, y, z);
                }
            }
        }
    }

    public static int light(BlockPos pos, int packed) {
        int block = packed & 0xFFFF;
        for (LightSource source : sources) {
            double distance = Math.sqrt(source.pos.distSqr(pos));
            if (distance < source.brightness) block = Math.max(block, (int) ((source.brightness - distance) * 16));
        }
        return (packed & 0xFFFF0000) | block;
    }

    private record LightSource(BlockPos pos, int brightness) {
    }
}
