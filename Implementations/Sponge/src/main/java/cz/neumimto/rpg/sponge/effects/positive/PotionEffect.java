package cz.neumimto.rpg.sponge.effects.positive;


import cz.neumimto.rpg.api.effects.EffectBase;
import cz.neumimto.rpg.api.effects.EffectContainer;
import cz.neumimto.rpg.api.effects.IEffectContainer;
import cz.neumimto.rpg.api.entity.IEffectConsumer;
import cz.neumimto.rpg.api.skills.scripting.JsBinding;
import cz.neumimto.rpg.sponge.model.PotionEffectModel;

/**
 * Created by NeumimTo on 9.7.2017.
 */
@JsBinding(JsBinding.Type.CLASS)
public class PotionEffect extends EffectBase<PotionEffectModel> {

    public static final String name = "Potion";

    public PotionEffect(IEffectConsumer consumer, long duration, PotionEffectModel model) {
        super(name, consumer);
        setDuration(duration);
        setValue(model);
        setStackable(true, null);
    }

    @Override
    public IEffectContainer<PotionEffectModel, PotionEffect> constructEffectContainer() {
        return new EffectContainer<PotionEffectModel, PotionEffect>(this) {
            @Override
            public void updateStackedValue() {
                setStackedValue(null);
                for (PotionEffect potionEffect : getEffects()) {
                    if (getStackedValue() == null) {
                        setStackedValue(potionEffect.getValue());
                    } else {
                        getStackedValue().mergeWith(potionEffect.getValue());
                    }
                }
            }
        };
    }
}
