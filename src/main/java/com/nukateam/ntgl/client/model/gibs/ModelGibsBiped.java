package com.nukateam.ntgl.client.model.gibs;

import dev.kosmx.playerAnim.mixin.BipedEntityModelMixin;
import net.minecraft.client.model.HumanoidModel;

public class ModelGibsBiped extends ModelGibs
{
	public HumanoidModel model;
    
    public ModelGibsBiped(HumanoidModel model)
    {
    	this.model = model;
    	this.model.bipedHead.setRotationPoint(0.0f,0.0f,0.0f);
    	this.model.bipedBody.setRotationPoint(0.0f,0.0f,0.0f);
    	this.model.bipedRightArm.setRotationPoint(0.0f,0.0f,0.0f);
    	this.model.bipedLeftArm.setRotationPoint(0.0f,0.0f,0.0f);
    	this.model.bipedRightLeg.setRotationPoint(0.0f,0.0f,0.0f);
    	this.model.bipedLeftLeg.setRotationPoint(0.0f,0.0f,0.0f);   	
    }

    /**
     * Sets the models various rotation angles then renders the model.
     */
    public void render(Entity entityIn, float scale, int part)
    {

        switch(part) {
	        case 0:
	        	this.model.bipedHead.render(scale);
	        	break;
	        case 1:
	        	this.model.bipedBody.render(scale);
	        	break;
	        case 2:
	        	this.model.bipedRightArm.render(scale);
	        	break;
	        case 3:
	        	this.model.bipedLeftArm.render(scale);
	        	break;
	        case 4:
	        	this.model.bipedRightLeg.render(scale);
	        	break;
	        case 5:
	        	this.model.bipedLeftLeg.render(scale);
	        	break;
        }
    }

	@Override
	public int getNumGibs() {
		return 6;
	}
}