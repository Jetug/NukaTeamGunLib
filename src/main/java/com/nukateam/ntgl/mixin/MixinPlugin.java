package com.nukateam.ntgl.mixin;

import net.minecraftforge.fml.loading.FMLEnvironment;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class MixinPlugin implements IMixinConfigPlugin {
    private boolean isFrameworkInstalled;

    @Override
    public void onLoad(String mixinPackage) {
        try {
            Class.forName("com.mrcrayfish.framework.Framework", false, this.getClass().getClassLoader());
            this.isFrameworkInstalled = true;
        } catch (Exception e) {
            this.isFrameworkInstalled = false;
        }
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        boolean isDevelopmentEnvironment = !FMLEnvironment.production;
        if (mixinClassName.contains("mixin.dev") && !isDevelopmentEnvironment)
            return false;
        else return !mixinClassName.contains("mixin.prod") || !isDevelopmentEnvironment;

        //return this.isFrameworkInstalled; // this makes sure that forge's helpful mods not found screen shows up
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
