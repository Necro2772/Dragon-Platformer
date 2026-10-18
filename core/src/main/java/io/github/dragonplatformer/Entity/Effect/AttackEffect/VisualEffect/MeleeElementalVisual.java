package io.github.dragonplatformer.Entity.Effect.AttackEffect.VisualEffect;

import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import io.github.dragonplatformer.Entity.AnimationManager;
import io.github.dragonplatformer.Entity.EffectManager;
import io.github.dragonplatformer.Entity.MovementType;

import java.util.ArrayList;
import java.util.List;

public class MeleeElementalVisual extends VisualEffect {
    public MeleeElementalVisual(float x, float y, Vector2 targetPos, Vector2 targetVel, List<PatrolPoint> patrolPath,
                                ParticleEffect effect, EffectManager effectManager, AnimationManager animationManager,
                                World world) {
        super(x, y, patrolPath.get(patrolPath.size() - 1).getTime(), 0, effect,
            effectManager, animationManager, world);
        setTargetPos(targetPos);
        setTargetVel(targetVel);
        setMovementType(MovementType.PATROL);
        setAutoMove(true);
        setSpeed(4);
        setAcceleration(40);
        addPatrolPoints(patrolPath);
        init();
    }
}
