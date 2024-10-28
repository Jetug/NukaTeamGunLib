package com.nukateam.ntgl.client.render.renderers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.base.utils.EntityDeathUtils;
import com.nukateam.ntgl.common.data.interfaces.IModelAccessor;
import com.nukateam.ntgl.common.data.util.MathUtil;
import com.nukateam.ntgl.common.foundation.item.MagazineItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

import static com.nukateam.ntgl.common.base.utils.EntityDeathUtils.DeathType.*;

public class DeathEffectEntityRenderer {
    private static final ResourceLocation RES_BIO_EFFECT = new ResourceLocation(Ntgl.MOD_ID, "textures/fx/bio.png");
//    private static final ResourceLocation RES_LASER_EFFECT = new ResourceLocation(Ntgl.MOD_ID, "textures/fx/laserdeath.png");
    private static final int MAX_DEATH_TIME = 20;

//    public static Field RLB_mainModel = ReflectionHelper.findField(RenderLivingBase.class, "mainModel", "field_77045_g");
//    protected static Method RLB_preRenderCallback = ReflectionHelper.findMethod(RenderLivingBase.class, "preRenderCallback", "func_77041_b", EntityLivingBase.class, float.class);
//    protected static Method R_bindEntityTexture = ReflectionHelper.findMethod(Render.class, "bindEntityTexture", "func_180548_c", Entity.class);
//
//    public static Field R_renderManager = ReflectionHelper.findField(Render.class, "renderManager", "field_76990_c");
//
//    protected static Method R_bindTexture = ReflectionHelper.findMethod(Render.class, "bindTexture", "func_110776_a", ResourceLocation.class);
//    protected static Method RLB_getColorMultiplier = ReflectionHelper.findMethod(RenderLivingBase.class, "getColorMultiplier", "func_77030_a", EntityLivingBase.class, float.class, float.class);
//
//    public static void preRenderCallback(RenderLivingBase<? extends EntityLivingBase> renderer, EntityLivingBase elb, float ptt) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
//        RLB_preRenderCallback.invoke(renderer, elb, ptt);
//    }
//
//    public static void bindEntityTexture(Render<? extends Entity> renderer, Entity entity) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
//        R_bindEntityTexture.invoke(renderer, entity);
//    }


    public static void setRenderScalingForEntity(LivingEntity elb, PoseStack poseStack) {
        if (elb instanceof Slime slime) {
            int size = slime.getSize();
            poseStack.scale((float) size, (float) size, (float) size);

            //slimes are 1,2 and 4
            if (size == 2) {
                poseStack.translate(0, -0.8f, 0);
            } else if (size == 4) {
                poseStack.translate(0, -1.2f, 0);
            }
        }
    }

