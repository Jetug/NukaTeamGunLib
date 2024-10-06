package com.nukateam.ntgl.client.particle;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import techguns.capabilities.TGExtendedPlayerClient;
import techguns.capabilities.TGShooterValues;

import java.util.ArrayList;
import java.util.List;

public class TGParticleSystemItemAttached extends TGParticleSystem {

	protected EnumHand hand;
	
	protected LivingEntity elb;
	protected EntityPlayer ply;
	
	public TGParticleSystemItemAttached(Entity entity, EnumHand hand, TGParticleSystemType type) {
		super(entity, type);
		this.hand=hand;
		if(this.entity instanceof EntityPlayer) {
			this.ply = (EntityPlayer) this.entity;
			this.elb=null;
		} else {
			this.elb= (LivingEntity) this.entity;
			this.ply=null;
		}
		this.itemAttached=true;
		float s = 1.0f; // + (Keybinds.X*10f);
		this.posX= type.offset.x; //* s + Keybinds.X;
		this.posY= type.offset.y; //* s + Keybinds.Y;
		this.posZ= type.offset.z; // * s + Keybinds.Z;
		this.scale=s;
	}

	@Override
	protected void addEffect(ITGParticle s) {
		List<ITGParticle> list = new ArrayList<>();
		s.setItemAttached();
		list.add(s);
		if(this.ply!=null) {
			TGExtendedPlayerClient props = TGExtendedPlayerClient.get(this.ply);
			props.addEffectHand(hand, list);
		} else if (this.elb!=null) {
			TGShooterValues props = TGShooterValues.get(this.elb);
			props.addEffectHand(hand, list);
		}
	}
	
	
}
