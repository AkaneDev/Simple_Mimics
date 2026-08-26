package au.akanedev.simplemimics.client;

import au.akanedev.simplemimics.entity.MimicEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.ArrowLayer;
import net.minecraft.client.renderer.entity.layers.BeeStingerLayer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.SpinAttackEffectLayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public class MimicEntityRenderer
        extends LivingEntityRenderer<
        MimicEntity,
        PlayerModel<MimicEntity>
        > {

    private final PlayerModel<MimicEntity> defaultModel;
    private final PlayerModel<MimicEntity> slimModel;

    public MimicEntityRenderer(
            EntityRendererProvider.Context context
    ) {
        super(
                context,
                new PlayerModel<>(
                        context.bakeLayer(
                                ModelLayers.PLAYER
                        ),
                        false
                ),
                0.5F
        );

        // =========================
        // PLAYER MODELS
        // =========================

        this.defaultModel =
                new PlayerModel<>(
                        context.bakeLayer(
                                ModelLayers.PLAYER
                        ),
                        false
                );

        this.slimModel =
                new PlayerModel<>(
                        context.bakeLayer(
                                ModelLayers.PLAYER_SLIM
                        ),
                        true
                );

        // =========================
        // ARMOUR
        // =========================

        this.addLayer(
                new HumanoidArmorLayer<>(
                        this,

                        new HumanoidModel<>(
                                context.bakeLayer(
                                        ModelLayers.PLAYER_INNER_ARMOR
                                )
                        ),

                        new HumanoidModel<>(
                                context.bakeLayer(
                                        ModelLayers.PLAYER_OUTER_ARMOR
                                )
                        ),

                        context.getModelManager()
                )
        );

        // =========================
        // HELD ITEMS
        // =========================

        this.addLayer(
                new ItemInHandLayer<>(
                        this,
                        context.getItemInHandRenderer()
                )
        );

        // =========================
        // ARROWS
        // =========================

        this.addLayer(
                new ArrowLayer<>(
                        context,
                        this
                )
        );

        // =========================
        // HEAD ITEMS
        // =========================

        this.addLayer(
                new CustomHeadLayer<>(
                        this,
                        context.getModelSet(),
                        context.getItemInHandRenderer()
                )
        );

        // =========================
        // ELYTRA
        // =========================

        this.addLayer(
                new ElytraLayer<>(
                        this,
                        context.getModelSet()
                )
        );

        // =========================
        // SPIN ATTACK
        // =========================

        this.addLayer(
                new SpinAttackEffectLayer<>(
                        this,
                        context.getModelSet()
                )
        );

        // =========================
        // BEE STINGER
        // =========================

        this.addLayer(
                new BeeStingerLayer<>(
                        this
                )
        );

        this.shadowRadius = 0.5F;
    }

    // =========================================================
    // TEXTURE
    // =========================================================

    @Override
    public ResourceLocation getTextureLocation(
            MimicEntity entity
    ) {
        String uuidStr =
                entity.getTargetAUUID();

        if (uuidStr == null ||
                uuidStr.isEmpty()) {

            return DefaultPlayerSkin
                    .getDefaultTexture();
        }

        try {
            UUID uuid =
                    UUID.fromString(uuidStr);

            var level =
                    Minecraft.getInstance().level;

            if (level == null) {
                return DefaultPlayerSkin
                        .getDefaultTexture();
            }

            var player =
                    level.getPlayerByUUID(uuid);

            if (player instanceof AbstractClientPlayer clientPlayer) {

                return clientPlayer
                        .getSkin()
                        .texture();
            }

        } catch (Exception ignored) {
        }

        return DefaultPlayerSkin
                .getDefaultTexture();
    }

    // =========================================================
    // SLIM SKIN
    // =========================================================

    private boolean isSlimSkin(
            MimicEntity entity
    ) {
        String uuidStr =
                entity.getTargetAUUID();

        if (uuidStr == null ||
                uuidStr.isEmpty()) {

            return false;
        }

        try {
            UUID uuid =
                    UUID.fromString(uuidStr);

            var level =
                    Minecraft.getInstance().level;

            if (level == null) {
                return false;
            }

            var player =
                    level.getPlayerByUUID(uuid);

            if (player instanceof AbstractClientPlayer clientPlayer) {

                return clientPlayer
                        .getSkin()
                        .model()
                        .equals(
                                PlayerSkin.Model.SLIM
                        );
            }

        } catch (Exception ignored) {
        }

        return false;
    }

    // =========================================================
    // RENDER
    // =========================================================

    @Override
    public void render(
            MimicEntity entity,
            float yaw,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light
    ) {
        // Select the correct player model based on
        // the target player's skin type.
        this.model =
                isSlimSkin(entity)
                        ? slimModel
                        : defaultModel;

        poseStack.pushPose();

        // Match your existing mimic scaling.
        poseStack.scale(
                1.0F,
                0.95F,
                1.0F
        );

        super.render(
                entity,
                yaw,
                partialTicks,
                poseStack,
                buffer,
                light
        );

        poseStack.popPose();
    }

    // =========================================================
    // NAME
    // =========================================================

    @Override
    protected boolean shouldShowName(
            MimicEntity entity
    ) {
        return true;
    }
}