    /**
     * Actually renders the given argument. This is a synthetic bridge method, always casting down its argument and then
     * handing it off to a worker function which does the actual work. In all probabilty, the class Render is generic
     * (Render<T extends Entity) and this method has signature public void func_76986_a(T entity, double d, double d1,
     * double d2, float f, float f1). But JAD is pre 1.5 so doesn't do that.
     */
    public static void doRender(LivingEntityRenderer renderer, LivingEntity entity, PoseStack poseStack,
                                MultiBufferSource buffer, int pPackedLight,
                                Vec3 pos) {
        this.model.renderToBuffer(pPoseStack, vertexconsumer, pPackedLight, i, 1.0F, 1.0F, 1.0F, flag1 ? 0.15F : 1.0F);

        poseStack.pushPose();
        {
            renderer.render();
            //renderer.mainModel.onGround = renderer.renderSwingProgress(entity, ptt);

            //   if (renderer.renderPassModel != null)
            //   {
            //       renderer.renderPassModel.onGround = renderer.mainModel.onGround;
            //   }

            //   renderer.mainModel.isRiding = entity.isRiding();

            //   if (renderer.renderPassModel != null)
            //   {
            //       renderer.renderPassModel.isRiding = renderer.mainModel.isRiding;
            //   }
            var mainModel = renderer.getModel();

//	        ModelBase renderPassModel = null;
//	        if (renderPassModel != null)
//	        {
//	            renderPassModel.isChild = mainModel.isChild;
//	        }

            try {
//                float f2 = MathUtil.interpolateRotation(entity.yRotO.prevRenderYawOffset, entity.yBodyRot.renderYawOffset, ptt);
//                float f3 = MathUtil.interpolateRotation(entity.yHeadRotO, entity.yHeadRot, ptt);
//                float f4;
//
//                float f13 = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * ptt;

//                poseStack.translate(pos.x, pos.y, pos.z);
//                f4 = (float) entity.ticksExisted + ptt;

                float f5 = 0.0625F;
                //GL11.glEnable(GL12.GL_RESCALE_NORMAL);

//                GlStateManager.enableRescaleNormal();
                poseStack.scale(-1.0f, -1.0f, -1.0f);
                //GL11.glScalef(-1.0F, -1.0F, 1.0F);

                //renderer.preRenderCallback(entity, ptt);

                poseStack.translate(0.0F, -24.0F * f5 - 0.0078125F, 0.0F);

//                float f6 = entity.prevLimbSwingAmount + (entity.limbSwingAmount - entity.prevLimbSwingAmount) * ptt;
//                float limbSwing = entity.limbSwing - entity.limbSwingAmount * (1.0F - ptt);

//                if (entity.isChild()) {
//                    limbSwing *= 3.0F;
//                }

//                if (f6 > 1.0F) {
//                    f6 = 1.0F;
//                }

                //GL11.glEnable(GL11.GL_ALPHA_TEST);
//                GlStateManager.enableAlpha();

                renderModelDeathBio(renderer, entity, poseStack, buffer, pPackedLight, OverlayTexture.NO_OVERLAY);

//                switch (deathType) {
//                    case BIO:
////                        mainModel.setupAnim(entity, limbSwing, f6, ptt);
//                        //renderModel(renderer, entity, f7, f6, f4, f3 - f2, f13, f5, null, RenderType.SOLID);
//                        //renderModel(renderer, entity, f7, f6, f4, f3 - f2, f13, f5, RES_BIO_EFFECT, RenderType.ADDITIVE);
////                        preRenderCallback(renderer, entity, ptt);
//                        renderModelDeathBio(renderer, entity, poseStack, buffer, pPackedLight, OverlayTexture.NO_OVERLAY);
//                        break;
////                    case LASER:
////                        mainModel.setLivingAnimations(entity, limbSwing, f6, ptt);
////                        preRenderCallback(renderer, entity, ptt);
////                        renderModelDeathLaser(renderer, entity, limbSwing, f6, f4, f3 - f2, f13, f5);
////                        break;
//                    case DEFAULT:
//                    default:
//                        break;
//
//                }
                /**DO NOT DISABLE ALPHA, VANILLA DOESN'T DO IT EITHER**/
                //    GlStateManager.disableAlpha();
//                GlStateManager.disableRescaleNormal();
                //renderExtraPasses(renderer, entity, f7, f6, f4, f3-f2, f13, f5, ptt);

            } catch (Exception exception) {
                //logger.error("Couldn\'t render entity", exception);
            }


//            GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
//            GlStateManager.enableTexture2D();
//            GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
//            GlStateManager.enableCull();
//            GlStateManager.popMatrix();
        }
        poseStack.popPose();
        //renderer.passSpecialRender(entity, x, y, z);
    }


