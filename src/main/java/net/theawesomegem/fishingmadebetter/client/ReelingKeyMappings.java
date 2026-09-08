package net.theawesomegem.fishingmadebetter.client;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.function.Consumer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.theawesomegem.fishingmadebetter.common.entity.FmbFishingHook;
import net.theawesomegem.fishingmadebetter.common.network.ReelingInput;
import org.lwjgl.glfw.GLFW;

public final class ReelingKeyMappings {
    public static final String CATEGORY = "key.categories.fishingmadebetter";
    public static final KeyMapping REEL_IN = new KeyMapping("key.fishingmadebetter.reel_in", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT, CATEGORY);
    public static final KeyMapping REEL_OUT = new KeyMapping("key.fishingmadebetter.reel_out", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT, CATEGORY);
    public static final KeyMapping OPEN_CONFIG = new KeyMapping("key.fishingmadebetter.open_config", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, CATEGORY);
    private static ReelingInput lastInput = ReelingInput.NONE;

    private ReelingKeyMappings() {
    }

    public static void clientTick(Consumer<ReelingInput> sender) {
        Minecraft minecraft = Minecraft.getInstance();
        ReelingInput input = currentInput();
        if (!(minecraft.player != null && minecraft.player.fishing instanceof FmbFishingHook)) {
            input = ReelingInput.NONE;
        }
        if (input != lastInput || input != ReelingInput.NONE) {
            sender.accept(input);
            lastInput = input;
        }
    }

    private static ReelingInput currentInput() {
        if (REEL_IN.isDown()) {
            return ReelingInput.REEL_IN;
        }
        if (REEL_OUT.isDown()) {
            return ReelingInput.REEL_OUT;
        }
        return ReelingInput.NONE;
    }
}
