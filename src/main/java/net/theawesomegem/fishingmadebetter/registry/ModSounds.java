package net.theawesomegem.fishingmadebetter.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.theawesomegem.fishingmadebetter.Constants;

public final class ModSounds {
    private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Constants.MOD_ID);

    public static final RegistryObject<SoundEvent> WHALE_AMBIENT = sound("entity.whale.ambient");
    public static final RegistryObject<SoundEvent> WHALE_ANGRY = sound("entity.whale.angry");
    public static final RegistryObject<SoundEvent> WHALE_HURT = sound("entity.whale.hurt");
    public static final RegistryObject<SoundEvent> WHALE_DEATH = sound("entity.whale.death");
    public static final RegistryObject<SoundEvent> EEL_RELEASE = sound("electric_eel_release");
    public static final RegistryObject<SoundEvent> EEL_SWING = sound("electric_eel_whip");

    private ModSounds() {
    }

    private static RegistryObject<SoundEvent> sound(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Constants.MOD_ID, name)));
    }

    public static void register(IEventBus bus) {
        SOUNDS.register(bus);
    }
}
