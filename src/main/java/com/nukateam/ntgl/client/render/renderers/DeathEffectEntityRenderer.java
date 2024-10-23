package com.nukateam.ntgl.client.render.renderers;

import com.nukateam.ntgl.common.data.interfaces.IModelAccessor;
import com.nukateam.ntgl.common.data.util.MathUtil;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Random;

public class DeathEffectEntityRenderer {
    private static final ResourceLocation RES_ITEM_GLINT = new ResourceLocation("textures/misc/enchanted_item_glint.png");
    private static final ResourceLocation RES_BIO_EFFECT = new ResourceLocation(Techguns.MODID, "textures/fx/bio.png");
    private static final ResourceLocation RES_LASER_EFFECT = new ResourceLocation(Techguns.MODID, "textures/fx/laserdeath.png");
    private static final int MAX_DEATH_TIME = 20;

    public static Field RLB_mainModel = ReflectionHelper.findField(RenderLivingBase.class, "mainModel", "field_77045_g");
    protected static Method RLB_preRenderCallback = ReflectionHelper.findMethod(RenderLivingBase.class, "preRenderCallback", "func_77041_b", EntityLivingBase.class, float.class);
    protected static Method R_bindEntityTexture = ReflectionHelper.findMethod(Render.class, "bindEntityTexture", "func_180548_c", Entity.class);

    public static Field R_renderManager = ReflectionHelper.findField(Render.class, "renderManager", "field_76990_c");

    protected static Method R_bindTexture = ReflectionHelper.findMethod(Render.class, "bindTexture", "func_110776_a", ResourceLocation.class);
    protected static Method RLB_getColorMultiplier = ReflectionHelper.findMethod(RenderLivingBase.class, "getColorMultiplier", "func_77030_a", EntityLivingBase.class, float.class, float.class);

