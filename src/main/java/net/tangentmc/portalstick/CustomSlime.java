package net.tangentmc.portalstick;

import net.minecraft.server.v1_14_R1.*;

public class CustomSlime extends EntitySlime {
    public CustomSlime(EntityTypes<? extends EntitySlime> entitytypes, World world) {
        super(EntityTypes.SLIME, world);
        this.setInvulnerable(true);
    }

    public void tick() {
        this.lastX = this.locX;
        this.lastY = this.locY;
        this.lastZ = this.locZ;
        if (!this.isNoGravity()) {
            this.setMot(this.getMot().add(0.0D, -0.04D, 0.0D));
        }
        this.move(EnumMoveType.SELF, this.getMot());
        this.setMot(this.getMot().a(0.4D));
    }

    @Override
    protected float b(EntityPose entitypose, EntitySize entitysize) {
        return 0;
    }

}
