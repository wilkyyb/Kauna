package lol.wilkyy.kauna.mixin.features.kahakka.parkour;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(KeyMapping.class)
public interface KeyMappingAccessor {
    // This targets the protected 'key' field and exposes it via a public method
    @Accessor("key")
    InputConstants.Key getBoundKey();
}