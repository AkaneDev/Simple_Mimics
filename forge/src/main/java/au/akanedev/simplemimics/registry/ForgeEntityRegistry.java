package au.akanedev.simplemimics.registry;

import au.akanedev.simplemimics.Constants;
import au.akanedev.simplemimics.entity.MimicEntity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.resources.ResourceLocation;

import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;


public class ForgeEntityRegistry implements PlatformEntityRegistry {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(
                    ForgeRegistries.ENTITY_TYPES,
                    Constants.MOD_ID
            );


    public static final RegistryObject<EntityType<MimicEntity>> MIMIC =
            ENTITY_TYPES.register(
                    "mimic",
                    () -> EntityType.Builder
                            .of(
                                    MimicEntity::new,
                                    MobCategory.MISC
                            )
                            .sized(0.6F, 1.8F)
                            .build(
                                    ResourceLocation
                                            .fromNamespaceAndPath(
                                                    Constants.MOD_ID,
                                                    "mimic"
                                            )
                                            .toString()
                            )
            );


    @Override
    public EntityType<MimicEntity> mimic() {
        return MIMIC.get();
    }
}