package com.nukateam.ntgl.common.data.datagen;

import com.nukateam.ntgl.Ntgl;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;

public class LanguageGen extends LanguageProvider {
    public LanguageGen(DataGenerator gen) {
        super(gen, Ntgl.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
    }
}
