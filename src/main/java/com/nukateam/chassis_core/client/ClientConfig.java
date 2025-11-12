package com.nukateam.chassis_core.client;

import com.nukateam.chassis_core.client.resources.ModResourceManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class ClientConfig {
    public static final ModResourceManager modResourceManager = new ModResourceManager();

    @NotNull
    public static final Options OPTIONS = Minecraft.getInstance().options;
}
