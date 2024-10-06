package com.nukateam.ntgl.client.particle;

import net.minecraft.entity.Entity;
import net.minecraft.util.EnumHand;
import net.minecraft.world.Level;

import java.util.List;

public abstract class TGFXType {
	public String name;
	boolean isList = false;
	
	public abstract List<TGParticleSystem> createParticleSystems(Level world, double posX, double posY, double posZ, double xo, double yo, double zo);
	public abstract List<TGParticleSystem> createParticleSystemsOnEntity(Entity ent);
	public abstract List<TGParticleSystem> createParticleSystemsOnParticle(Level worldIn, TGParticle part);
	public abstract List<TGParticleSystem> createParticleSystemsOnEntityItemAttached(Entity ent, EnumHand hand);
}
