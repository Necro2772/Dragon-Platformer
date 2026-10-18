package io.github.dragonplatformer.Screen;

import com.badlogic.gdx.scenes.scene2d.ui.*;

public class ClippingProgressBar extends Stack {
    float value;
    float maxValue;

    Table knob;
    Table fillWrapper;

    public ClippingProgressBar(float maxValue, float value, Image background, Image knobBeforeImage) {
        super();
        setMaxValue(maxValue);
        setValue(value);
        Table knobBefore = new Table();
        knob = new Table() {
            private float toValue = value;
            private float currentValue = value;
            private float interpolateTimer = 0;

            @Override
            public void act(float delta) {
                super.act(delta);
                interpolateTimer += delta;
                Cell<Table> knobBeforeCell = getCell(knobBefore);

                float currentPlayerHealth = getValue() / getMaxValue();
                if (toValue != currentPlayerHealth) {
                    interpolateTimer = 0;
                    currentValue = toValue;
                    toValue = currentPlayerHealth;
                }
                float interpolateDuration = 0.3f;
                float progress = Math.min(1, interpolateTimer / interpolateDuration);
                if (currentValue != toValue) {
                    float displayPlayerHealth = (currentValue * (1 - progress) + toValue * progress);
                    knobBeforeCell.width(Value.percentWidth(displayPlayerHealth, this));
                    knobBeforeCell.padRight(Value.percentWidth(1 - displayPlayerHealth, this));
                    if (progress >= 1) currentValue = toValue;
                    invalidate();
                }
            }
        };
        float startValue = getValue() / getMaxValue();
        knob.row().height(Value.percentHeight(1f, this));
        knob.add(knobBefore).width(Value.percentWidth(startValue, this)).padRight(Value.percentWidth(1 - startValue, this));

        fillWrapper = new Table();
        fillWrapper.add(knobBeforeImage)
            .width(Value.percentWidth(1, this)).height(Value.percentHeight(1, this))
            .padLeft(Value.percentWidth(1, this)).padBottom(Value.percentHeight(1, this));
        knobBefore.addActor(fillWrapper);
        knobBefore.setClip(true);
        add(knob);
        add(background);
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (fillWrapper != null) fillWrapper.invalidate();
    }

    public void setValue(float value) {
        this.value = Math.min(value, maxValue);
    }

    public void setMaxValue(float maxValue) {
        this.maxValue = maxValue;
    }

    public float getValue() {
        return value;
    }

    public float getMaxValue() {
        return maxValue;
    }
}
