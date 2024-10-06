package com.nukateam.ntgl.client.model.gibs;


import net.minecraft.client.model.VillagerModel;
import net.minecraft.world.entity.Entity;

public class ModelGibsVillager extends ModelGibs {

    public ModelGibsVillager(ModelVillager model) {
        this.model = model;
        this.model.villagerHead.setRotationPoint(0.0f, 0.0f, 0.0f);
        this.model.villagerBody.setRotationPoint(0.0f, 0.0f, 0.0f);
        this.model.villagerArms.setRotationPoint(0.0f, 0.0f, 0.0f);
        this.model.rightVillagerLeg.setRotationPoint(0.0f, 0.0f, 0.0f);
        this.model.leftVillagerLeg.setRotationPoint(0.0f, 0.0f, 0.0f);
    }


    @Override
    public void render(Entity entityIn, float scale, int part) {

        switch (part) {
            case 0:
                this.model.villagerHead.render(scale);
                break;
            case 1:
                this.model.villagerBody.render(scale);
                break;
            case 2:
                this.model.rightVillagerLeg.render(scale);
                break;
            case 3:
                this.model.leftVillagerLeg.render(scale);
                break;
            case 4:
                this.model.villagerArms.render(scale);
                break;
        }
    }


    @Override
    public int getNumGibs() {
        return 5;
    }
}