    public static void preRenderCallback(RenderLivingBase<? extends EntityLivingBase> renderer, EntityLivingBase elb, float ptt) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        RLB_preRenderCallback.invoke(renderer, elb, ptt);
    }

    public static void bindEntityTexture(Render<? extends Entity> renderer, Entity entity) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        R_bindEntityTexture.invoke(renderer, entity);
    }

    public static void setRenderScalingForEntity(EntityLivingBase elb) {
        if (elb instanceof EntitySlime) {
            EntitySlime slime = (EntitySlime) elb;
            int size = slime.getSlimeSize();
            GlStateManager.scale((float) size, (float) size, (float) size);

            //slimes are 1,2 and 4
            if (size == 2) {
                /**
                 * Actually renders the given argument. This is a synthetic bridge method, always casting down its argument and then
                 * handing it off to a worker function which does the actual work. In all probabilty, the class Render is generic
                 * (Render<T extends Entity) and this method has signature public void func_76986_a(T entity, double d, double d1,
                 * double d2, float f, float f1). But JAD is pre 1.5 so doesn't do that.
                 */
                public static void doRender(RenderLivingBase renderer, EntityLivingBase entity, double x, double y, double z, float ptt, DeathType deathType) {
                    GlStateManager.pushMatrix();
                    GlStateManager.disableCull();
                    ModelBase mainModel = null;

                    try {
                        mainModel = (ModelBase) RLB_mainModel.get(renderer);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mainModel.isChild = entity.isChild();

                    try {
                        float f2 = MathUtil.interpolateRotation(entity.prevRenderYawOffset, entity.renderYawOffset, ptt);
                        float f3 = MathUtil.interpolateRotation(entity.prevRotationYawHead, entity.rotationYawHead, ptt);
                        float f4;
                        float f13 = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * ptt;

                        GlStateManager.translate((float) x, (float) y, (float) z);

                        f4 = (float) entity.ticksExisted + ptt;

                        float f5 = 0.0625F;
                        GlStateManager.enableRescaleNormal();
                        GlStateManager.scale(-1.0f, -1.0f, -1.0f);
                        GlStateManager.translate(0.0F, -24.0F * f5 - 0.0078125F, 0.0F);

                        float f6 = entity.prevLimbSwingAmount + (entity.limbSwingAmount - entity.prevLimbSwingAmount) * ptt;
                        float f7 = entity.limbSwing - entity.limbSwingAmount * (1.0F - ptt);

                        if (entity.isChild())
                            f7 *= 3.0F;

                        if (f6 > 1.0F)
                            f6 = 1.0F;

                        GlStateManager.enableAlpha();

                        switch (deathType) {
                            case BIO:
                                mainModel.setLivingAnimations(entity, f7, f6, ptt);
                                preRenderCallback(renderer, entity, ptt);
                                renderModelDeathBio(renderer, entity, f7, f6, f4, f3 - f2, f13, f5);
                                break;
                            case LASER:
                                mainModel.setLivingAnimations(entity, f7, f6, ptt);
                                preRenderCallback(renderer, entity, ptt);
                                renderModelDeathLaser(renderer, entity, f7, f6, f4, f3 - f2, f13, f5);
                                break;
                            default:
                                break;
                        }

                        GlStateManager.disableRescaleNormal();

                    } catch (Exception ignored) {
                    }


                    GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
                    GlStateManager.enableTexture2D();
                    GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
                    GlStateManager.enableCull();
                    GlStateManager.popMatrix();
                }GlStateManager.translate(0, -0.8f, 0);
            } else if (size == 4) {
                GlStateManager.translate(0, -1.2f, 0);
            }
        }

    }

    /**
     * Renders the model in RenderLiving
     */
    static void renderModelDeathBio(LivingEntityRenderer renderer, EntityLivingBase entity, float f7, float f6, float f4, float p_77036_5_, float f13, float f5) {
        float prog = ((float) entity.deathTime / (float) MAX_DEATH_TIME);

        Random rand = new Random(entity.getEntityId());


        RenderType renderType = RenderType.ADDITIVE;
        var mainModel = renderer.getModel();
        RenderManager renderManager = null;
        try {

            mainModel = (ModelBase) RLB_mainModel.get(renderer);
            renderManager = (RenderManager) R_renderManager.get(renderer);
            R_bindEntityTexture.invoke(renderer, entity);

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (mainModel instanceof ModelBiped) {
            mainModel.setRotationAngles(f7, f6, f4, p_77036_5_, f13, f5, entity);
        }

//        HashSet<ModelRenderer> childBoxes = new HashSet<>(64);
//        for (Object o : mainModel.boxList) {
//            ModelRenderer box = (ModelRenderer) o;
//            if (box.childModels != null) {
//                childBoxes.addAll(box.childModels);
//            }
//        }

        var accessor = (IModelAccessor)mainModel;
        var childBoxes = accessor.getModelParts();


        GlStateManager.pushMatrix();
        GlStateManager.rotate(entity.rotationYaw, 0, 1, 0);

        setRenderScalingForEntity(entity);

        for (Object o : mainModel.boxList) {
            ModelRenderer box = (ModelRenderer) o;
            if (!childBoxes.contains(box) && !box.isHidden && box.showModel) {
                float scale = 1.0f + (rand.nextFloat() * prog);
                GlStateManager.pushMatrix();
                GlStateManager.translate(-box.offsetX, -box.offsetY, -box.offsetZ);
                GlStateManager.scale(scale, scale, scale);
                GlStateManager.translate(box.offsetX, box.offsetY, box.offsetZ);
                double mainColor = 1.0 - Math.pow(prog, 2.0);
                double mainAlpha = Math.pow(1.0 - prog, 2.0);
                GlStateManager.color((float) mainColor, 1.0f, (float) mainColor, (float) mainAlpha);
                box.render(f5);
                renderManager.renderEngine.bindTexture(RES_BIO_EFFECT);
                TGRenderHelper.enableBlendMode(renderType);
                double overlayColor = 0.5 + (Math.sin((Math.sqrt(prog) + 0.75) * 2.0 * Math.PI) / 2);
                GlStateManager.color((float) overlayColor, (float) overlayColor, (float) overlayColor);
                box.render(f5);
                TGRenderHelper.disableBlendMode(renderType);
                GlStateManager.popMatrix();
            }
        }

        GlStateManager.popMatrix();
    }

    /**
     * Renders the model in RenderLiving
     */
    static void renderModelDeathLaser(RenderLivingBase renderer, EntityLivingBase entity, float f7, float f6, float f4, float p_77036_5_, float f13, float f5) {
        float prog = ((float) entity.deathTime / (float) MAX_DEATH_TIME);

        Random rand = new Random(entity.getEntityId());
        //ResourceLocation texture = RES_BIO_EFFECT;
        RenderType renderType = RenderType.ADDITIVE;
        ModelBase mainModel = null;
        //ModelBase renderPassModel;
        RenderManager renderManager = null;
        try {
            mainModel = (ModelBase) RLB_mainModel.get(renderer);
            renderManager = (RenderManager) R_renderManager.get(renderer);
            //renderPassModel = (ModelBase)R_renderPassModel.get(renderer);
            R_bindEntityTexture.invoke(renderer, entity);

        } catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println("BoxList: "+mainModel.boxList.size());
        //1st: Entity Texture
        //mainModel.render(entity, f7, f6, f4, p_77036_5_, f13, f5);

        if (mainModel instanceof ModelBiped) {
            ((ModelBiped) mainModel).setRotationAngles(f7, f6, f4, p_77036_5_, f13, f5, entity);
        }

//    	HashSet<ModelRenderer> childBoxes = new HashSet<ModelRenderer>(64);
//        for (Object o : mainModel.boxList) {
//        	ModelRenderer box = (ModelRenderer)o;
//        	if (box.childModels != null) {
//        		childBoxes.addAll(box.childModels);
//        	}
//        }

        GlStateManager.pushMatrix();
        GlStateManager.rotate(entity.rotationYaw, 0, 1, 0);

        setRenderScalingForEntity(entity);

//        for (Object o : mainModel.boxList) {
//        	ModelRenderer box = (ModelRenderer)o;
//        	if (!childBoxes.contains(box) && !box.isHidden && box.showModel) {
//        		float scale = 1.0f-(rand.nextFloat()*prog);
//        		GL11.glPushMatrix();		
//        		GL11.glTranslatef(-box.offsetX, -box.offsetY, -box.offsetZ);
//        		GL11.glScalef(scale, scale, scale);
//        		GL11.glTranslatef(box.offsetX, box.offsetY, box.offsetZ);
//        		double mainColor = 1.0-Math.pow(prog, 2.0);
//        		double mainAlpha = Math.pow(1.0-prog, 2.0);
//        		GL11.glColor4d(1.0,mainColor, mainColor, mainAlpha);
//        		box.render(f5);
//        		renderManager.renderEngine.bindTexture(RES_LASER_EFFECT);
//            	TGRenderHelper.enableBlendMode(renderType);
//            	double overlayColor = 0.5+(Math.sin((Math.sqrt(prog)+0.75)*2.0*Math.PI)/2);
//            	GL11.glColor3d(overlayColor, overlayColor, overlayColor);
//        		box.render(f5);
//                TGRenderHelper.disableBlendMode(renderType);
//
//        		GL11.glPopMatrix();
//        	}
//        }

        //  GlStateManager.pushMatrix();
        double mainColor = 1.0 - Math.pow(prog, 2.0);
        double mainAlpha = Math.pow(1.0 - prog, 2.0);
        GlStateManager.color(1.0f, (float) mainColor, (float) mainColor, (float) mainAlpha);
        mainModel.render(entity, f7, f6, f4, p_77036_5_, f13, f5);
        renderManager.renderEngine.bindTexture(RES_LASER_EFFECT);
        TGRenderHelper.enableBlendMode(renderType);
        double overlayColor = 0.5 + (Math.sin((Math.sqrt(prog) + 0.75) * 2.0 * Math.PI) / 2);
        GlStateManager.color((float) overlayColor, (float) overlayColor, (float) overlayColor);
        mainModel.render(entity, f7, f6, f4, p_77036_5_, f13, f5);
        TGRenderHelper.disableBlendMode(renderType);

        //GlStateManager.popMatrix();

        GlStateManager.popMatrix();

    }


    /**
     * Renders the model in RenderLiving
     */
    
    /*
    static void renderModelDeathDismember(RenderLivingBase renderer, EntityLivingBase entity, float f7, float f6, float f4, float p_77036_5_, float f13, float f5, float ptt)
    {
    	float prog = (((float)entity.deathTime)-ptt) / (float)MAX_DEATH_TIME; 
    	
    	Random rand = new Random(entity.getEntityId());
    	ResourceLocation texture = RES_BIO_EFFECT;
    	RenderType renderType = RenderType.ADDITIVE;
    	ModelBase mainModel = null;
    	ModelBase renderPassModel;
    	RenderManager renderManager = null;
    	try {
    		mainModel = (ModelBase)R_mainModel.get(renderer);
    		renderManager = (RenderManager)R_renderManager.get(renderer);
            renderPassModel = (ModelBase)R_renderPassModel.get(renderer);
           	R_bindEntityTexture.invoke(renderer, entity);
        	
        }catch (Exception e) {
        	e.printStackTrace();
        }
    	
    	GL11.glPushMatrix();
	    GL11.glRotated(entity.rotationYaw, 0, 1, 0);	                
	    setRenderScalingForEntity(entity);
	      
	    
    	if (mainModel instanceof ModelBiped) {
    		GL11.glPushMatrix();
    		GLTransformDeath_Body(prog, entity, 1, 0, 0);
    		
    		ModelBiped modelBiped = (ModelBiped)mainModel;
    		modelBiped.setRotationAngles(f7, f6, f4, p_77036_5_, f13, f5, entity);
    		
    		modelBiped.bipedBody.render(f5);
    		//modelBiped.bipedHead.render(f5);
    		modelBiped.bipedLeftArm.render(f5);
    		modelBiped.bipedRightArm.render(f5);
    		modelBiped.bipedLeftLeg.render(f5);
    		modelBiped.bipedRightLeg.render(f5);
    		
    		//biped_renderEquippedItems(renderer, renderManager, modelBiped, (EntityLiving)entity, ptt);
    		GL11.glPopMatrix();
    		
    		GL11.glPushMatrix();
    		GLTransformDeath_Head(prog, entity, rand, modelBiped.bipedHead);
    		modelBiped.bipedHead.render(f5);
    		GL11.glPopMatrix();
    		
    	}else if (mainModel instanceof ModelQuadruped) {
    		GL11.glPushMatrix();
    		GLTransformDeath_Body(prog, entity, 0, 0, 1);
    		
    		ModelQuadruped modelQ = (ModelQuadruped)mainModel;
    		modelQ.setRotationAngles(f7, f6, f4, p_77036_5_, f13, f5, entity);

    		//modelQ.head.render(f5);
    		modelQ.body.render(f5);
    		modelQ.leg1.render(f5);
    		modelQ.leg2.render(f5);
    		modelQ.leg3.render(f5);
    		modelQ.leg4.render(f5);

    		GL11.glPopMatrix();
    		GL11.glPushMatrix();
    		GLTransformDeath_Head(prog, entity, rand, modelQ.head);
    		modelQ.head.render(f5);
    		GL11.glPopMatrix();
    		
    	}else {
    	
	    	HashSet<ModelRenderer> childBoxes = new HashSet<ModelRenderer>(64);
	        for (Object o : mainModel.boxList) {
	        	ModelRenderer box = (ModelRenderer)o;
	        	if (box.childModels != null) {
	        		childBoxes.addAll(box.childModels);
	        	}
	        }             
	        
	        System.out.println("boxes.size = "+ childBoxes.size());
	        for (Object o : mainModel.boxList) {
	        	ModelRenderer box = (ModelRenderer)o;
	        	if (!childBoxes.contains(box) && !box.isHidden && box.showModel) {
	
	        		//if (rand.nextDouble() > 0.25) {
	        			box.render(f5);
	        		//}
	        			
	        			
	        	}
	        }
	        
    	}
        GL11.glPopMatrix();
        
        //2nd: Render Overlay texture
//        renderManager.renderEngine.bindTexture(RES_BIO_EFFECT);
//    	TGRenderHelper.enableBlendMode(renderType);
//        mainModel.render(entity, f7, f6, f4, p_77036_5_, f13, f5);      
//        TGRenderHelper.disableBlendMode(renderType);
    }
    */
    static void GLTransformDeath_Body(float prog, EntityLivingBase entity, double xdir, double ydir, double zdir) {
        double p = Math.min(1.0, prog * 2.0);
        double y = Math.sin(p * Math.PI);//(1.0-Math.cos(p*2.0*Math.PI))*0.5;
        double y2 = (1.0 - Math.cos(p * Math.PI)) * 0.5;
        GlStateManager.translate(0, +entity.height * 0.75, 0);
        GlStateManager.rotate(-90.0f * (float) y2, (float) xdir, (float) ydir, (float) zdir);
        GlStateManager.translate(0, -entity.height * 0.75, 0);
        GlStateManager.translate(0, (0.25 * +y), 0);
    }

    static void GLTransformDeath_Head(float prog, EntityLivingBase entity, Random rand, ModelRenderer head) {
        double p = Math.min(1.0, prog * 2.0);
        double a = Math.sin(p * Math.PI);//(1.0-Math.cos(p*2.0*Math.PI))*0.5;
        double a2 = (1.0 - Math.cos(p * Math.PI)) * 0.5;

        //Get Head Center
        double x1 = -1, x2 = -1;
        double y1 = -1, y2 = -1;
        double z1 = -1, z2 = -1;
        for (Object b : head.cubeList) {
            ModelBox box = ((ModelBox) b);
            if (x1 == -1 || box.posX1 < x1) x1 = box.posX1;
            if (x2 == -1 || box.posX2 > x2) x2 = box.posX2;
            if (y1 == -1 || box.posY1 < y1) y1 = box.posY1;
            if (y2 == -1 || box.posY2 > y2) y2 = box.posY2;
            if (z1 == -1 || box.posZ1 < z1) z1 = box.posZ1;
            if (z2 == -1 || box.posZ2 > z2) z2 = box.posZ2;
        }
        double d = 0.0625;
        double offsetX = /*(head.offsetX+*/(x1 + (x2 - x1) * 0.5) * d;
        double offsetY = /*(head.offsetY+*/(y1 + (y2 - y1) * 0.5) * d;
        double offsetZ = /*(head.offsetZ+*/(z1 + (z2 - z1) * 0.5) * d;

        //System.out.println("offsets: "+offsetX+" / "+offsetY+" / " + offsetZ);
        //GL11.glTranslated(a2, (2.5*a), a2);

        GlStateManager.translate(offsetX, offsetY, offsetZ);
        GlStateManager.rotate(360.0f * prog, rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
        GlStateManager.translate(-offsetX, -offsetY, -offsetZ);

    }

    //---------------


    /**
     * Renders the model in RenderLiving
     */
    static void renderModel(RenderLivingBase renderer, EntityLivingBase entity, float f7, float f6, float f4, float p_77036_5_, float f13, float f5, ResourceLocation texture, RenderType renderType) {

        ModelBase mainModel = null;
        ModelBase renderPassModel;
        RenderManager renderManager = null;
        try {
            mainModel = (ModelBase) RLB_mainModel.get(renderer);
            renderManager = (RenderManager) R_renderManager.get(renderer);
//            renderPassModel = (ModelBase)RLB_renderPassModel.get(renderer);

            if (texture != null) {
                renderManager.renderEngine.bindTexture(RES_BIO_EFFECT);
            } else {
                R_bindEntityTexture.invoke(renderer, entity);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        TGRenderHelper.enableBlendMode(renderType);

        if (!entity.isInvisible()) {
            mainModel.render(entity, f7, f6, f4, p_77036_5_, f13, f5);
        }
//        else if (!entity.isInvisibleToPlayer(Minecraft.getMinecraft().thePlayer))
//        {
//            GL11.glPushMatrix();
//            GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.15F);
//            GL11.glDepthMask(false);
//            GL11.glEnable(GL11.GL_BLEND);
//            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//            GL11.glAlphaFunc(GL11.GL_GREATER, 0.003921569F);
//            mainModel.render(entity, f7, f6, f4, p_77036_5_, f13, f5);
//            GL11.glDisable(GL11.GL_BLEND);
//            GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
//            GL11.glPopMatrix();
//            GL11.glDepthMask(true);
//        }
//        else
//        {
//            mainModel.setRotationAngles(f7, f6, f4, p_77036_5_, f13, f5, entity);
//        }

        TGRenderHelper.disableBlendMode(renderType);
    }
}
