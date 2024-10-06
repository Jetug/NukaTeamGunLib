package com.nukateam.ntgl.client.particle;

import net.minecraft.entity.Entity;
import net.minecraft.util.EnumHand;
import net.minecraft.world.Level;

import java.util.ArrayList;
import java.util.List;

public class TGParticleListType extends TGFXType {
	ArrayList<ParticleSystemEntry> particleSystems = new ArrayList<ParticleSystemEntry>();

	
	
	public TGParticleListType() {
		isList = true;
	}

	public void addParticleSystem(String particleSystem) {
		particleSystems.add(new ParticleSystemEntry(particleSystem));
	}
	
	class ParticleSystemEntry {
		//ParticleSystemType type; //Actually is just a string
		String particleSystem;
		//TODO: Delay, Offset, Scale, etc.

		public ParticleSystemEntry(String particleSystem) {
			super();
			this.particleSystem = particleSystem;
		}
	}

	
	@Override
	public List<TGParticleSystem> createParticleSystems(Level world, double posX, double posY, double posZ, double xo, double yo, double zo) {
		ArrayList<TGParticleSystem> list = new ArrayList<TGParticleSystem>();
		for (ParticleSystemEntry system : particleSystems) {		
			if (TGFX.FXList.containsKey(system.particleSystem)) {
				TGFXType fxtype = TGFX.FXList.get(system.particleSystem);
				list.addAll(fxtype.createParticleSystems(world, posX, posY, posZ, xo, yo, zo));
			}
		}
		return list;
	}

	@Override
	public List<TGParticleSystem> createParticleSystemsOnEntity(Entity ent) {
		ArrayList<TGParticleSystem> list = new ArrayList<TGParticleSystem>();
		for (ParticleSystemEntry system : particleSystems) {		
			if (TGFX.FXList.containsKey(system.particleSystem)) {
				TGFXType fxtype = TGFX.FXList.get(system.particleSystem);
				list.addAll(fxtype.createParticleSystemsOnEntity(ent));
			}
		}
		return list;
	}
	
	@Override
	public List<TGParticleSystem> createParticleSystemsOnEntityItemAttached(Entity ent, EnumHand hand) {
		ArrayList<TGParticleSystem> list = new ArrayList<TGParticleSystem>();
		for (ParticleSystemEntry system : particleSystems) {		
			if (TGFX.FXList.containsKey(system.particleSystem)) {
				TGFXType fxtype = TGFX.FXList.get(system.particleSystem);
				list.addAll(fxtype.createParticleSystemsOnEntityItemAttached(ent, hand));
			}
		}
		return list;
	}

	@Override
	public List<TGParticleSystem> createParticleSystemsOnParticle(Level worldIn, TGParticle ent) {
		ArrayList<TGParticleSystem> list = new ArrayList<TGParticleSystem>();
		for (ParticleSystemEntry system : particleSystems) {		
			if (TGFX.FXList.containsKey(system.particleSystem)) {
				TGFXType fxtype = TGFX.FXList.get(system.particleSystem);
				list.addAll(fxtype.createParticleSystemsOnParticle(worldIn, ent));
			}
		}
		return list;
	}
}
