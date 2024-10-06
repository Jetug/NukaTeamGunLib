package com.nukateam.ntgl.client.model.gibs;

import com.nukateam.ntgl.ClientProxy;
import dev.kosmx.playerAnim.mixin.BipedEntityModelMixin;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.Entity;

import static com.nukateam.ntgl.ClientProxy.*;

public class ModelGibsBiped extends ModelGibs
{
	public HumanoidModel model;
    
    public ModelGibsBiped(HumanoidModel model) {
		super();
		this.model = model;
    	this.model.head.setRotation(0.0f,0.0f,0.0f);
    	this.model.body.setRotation(0.0f,0.0f,0.0f);
    	this.model.rightArm.setRotation(0.0f,0.0f,0.0f);
    	this.model.leftArm.setRotation(0.0f,0.0f,0.0f);
    	this.model.rightLeg.setRotation(0.0f,0.0f,0.0f);
    	this.model.leftLeg.setRotation(0.0f,0.0f,0.0f);
    }

    /**
     * Sets the models various rotation angles then renders the model.
     */
    public void render(Entity entityIn, float scale, int part)
    {
		var s = (HumanoidModel)(Object)getEntityRenderer(entityIn);
		
        switch(part) {
	        case 0:
	        	this.model.head.render(scale);
	        	break;
	        case 1:
	        	this.model.body.render(scale);
	        	break;
	        case 2:
	        	this.model.rightArm.render(scale);
	        	break;
	        case 3:
	        	this.model.leftArm.render(scale);
	        	break;
	        case 4:
	        	this.model.rightLeg.render(scale);
	        	break;
	        case 5:
	        	this.model.leftLeg.render(scale);
	        	break;
        }
    }

	@Override
	public int getNumGibs() {
		return 6;
	}
}