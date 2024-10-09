package com.nukateam.ntgl.common.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import com.nukateam.ntgl.Config;
import com.nukateam.ntgl.common.foundation.entity.projectile.DeathEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.Objects;

import static com.nukateam.ntgl.common.base.utils.EntityDeathUtils.*;

public class S2CMessageEntityDeath extends PlayMessage<S2CMessageEntityDeath> {
    int entityId;
    int deathTypeId;
    float motionX;
    float motionY;
    float motionZ;

    public S2CMessageEntityDeath() {}

	public S2CMessageEntityDeath(int entityId, int deathTypeId, float motionX, float motionY, float motionZ) {
		this.entityId = entityId;
		this.deathTypeId = deathTypeId;
		this.motionX = motionX;
		this.motionY = motionY;
		this.motionZ = motionZ;
	}

    public S2CMessageEntityDeath(LivingEntity entity, DeathType deathtype) {
		this.entityId = entity.getId();
		this.deathTypeId = Objects.requireNonNullElse(deathtype, DeathType.DEFAULT).getValue();
		this.motionX = (float) entity.xo;
		this.motionY = (float) entity.yo;
		this.motionZ = (float) entity.zo;
    }


	@Override
	public void encode(S2CMessageEntityDeath message, FriendlyByteBuf buf) {
		buf.writeInt(message.entityId);
		buf.writeInt(message.deathTypeId);
		buf.writeFloat(message.motionX);
		buf.writeFloat(message.motionY);
		buf.writeFloat(message.motionZ);
	}

	@Override
	public S2CMessageEntityDeath decode(FriendlyByteBuf buf) {
		return new S2CMessageEntityDeath(buf.readInt(), buf.readInt(), buf.readFloat(), buf.readFloat(), buf.readFloat());
	}

	@Override
	public void handle(S2CMessageEntityDeath message, MessageContext supplier) {
		supplier.execute((() -> {
//			if (Config.CLIENT.particle.enableDeathFX.get()) {
//				Player player = Minecraft.getInstance().player; //TGPackets.getPlayerFromContext(ctx);
//				var entity = (LivingEntity) player.level().getEntity(message.entityId);
//				DeathType deathtype = DeathType.values()[message.deathTypeId];
//
//				if (deathtype != DeathType.GORE || (deathtype == DeathType.GORE && Config.CLIENT.particle.enableDeathFX.get())) {
//
//					if (entity != null) {
//						entity.xo = message.motionX;
//						entity.yo = message.motionY;
//						entity.zo = message.motionZ;
//						//System.out.printf("(message)EntityMotion: (%.1f/%.1f/%.1f)\n",message.motionX,message.motionY,message.motionZ);
//
////						ClientProxy.get().setEntityDeathType(entity, deathtype);
//						DeathEffect.createDeathEffect(entity, deathtype, entity.getDeltaMovement());
//					}
//				}
//			}
		}));
		supplier.setHandled(true);
	}
//
//    public static class Handler implements IMessageHandler<S2CMessageEntityDeath, IMessage> {
//
//        @Override
//        public IMessage onMessage(S2CMessageEntityDeath message, MessageContext ctx) {
//            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
//            return null;
//        }
//
//        private void handle(S2CMessageEntityDeath message, MessageContext ctx) {
//
////			System.out.println("Get Packet!");
//
//            //If deathFX are disabled, ignore packet
//            if (TGConfig.cl_enableDeathFX) {
//
//                EntityPlayer ply = TGPackets.getPlayerFromContext(ctx);
//                EntityLivingBase entity = (LivingEntity) ply.world.getEntityByID(message.entityId);
//                DeathType deathtype = DeathType.values()[message.deathTypeId];
//
//                if (deathtype != DeathType.GORE || (deathtype == DeathType.GORE && TGConfig.cl_enableDeathFX_Gore)) {
//
//                    if (entity != null) {
//                        entity.motionX = message.motionX;
//                        entity.motionY = message.motionY;
//                        entity.motionZ = message.motionZ;
//                        //System.out.printf("(message)EntityMotion: (%.1f/%.1f/%.1f)\n",message.motionX,message.motionY,message.motionZ);
//
//                        ClientProxy.get().setEntityDeathType(entity, deathtype);
//                        DeathEffect.createDeathEffect(entity, deathtype, message.motionX, message.motionY, message.motionZ);
//                    }
//                }
//            }
//        }
//
//    }
}
