package net.theawesomegem.fishingmadebetter.client.renderer;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;

public final class WhaleRenderState extends LivingEntityRenderState {
    public boolean beached;
    public float visualPitch;
    public final AnimationState beachedAnimationState = new AnimationState();
    public final AnimationState swimAnimationState = new AnimationState();
    public final AnimationState ramAnimationState = new AnimationState();
    public final AnimationState blowAnimationState = new AnimationState();
}
