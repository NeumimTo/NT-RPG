package cz.neumimto.effects.positive;

import cz.neumimto.rpg.ClassGenerator;
import cz.neumimto.rpg.effects.EffectBase;
import cz.neumimto.rpg.effects.IEffectConsumer;
import cz.neumimto.rpg.effects.IEffectContainer;
import cz.neumimto.rpg.effects.common.stacking.ValueProcessor;

/**
 * Created by NeumimTo on 7.7.2017.
 */
@ClassGenerator.Generate(id = "name", description = "An effect which cancells all incoming damage to the target, if attacking player has manapool "
		+ "below specific % value")
public class DampenEffect extends EffectBase<Double> {

	public static final String name = "Dampen";

	public DampenEffect(IEffectConsumer consumer, long duration, double value) {
		super(name, consumer);
		setDuration(duration);
		setValue(value);
		setStackable(true, null);
	}


	@Override
	@SuppressWarnings("unchecked")
	public IEffectContainer constructEffectContainer() {
		return new ValueProcessor.D_MIN(this);
	}
}
