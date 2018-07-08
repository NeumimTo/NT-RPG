package cz.neumimto.effects.positive;

import cz.neumimto.core.ioc.Inject;
import cz.neumimto.model.BashModel;
import cz.neumimto.rpg.ClassGenerator;
import cz.neumimto.rpg.effects.EffectBase;
import cz.neumimto.rpg.effects.EffectContainer;
import cz.neumimto.rpg.effects.IEffectConsumer;
import cz.neumimto.rpg.effects.IEffectContainer;

/**
 * Created by NeumimTo on 4.7.2017.
 */
@ClassGenerator.Generate(id = "name", description = "An effect, which gives the target % chance to apply stun while attacking to entity")
public class Bash extends EffectBase<BashModel> {

	public static final String name = "Bash";

	public Bash(IEffectConsumer consumer, long duration, @Inject BashModel value) {
		super(name, consumer);
		setValue(value);
		setDuration(duration);
		setStackable(true, null);
	}


	@Override
	public IEffectContainer constructEffectContainer() {
		return new EffectContainer.UnstackableSingleInstance(this);
	}
}
