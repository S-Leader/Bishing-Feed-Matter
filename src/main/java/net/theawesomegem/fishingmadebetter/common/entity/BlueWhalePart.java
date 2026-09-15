package net.theawesomegem.fishingmadebetter.common.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.entity.PartEntity;

import javax.annotation.Nullable;

public final class BlueWhalePart extends PartEntity<BlueWhaleEntity> {
    private final EntityDimensions dimensions;

    public BlueWhalePart(BlueWhaleEntity parent, float width, float height) {
        super(parent);
        this.dimensions = EntityDimensions.scalable(width, height);
        refreshDimensions();
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return dimensions;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean isPushable() {
        // 只有搁浅的鲸鱼推得动；泡在水里那几十吨不该被谁顶着走。
        return getParent().isBeached();
    }

    @Override
    public void push(double x, double y, double z) {
        getParent().pushFromBodyPart(x, z);
    }

    @Override
    public boolean is(Entity entity) {
        return entity == this || entity == getParent();
    }

    @Nullable
    @Override
    public ItemStack getPickResult() {
        return getParent().getPickResult();
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        return getParent().interact(player, hand);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isInvulnerableTo(source)) {
            return false;
        }
        boolean hurt = getParent().hurtFromPart(this, source, amount);
        if (hurt && !level().isClientSide && source.getDirectEntity() instanceof AbstractArrow
                && !(source.getDirectEntity() instanceof ThrownTrident)) {
            getParent().setArrowCount(getParent().getArrowCount() + 1);
        }
        return hurt;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        throw new UnsupportedOperationException("Whale body parts are sent with their parent");
    }
}
