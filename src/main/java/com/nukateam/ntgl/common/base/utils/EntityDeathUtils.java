
package com.nukateam.ntgl.common.base.utils;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.animal.horse.Donkey;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.animal.horse.Mule;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Server and client side, needed by server to know to send out packets
 *
 */
public class EntityDeathUtils {
	public static HashMap<DeathType, HashSet<Class<? extends LivingEntity>>> entityDeathTypes;

	public static HashSet<Class<? extends LivingEntity>> goreMap = new HashSet<>();
	static {
		entityDeathTypes = new HashMap<>();
		//Gore
		
		goreMap.add(Player.class);
		goreMap.add(Zombie.class);
		goreMap.add(Skeleton.class);
		goreMap.add(EnderMan.class);
		goreMap.add(Creeper.class);
		goreMap.add(Cow.class);
		goreMap.add(Sheep.class);
		goreMap.add(Pig.class);
		goreMap.add(Chicken.class);
		goreMap.add(ZombifiedPiglin.class);
		goreMap.add(Spider.class);
		goreMap.add(CaveSpider.class);
		goreMap.add(Witch.class);
		goreMap.add(Slime.class);
		goreMap.add(Horse.class);
		goreMap.add(MushroomCow.class);
		goreMap.add(Wolf.class);
		goreMap.add(Squid.class);
		goreMap.add(Ghast.class);
		goreMap.add(Villager.class);
		
		goreMap.add(Llama.class);
		goreMap.add(Evoker.class);
		goreMap.add(Husk.class);
		goreMap.add(PolarBear.class);
		goreMap.add(MagmaCube.class);
		goreMap.add(Parrot.class);
		goreMap.add(Rabbit.class);
		goreMap.add(Stray.class);
		goreMap.add(Silverfish.class);
		goreMap.add(Vindicator.class);
		goreMap.add(Vex.class);
		goreMap.add(Shulker.class);
		goreMap.add(WitherSkeleton.class);
		goreMap.add(Ghast.class);
		goreMap.add(ZombieVillager.class);
		goreMap.add(Horse.class);
		goreMap.add(Donkey.class);
		goreMap.add(Mule.class);
		
		
		entityDeathTypes.put(DeathType.GORE, goreMap);
	}
	
	/**
	 * Add an entity to the gore death type list
	 * @param clazz
	 */
	public static void addEntityToDeathEffectList(Class<? extends LivingEntity> clazz) {
		entityDeathTypes.get(DeathType.GORE).add(clazz);
	}
	
	public static boolean hasSpecialDeathAnim(LivingEntity entityLiving, DeathType deathtype) {

		//TEST CODE:
		if (deathtype == DeathType.BIO || deathtype == DeathType.LASER) return true;
		
		//GenericGore
		if (entityDeathTypes.get(DeathType.GORE).contains(entityLiving.getClass())){
			return true;
		}
		
		return false;
		

	}
	
    public enum DeathType {
    	DEFAULT(0), GORE(1), BIO(2), LASER(3);
    	
    	int value;
    	
    	private DeathType(int value) {
    		this.value = value;
    	}
    	
    	public int getValue() {
    		return value;
    	}
    }
}
