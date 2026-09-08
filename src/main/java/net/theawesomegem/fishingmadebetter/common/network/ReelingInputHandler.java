package net.theawesomegem.fishingmadebetter.common.network;

import net.minecraft.world.entity.player.Player;
import net.theawesomegem.fishingmadebetter.common.entity.FmbFishingHook;

public final class ReelingInputHandler {
    private ReelingInputHandler() {
    }

    public static void handle(Player player, ReelingInput input) {
        if (player.fishing instanceof FmbFishingHook hook) {
            hook.setReelingInput(input);
        }
    }
}
