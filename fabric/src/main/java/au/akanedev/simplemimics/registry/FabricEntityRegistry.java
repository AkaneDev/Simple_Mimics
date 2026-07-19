package au.akanedev.simplemimics.registry;

import au.akanedev.simplemimics.Constants;
import au.akanedev.simplemimics.entity.MimicEntity;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;


public class FabricEntityRegistry implements PlatformEntityRegistry {

    public static final EntityType<MimicEntity> MIMIC =
            Registry.register(
                    BuiltInRegistries.ENTITY_TYPE,
                    ResourceLocation.fromNamespaceAndPath(
                            Constants.MOD_ID,
                            "mimic"
                    ),
                    FabricEntityTypeBuilder
                            .create(
                                    MobCategory.MISC,
                                    MimicEntity::new
                            )
                            .dimensions(
                                    EntityDimensions.fixed(
                                            0.6F,
                                            1.8F
                                    )
                            )
                            .build()
            );

    public static void registerAttributes() {
        FabricDefaultAttributeRegistry.register(
                MIMIC,
                MimicEntity.createAttributes()
        );
    }

    @Override
    public EntityType<MimicEntity> mimic() {
        return MIMIC;
    }
}