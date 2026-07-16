package au.akanedev.simplemimics.mimics.ai;

import au.akanedev.simplemimics.mimics.entity.MimicEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class MimicBehaviourGoal extends Goal {

    private final MimicEntity mimic;

    private int repathDelay = 0;
    private int lookDelay = 0;

    public MimicBehaviourGoal(MimicEntity mimic) {
        this.mimic = mimic;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return mimic.getTargetA() != null || mimic.getTargetB() != null;
    }

    @Override
    public void tick() {

        Player targetA = mimic.getTargetA();
        Player targetB = mimic.getTargetB();

        PathNavigation nav = mimic.getNavigation();

        // ==============================
        // ALWAYS LOOK AT TARGET (B PRIORITY)
        // ==============================
        Player lookTarget = (targetB != null) ? targetB : targetA;

        if (lookTarget != null && lookDelay-- <= 0) {
            lookDelay = 2; // smooth head tracking

            mimic.getLookControl().setLookAt(
                    lookTarget.getX(),
                    lookTarget.getEyeY(),
                    lookTarget.getZ(),
                    35.0f,
                    35.0f
            );
        }

        // ==============================
        // PRIORITY 1: HARASS TARGET B
        // (1–5 block pressure system)
        // ==============================
        if (targetB != null && targetB.isAlive()) {

            double dist = mimic.distanceTo(targetB);

            Vec3 predicted = predictPosition(targetB, 6);

            // too far → close in
            if (dist > 5.5) {

                if (repathDelay-- <= 0) {
                    repathDelay = 8;

                    nav.moveTo(
                            predicted.x,
                            predicted.y,
                            predicted.z,
                            1.35
                    );
                }

            }
            // too close → back off slightly
            else if (dist < 1.5) {

                Vec3 away = mimic.position()
                        .subtract(targetB.position())
                        .normalize()
                        .scale(0.25);

                mimic.setDeltaMovement(mimic.getDeltaMovement().add(away));

            }

            return;
        }

        // ==============================
        // PRIORITY 2: MIMIC TARGET A
        // (maintain ~10 block spacing)
        // ==============================
        if (targetA != null && targetA.isAlive()) {

            double distA = mimic.distanceTo(targetA);

            Vec3 predictedA = predictPosition(targetA, 10);

            if (Math.abs(distA - 10.0) > 3.0) {

                if (repathDelay-- <= 0) {
                    repathDelay = 12;

                    nav.moveTo(
                            predictedA.x,
                            predictedA.y,
                            predictedA.z,
                            1.0
                    );
                }
            }
        }
    }

    /**
     * Simple movement prediction (makes it feel like a player, not a bot)
     */
    private Vec3 predictPosition(Player player, double leadTicks) {

        Vec3 vel = player.getDeltaMovement();

        return new Vec3(
                player.getX() + vel.x * leadTicks,
                player.getY(),
                player.getZ() + vel.z * leadTicks
        );
    }
}