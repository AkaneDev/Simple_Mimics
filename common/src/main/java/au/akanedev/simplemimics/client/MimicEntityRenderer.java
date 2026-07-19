package au.akanedev.simplemimics.client;

import au.akanedev.simplemimics.entity.MimicEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public class MimicEntityRenderer extends LivingEntityRenderer<MimicEntity, PlayerModel<MimicEntity>> {

    public MimicEntityRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), true),
                0.5f
        );

        // ===== SAFE LAYERS (LivingEntity-compatible only) =====

        this.addLayer(new HumanoidArmorLayer<>(
                this,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager()
        ));

        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));

        this.addLayer(new ArrowLayer<>(context, this));

        this.addLayer(new CustomHeadLayer<>(this, context.getModelSet(), context.getItemInHandRenderer()));

        this.addLayer(new ElytraLayer<>(this, context.getModelSet()));

        this.addLayer(new SpinAttackEffectLayer<>(this, context.getModelSet()));

        this.addLayer(new BeeStingerLayer<>(this));

        this.shadowRadius = 0.5f;
    }

    @Override
    public ResourceLocation getTextureLocation(MimicEntity entity) {

        String uuidStr = entity.getTargetAUUID();

        if (uuidStr == null || uuidStr.isEmpty()) {
            return DefaultPlayerSkin.getDefaultTexture();
        }

        try {
            UUID uuid = UUID.fromString(uuidStr);

            var level = Minecraft.getInstance().level;
            if (level == null) {
                return DefaultPlayerSkin.getDefaultTexture();
            }

            var player = level.getPlayerByUUID(uuid);

            if (player instanceof AbstractClientPlayer clientPlayer) {
                return clientPlayer.getSkin().texture();
            }

        } catch (Exception ignored) {}

        return DefaultPlayerSkin.getDefaultTexture();
    }

    @Override
    protected boolean shouldShowName(MimicEntity entity) {
        return true;
    }
}