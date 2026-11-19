package net.theawesomegem.fishingmadebetter.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.theawesomegem.fishingmadebetter.Constants;

public final class ModSounds {
    private static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, Constants.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> WHALE_AMBIENT =
            sound("entity.whale.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> WHALE_ANGRY =
            sound("entity.whale.angry");
    public static final DeferredHolder<SoundEvent, SoundEvent> WHALE_HURT =
            sound("entity.whale.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> WHALE_DEATH =
            sound("entity.whale.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> EEL_RELEASE =
            sound("electric_eel_release");
    public static final DeferredHolder<SoundEvent, SoundEvent> EEL_SWING =
            sound("electric_eel_whip");

    private static DeferredHolder<SoundEvent, SoundEvent> sound(String name) {
        return SOUNDS.register(
                name,
                () ->
                        SoundEvent.createVariableRangeEvent(
                                Identifier.fromNamespaceAndPath(Constants.MOD_ID, name)));
    }

    public static void register(IEventBus bus) {
        SOUNDS.register(bus);
    }
}
