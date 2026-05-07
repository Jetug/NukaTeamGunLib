package com.nukateam.ntgl.common.datagen;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.foundation.init.NtglDamageTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.DamageTypeTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageType;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/**
 * Author: MrCrayfish
 */
public class DamageTypeGen extends DamageTypeTagsProvider {
    public DamageTypeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, Ntgl.MOD_ID, existingFileHelper);
    }

//    @Override
//    protected void addTags(HolderLookup.Provider provider) {
//        this.tag(DamageTypeTags.IS_PROJECTILE).add(NtglDamageTypes.BULLET);
//    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(DamageTypeTags.IS_PROJECTILE)
                .add(NtglDamageTypes.BULLET);

    }

    @Override
    public String getName() {
        return "Ntgl Damage Type Tags";
    }
}