    /**
     * Renders the model in RenderLiving
     */
    static void renderModelDeathBio(LivingEntityRenderer renderer, LivingEntity entity, PoseStack poseStack,
                                    MultiBufferSource buffer, int pPackedLight, int pPackedOverlay) {
        var prog = ((float) entity.deathTime / (float) MAX_DEATH_TIME);
        var rand = entity.getRandom();
        var mainModel = renderer.getModel();

//        try {
//            R_bindEntityTexture.invoke(renderer, entity);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

//        if (mainModel instanceof HumanoidModel<?>) {
//            mainModel.setupAnim(entity, limbSwing, f6, f4, p_77036_5_, f13, f5);
//        }

//        HashSet<ModelRenderer> childBoxes = new HashSet<>(64);
//        for (Object o : mainModel.boxList) {
//            ModelRenderer box = (ModelRenderer) o;
//            if (box.childModels != null) {
//                childBoxes.addAll(box.childModels);
//            }
//        }

        var accessor = (IModelAccessor)mainModel;
        var childBoxes = accessor.getModelParts();

        poseStack.pushPose();
        {
//            poseStack.rotate(entity.yHeadRot, 0, 1, 0);

//            setRenderScalingForEntity(entity, poseStack);

            for (var box : accessor.getModelParts()) {
                if (childBoxes.contains(box) && box.visible && !box.skipDraw) {
                    float scale = 1.0f + (rand.nextFloat() * prog);
                    poseStack.pushPose();
                    {
                        poseStack.translate(-box.x, -box.y, -box.z);
                        poseStack.scale(scale, scale, scale);
                        poseStack.translate(box.x, box.y, box.z);
                        var mainColor = (float)(1.0 - Math.pow(prog, 2.0));
                        var mainAlpha = (float)Math.pow(1.0 - prog, 2.0);

                        RenderSystem.setShaderColor(mainColor, 1.0f, mainColor, mainAlpha);

                        var texture = renderer.getTextureLocation(entity);
                        var rendertype = RenderType.itemEntityTranslucentCull(texture);
                        var vertexConsumer = buffer.getBuffer(rendertype);

                        box.render(poseStack, vertexConsumer, pPackedLight, pPackedOverlay);
                        RenderSystem.setShaderTexture(0, RES_BIO_EFFECT);
//                        renderManager.renderEngine.bindTexture(RES_BIO_EFFECT);
//                        TGRenderHelper.enableBlendMode(renderType);
                        RenderSystem.enableBlend();
                        var overlayColor = 0.5 + (Math.sin((Math.sqrt(prog) + 0.75) * 2.0 * Math.PI) / 2);
                        RenderSystem.setShaderColor((float) overlayColor, (float) overlayColor, (float) overlayColor, 1);
                        box.render(poseStack, vertexConsumer, pPackedLight, pPackedOverlay);
                        RenderSystem.disableBlend();
//                        TGRenderHelper.disableBlendMode(renderType);
                    }
                    poseStack.popPose();
                }
            }
        }
        poseStack.popPose();
    }

//    /**
//     * Renders the model in RenderLiving
//     */
//    static void renderModelDeathLaser(RenderLivingBase renderer, EntityLivingBase entity, float f7, float f6, float f4, float p_77036_5_, float f13, float f5) {
//        float prog = ((float) entity.deathTime / (float) MAX_DEATH_TIME);
//
//        Random rand = new Random(entity.getEntityId());
//        //ResourceLocation texture = RES_BIO_EFFECT;
//        RenderType renderType = RenderType.ADDITIVE;
//        ModelBase mainModel = null;
//        //ModelBase renderPassModel;
//        RenderManager renderManager = null;
//        try {
//            mainModel = (ModelBase) RLB_mainModel.get(renderer);
//            renderManager = (RenderManager) R_renderManager.get(renderer);
//            //renderPassModel = (ModelBase)R_renderPassModel.get(renderer);
//            R_bindEntityTexture.invoke(renderer, entity);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        //System.out.println("BoxList: "+mainModel.boxList.size());
//        //1st: Entity Texture
//        //mainModel.render(entity, f7, f6, f4, p_77036_5_, f13, f5);
//
//        if (mainModel instanceof ModelBiped) {
//            ((ModelBiped) mainModel).setRotationAngles(f7, f6, f4, p_77036_5_, f13, f5, entity);
//        }
//
////    	HashSet<ModelRenderer> childBoxes = new HashSet<ModelRenderer>(64);
////        for (Object o : mainModel.boxList) {
////        	ModelRenderer box = (ModelRenderer)o;
////        	if (box.childModels != null) {
////        		childBoxes.addAll(box.childModels);
////        	}
////        }
//
//        GlStateManager.pushMatrix();
//        GlStateManager.rotate(entity.rotationYaw, 0, 1, 0);
//
//        setRenderScalingForEntity(entity);
//
////        for (Object o : mainModel.boxList) {
////        	ModelRenderer box = (ModelRenderer)o;
////        	if (!childBoxes.contains(box) && !box.isHidden && box.showModel) {
////        		float scale = 1.0f-(rand.nextFloat()*prog);
////        		GL11.glPushMatrix();
////        		GL11.glTranslatef(-box.offsetX, -box.offsetY, -box.offsetZ);
////        		GL11.glScalef(scale, scale, scale);
////        		GL11.glTranslatef(box.offsetX, box.offsetY, box.offsetZ);
////        		double mainColor = 1.0-Math.pow(prog, 2.0);
////        		double mainAlpha = Math.pow(1.0-prog, 2.0);
////        		GL11.glColor4d(1.0,mainColor, mainColor, mainAlpha);
////        		box.render(f5);
////        		renderManager.renderEngine.bindTexture(RES_LASER_EFFECT);
////            	TGRenderHelper.enableBlendMode(renderType);
////            	double overlayColor = 0.5+(Math.sin((Math.sqrt(prog)+0.75)*2.0*Math.PI)/2);
////            	GL11.glColor3d(overlayColor, overlayColor, overlayColor);
////        		box.render(f5);
////                TGRenderHelper.disableBlendMode(renderType);
////
////        		GL11.glPopMatrix();
////        	}
////        }
//
//        //  GlStateManager.pushMatrix();
//        double mainColor = 1.0 - Math.pow(prog, 2.0);
//        double mainAlpha = Math.pow(1.0 - prog, 2.0);
//        GlStateManager.color(1.0f, (float) mainColor, (float) mainColor, (float) mainAlpha);
//        mainModel.render(entity, f7, f6, f4, p_77036_5_, f13, f5);
//        renderManager.renderEngine.bindTexture(RES_LASER_EFFECT);
//        TGRenderHelper.enableBlendMode(renderType);
//        double overlayColor = 0.5 + (Math.sin((Math.sqrt(prog) + 0.75) * 2.0 * Math.PI) / 2);
//        GlStateManager.color((float) overlayColor, (float) overlayColor, (float) overlayColor);
//        mainModel.render(entity, f7, f6, f4, p_77036_5_, f13, f5);
//        TGRenderHelper.disableBlendMode(renderType);
//
//        //GlStateManager.popMatrix();
//
//        GlStateManager.popMatrix();
//
//    }
//

//    /**
//     * Renders the model in RenderLiving
//     */
//    static void renderModel(RenderLivingBase renderer, EntityLivingBase entity, float f7, float f6, float f4, float p_77036_5_, float f13, float f5, ResourceLocation texture, RenderType renderType) {
//
//        ModelBase mainModel = null;
//        ModelBase renderPassModel;
//        RenderManager renderManager = null;
//        try {
//            mainModel = (ModelBase) RLB_mainModel.get(renderer);
//            renderManager = (RenderManager) R_renderManager.get(renderer);
////            renderPassModel = (ModelBase)RLB_renderPassModel.get(renderer);
//
//            if (texture != null) {
//                renderManager.renderEngine.bindTexture(RES_BIO_EFFECT);
//            } else {
//                R_bindEntityTexture.invoke(renderer, entity);
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        TGRenderHelper.enableBlendMode(renderType);
//
//        if (!entity.isInvisible()) {
//            mainModel.render(entity, f7, f6, f4, p_77036_5_, f13, f5);
//        }
//
//        TGRenderHelper.disableBlendMode(renderType);
//    }
}
