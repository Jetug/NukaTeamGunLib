package com.nukateam.ntgl.common.base.utils;

public enum DeathType {
	DEFAULT(0), GORE(1), FIRE(2), LASER(3);

	final int value;

	DeathType(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static DeathType getById(int id){
		for(var val : DeathType.values()){
			if(val.getValue() == id){
				return val;
			}
		}
		return DEFAULT;
	}
}
