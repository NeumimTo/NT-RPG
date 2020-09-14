package cz.neumimto.rpg.sponge.effects.positive;

import cz.neumimto.rpg.api.Rpg;
import cz.neumimto.rpg.api.effects.EffectBase;
import cz.neumimto.rpg.api.effects.Generate;
import cz.neumimto.rpg.api.effects.IEffect;
import cz.neumimto.rpg.api.effects.stacking.FloatEffectStackingStrategy;
import cz.neumimto.rpg.api.entity.CommonProperties;
import cz.neumimto.rpg.api.entity.IEffectConsumer;
import cz.neumimto.rpg.api.skills.scripting.JsBinding;
import cz.neumimto.rpg.sponge.entities.ISpongeEntity;

/**
 * Created by NeumimTo on 3.7.2017.
 */
@JsBinding(JsBinding.Type.CLASS)
@Generate(id = "name", description = "Increments walk speed, unlike speedboost effect this one wont apply vanilla potion effect")
public class IncreasedMovementSpeedEffect extends EffectBase<Float> {

    public static final String name = "Movement Speed";

    public IncreasedMovementSpeedEffect(IEffectConsumer consumer, long duration, float value) {
        super(name, consumer);
        setValue(value);
        setDuration(duration);
        setStackable(true, FloatEffectStackingStrategy.INSTANCE);
    }

    @Override
    public void onApply(IEffect self) {
        getConsumer().setProperty(CommonProperties.walk_speed, getConsumer().getProperty(CommonProperties.walk_speed) + getValue());
        Rpg.get().getEntityService().updateWalkSpeed((ISpongeEntity) getConsumer());
    }

    @Override
    public void onRemove(IEffect self) {
        getConsumer().setProperty(CommonProperties.walk_speed, getConsumer().getProperty(CommonProperties.walk_speed) - getValue());
        Rpg.get().getEntityService().updateWalkSpeed((ISpongeEntity) getConsumer());
    }

    @Override
    public void setValue(Float o) {
        if (getValue() != null) {
            throw new IllegalStateException("Operation permited");
        }
        super.setValue(o);
    }
}
