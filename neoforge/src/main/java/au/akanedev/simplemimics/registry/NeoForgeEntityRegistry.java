package au.akanedev.simplemimics.registry;

import au.akanedev.simplemimics.Constants;
import au.akanedev.simplemimics.entity.MimicEntity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class NeoForgeEntityRegistry implements PlatformEntityRegistry {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(
                    Registries.ENTITY_TYPE,
                    Constants.MOD_ID
            );


    public static final DeferredHolder<EntityType<?>, EntityType<MimicEntity>> MIMIC =
            ENTITY_TYPES.register(
                    "mimic",
                    () -> EntityType.Builder
                            .of(
                                    MimicEntity::new,
                                    MobCategory.MISC
                            )
                            .sized(0.6F, 1.8F)
                            .build("mimic")
            );


    @Override
    public EntityType<MimicEntity> mimic() {
        return MIMIC.get();
    }
}