package net.tangentmc.portalstick;

import net.minecraft.server.v1_14_R1.*;

public class CustomFallingBlock extends EntityFallingBlock {
    public CustomFallingBlock(EntityTypes<? extends EntityFallingBlock> entitytypes, World world) {
        super(entitytypes, world);
    }

    public CustomFallingBlock(World world, double d0, double d1, double d2, IBlockData iblockdata) {
        super(world, d0, d1, d2, iblockdata);
        this.noclip = false;
        this.updateSize();
    }

    public void tick() {
        this.lastX = this.locX;
        this.lastY = this.locY;
        this.lastZ = this.locZ;
        if (!this.isNoGravity()) {
            this.setMot(this.getMot().add(0.0D, -0.04D, 0.0D));
        }

        this.move(EnumMoveType.SELF, this.getMot());
    }

    @Override
    public void collide(Entity entity) {
        System.out.println("COLLIDE!");
    }
}
