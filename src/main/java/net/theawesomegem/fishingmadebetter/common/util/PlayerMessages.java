package net.theawesomegem.fishingmadebetter.common.util;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public final class PlayerMessages {

    public static void send(Player player, Component message, boolean overlay) {
        if (overlay) player.sendOverlayMessage(message);
        else player.sendSystemMessage(message);
    }
}
