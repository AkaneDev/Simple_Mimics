package au.akanedev.simplemimics.registry;

import au.akanedev.simplemimics.entity.MimicEntity;
import net.minecraft.world.entity.EntityType;

public interface PlatformEntityRegistry {

    EntityType<MimicEntity> mimic();
}