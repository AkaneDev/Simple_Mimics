package au.akanedev.simplemimics.mimics.entity;

import au.akanedev.simplemimics.Simplemimics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registry for Mimic entity type
 */
public class MimicEntityType {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = 
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Simplemimics.MODID);

    // The Mimic entity type - uses Player entity type for rendering
    public static final RegistryObject<EntityType<MimicEntity>> MIMIC = ENTITY_TYPES.register(
            "mimic",
            () -> EntityType.Builder.<MimicEntity>of(
                            MimicEntity::new,
                    MobCategory.MISC
            )
            .sized(0.6F, 1.8F) // Player size
            .build(ResourceLocation.fromNamespaceAndPath(Simplemimics.MODID, "mimic").toString())
    );

    @Mod.EventBusSubscriber(modid = Simplemimics.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class Registrar {
        @SubscribeEvent
        public static void onEntityAttributeCreate(EntityAttributeCreationEvent event) {
            // Register attributes for mimic entity
            event.put(MIMIC.get(), MimicEntity.createAttributes().build());
        }
    }
}