package com.nukateam.ntgl.client.particle;


import com.mojang.blaze3d.vertex.BufferBuilder;
import dev.kosmx.playerAnim.core.util.Vec3d;
import net.minecraft.world.entity.Entity;

public interface ITGParticle {
	public Vec3d getPos();
	
	public boolean shouldRemove();
	public void updateTick();
	
	public void doRender(BufferBuilder buffer, Entity entityIn, float partialTickTime, float rotX, float rotZ, float rotYZ, float rotXY, float rotXZ);
	   
	public AxisAlignedBB getRenderBoundingBox(float ptt, Entity viewEnt);
	
	public default boolean doNotSort() {
		return false;
	}
	
	public double getDepth();
	
	public void setDepth(double depth);
	public void setItemAttached();
}
