package io.github.dragonplatformer.Entity;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class AnimationWrapper extends Animation<TextureRegion> {
    private final Vector2 offset;
    //private final Vector2 size;

    public AnimationWrapper(float frameDuration, Array<? extends TextureRegion> keyFrames, PlayMode playMode) {
        this(frameDuration, keyFrames, playMode, new Vector2());
    }

    public AnimationWrapper(float frameDuration, Array<? extends TextureRegion> keyFrames, PlayMode playMode,
                            Vector2 offset) {
        super(frameDuration, keyFrames, playMode);
        this.offset = offset;
        //this.size = new Vector2(keyFrames.get(0).getRegionWidth(), keyFrames.get(0).getRegionHeight());
    }

    public Vector2 getOffset() {
        return offset;
    }

//    public Vector2 getSize() {
//        return size;
//    }
}
