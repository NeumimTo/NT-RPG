/*    
 *     Copyright (c) 2015, NeumimTo https://github.com/NeumimTo
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *     
 */

package cz.neumimto.rpg.effects;

import cz.neumimto.core.ioc.Inject;
import cz.neumimto.core.ioc.PostProcess;
import cz.neumimto.core.ioc.Singleton;
import cz.neumimto.rpg.ClassGenerator.Generate;
import cz.neumimto.rpg.NtRpgPlugin;
import cz.neumimto.rpg.ResourceLoader;
import cz.neumimto.rpg.configuration.EffectDumpConfiguration;
import cz.neumimto.rpg.configuration.PluginConfig;
import cz.neumimto.rpg.configuration.SkillDumpConfiguration;
import cz.neumimto.rpg.configuration.SkillsDumpConfiguration;
import cz.neumimto.rpg.effects.model.EffectModelFactory;
import cz.neumimto.rpg.players.ActiveCharacter;
import cz.neumimto.rpg.players.IActiveCharacter;
import cz.neumimto.rpg.skills.ISkill;
import ninja.leaping.configurate.SimpleConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import org.spongepowered.api.Game;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.text.Text;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Created by NeumimTo on 17.1.2015.
 */
@Singleton
@ResourceLoader.ListenerClass
public class EffectService {

	public static final long TICK_PERIOD = 5L;

	private static final long unlimited_duration = -1;

	@Inject
	private Game game;

	@Inject
	private NtRpgPlugin plugin;

	private Set<IEffect> effectSet = new HashSet<>();
	private Set<IEffect> pendingAdditions = new HashSet<>();
	private Set<IEffect> pendingRemovals = new HashSet<>();
	private Map<String, IGlobalEffect> globalEffects = new HashMap<>();

	/**
	 * calls effect.onApply and registers if effect requires
	 *
	 * @param effect
	 */
	protected void runEffect(IEffect effect) {
		pendingAdditions.add(effect);
	}

	/**
	 * Puts the effect into the remove queue. onRemove will be called one tick later
	 *
	 * @param effect
	 */
	public void stopEffect(IEffect effect) {
		if (effect.requiresRegister()) {
			pendingRemovals.add(effect);
		}
	}

	@Listener
	public void onLoadLate(GameStartingServerEvent event) {
		SkillsDumpConfiguration c = new SkillsDumpConfiguration();
		for (Map.Entry<String, IGlobalEffect> effect : globalEffects.entrySet()) {
			Class aClass = effect.getValue().asEffectClass();
			if (aClass != null && aClass.isAnnotationPresent(Generate.class)) {
				Generate meta = (Generate) aClass.getAnnotation(Generate.class);
				String description = meta.description();
				String name = effect.getKey();
				EffectDumpConfiguration w = new EffectDumpConfiguration();
				Class<?> modelType = EffectModelFactory.getModelType(aClass);
				if (EffectModelFactory.typeMappers.containsKey(modelType)) {
					w.getSettingNodes().put("value", modelType.getSimpleName());
				} else {
					Stream.of(modelType.getFields())
							.forEach(f -> w.getSettingNodes().put(f.getName(), f.getType().getSimpleName()));
				}
				w.setDescription(description);
				c.getEffects().put(name, w);
			}
		}

		for (ISkill skill : NtRpgPlugin.GlobalScope.skillService.getAll()) {
			SkillDumpConfiguration w = new SkillDumpConfiguration();
			w.setSkillId(skill.getId());
			w.getFloatNodes().addAll(skill.getSettings().getNodes().keySet());
			//skill.getSettings().getObjectNodes()
			c.getSkills().put(skill.getName(), w);
		}

		try {
			File file = new File(NtRpgPlugin.workingDir, "skills.conf");
			if (file.exists())
				file.delete();
			ObjectMapper.BoundInstance configMapper = ObjectMapper.forObject(c);
			HoconConfigurationLoader hcl = HoconConfigurationLoader.builder().setPath(file.toPath()).build();
			SimpleConfigurationNode scn = SimpleConfigurationNode.root();
			configMapper.serialize(scn);
			hcl.save(scn);
		} catch (Exception e) {
			throw new RuntimeException("Could not create file skills.conf", e);
		}
	}

	@PostProcess(priority = 1000)
	public void run() {
		game.getScheduler().createTaskBuilder().name("EffectTask")
				.delay(5L, TimeUnit.MILLISECONDS)
				.interval(TICK_PERIOD, TimeUnit.MILLISECONDS)
				.execute(this::schedule)
				.submit(plugin);
	}

	public void schedule() {
		for (IEffect pendingRemoval : pendingRemovals) {
			removeEffectContainer(pendingRemoval.getEffectContainer(), pendingRemoval, pendingRemoval.getConsumer());
			effectSet.remove(pendingRemoval);
		}
		pendingRemovals.clear();
		long l = System.currentTimeMillis();
		for (IEffect e : effectSet) {
			if (e.getConsumer() == null || e.getConsumer().isDetached()) {
				pendingRemovals.add(e);
				continue;
			}
			if (e.getPeriod() + e.getLastTickTime() <= l) {
				tickEffect(e, l);
			}

			if (e.getDuration() == unlimited_duration) {
				continue;
			}

			if (e.getExpireTime() <= l) {
				removeEffect(e, e.getConsumer());
			}
		}

		effectSet.addAll(pendingAdditions);
		pendingAdditions.clear();
	}

	/**
	 * Calls onTick and increments tickCount
	 *
	 * @param effect
	 */
	public void tickEffect(IEffect effect, long time) {
		if (!effect.isTickingDisabled()) {
			if (effect.getConsumer().isDetached()) {
				removeEffect(effect.getName(), effect.getConsumer(), effect.getEffectSourceProvider());
				return;
			}
			effect.onTick();
			effect.tickCountIncrement();
		}
		effect.setLastTickTime(time);
	}

	/**
	 * Adds effect to the consumer,
	 * Effects requiring register are registered into the scheduler one tick later
	 *
	 * @param iEffect
	 * @param consumer
	 */
	@SuppressWarnings("unchecked")
	public void addEffect(IEffect iEffect, IEffectConsumer consumer, IEffectSourceProvider effectSourceProvider) {
		IEffectContainer eff = consumer.getEffect(iEffect.getName());
		if (PluginConfig.DEBUG.isDevelop()) {
			IEffectConsumer consumer1 = iEffect.getConsumer();
			if (consumer1 instanceof ActiveCharacter) {
				ActiveCharacter chara = (ActiveCharacter) consumer1;
				chara.getPlayer().sendMessage(Text.of("Adding effect: " + iEffect.getName() +
						" container: "  + (eff == null ? "null" : eff.getEffects().size()) + " provider: " + effectSourceProvider.getType().getClass().getSimpleName() ));
			}
		}
		if (eff == null) {
			eff = iEffect.constructEffectContainer();
			consumer.addEffect(eff);
			iEffect.onApply();
		} else if (eff.isStackable()) {
			eff.stackEffect(iEffect, effectSourceProvider);
		} else {
			eff.forEach((Consumer<IEffect>) this::stopEffect); //there should be always only one
			//on remove will be called one tick later.
			eff.getEffects().add(iEffect);
			iEffect.onApply();
		}

		iEffect.setEffectContainer(eff);
		iEffect.setEffectSourceProvider(effectSourceProvider);
		if (iEffect.requiresRegister()) {
			runEffect(iEffect);
		}
	}

	/**
	 * Removes effect from IEffectConsumer, and stops it. The effect will be removed from the scheduler next tick
	 *
	 * @param iEffect
	 * @param consumer
	 */
	public void removeEffect(IEffect iEffect, IEffectConsumer consumer) {
		IEffectContainer effect = consumer.getEffect(iEffect.getName());
        if (PluginConfig.DEBUG.isDevelop()) {
            IEffectConsumer consumer1 = iEffect.getConsumer();
            if (consumer1 instanceof ActiveCharacter) {
                ActiveCharacter chara = (ActiveCharacter) consumer1;
                chara.getPlayer().sendMessage(Text.of("Adding effect: " + iEffect.getName() +
                        " container: "  + (effect == null ? "null" : effect.getEffects().size())));
            }
        }
		if (effect != null) {
			removeEffectContainer(effect, iEffect, consumer);
			stopEffect(iEffect);
		}
	}

	public <T, E extends IEffect<T>> void removeEffectContainer(IEffectContainer<T, E> container, IEffectConsumer consumer) {
		container.forEach(a -> removeEffect(a, consumer));
	}

	protected void removeEffectContainer(IEffectContainer container, IEffect iEffect, IEffectConsumer consumer) {
		if (container == null)
			return;
		if (iEffect == container) {
			if (!iEffect.getConsumer().isDetached()) {
				iEffect.onRemove();
			}
			if (!consumer.isDetached())
				consumer.removeEffect(iEffect);
		} else if (container.getEffects().contains(iEffect)) {
			container.removeStack(iEffect);
			if (container.getEffects().isEmpty()) {
				if (!consumer.isDetached())
					consumer.removeEffect(container);
			}
		} else {

		}
	}

	/**
	 * Removes and stops the effect previously applied as item enchantement
	 *
	 * @param iEffect
	 * @param consumer
	 */
	@SuppressWarnings("unchecked")
	public void removeEffect(String iEffect, IEffectConsumer consumer, IEffectSourceProvider effectSource) {
		IEffectContainer effect = consumer.getEffect(iEffect);
		if (effect != null) {
			Iterator<IEffect> iterator = effect.getEffects().iterator();
			IEffect e;
			while (iterator.hasNext()) {
				e = iterator.next();
				if (e.getEffectSourceProvider() == effectSource) {
					removeEffectContainer(effect, e, consumer);
					stopEffect(e);
				}
			}
		}
	}

	/**
	 * Register global effect
	 *
	 * @param iGlobalEffect
	 */
	public void registerGlobalEffect(IGlobalEffect iGlobalEffect) {
		globalEffects.put(iGlobalEffect.getName().toLowerCase(), iGlobalEffect);
	}

	/**
	 * Removes cached globaleffect
	 *
	 * @param name
	 */
	public void removeGlobalEffect(String name) {
		name = name.toLowerCase();
        globalEffects.remove(name);
	}

	/**
	 * Returns global effect by its name, if effect does not exists return null
	 *
	 * @param name
	 * @return effect or null if key is not in the map
	 */
	public IGlobalEffect getGlobalEffect(String name) {
		return globalEffects.get(name.toLowerCase());
	}

	public Map<String, IGlobalEffect> getGlobalEffects() {
		return globalEffects;
	}

	/**
	 * Applies global effect with unlimited duration
	 *
	 * @param effect
	 * @param consumer
	 * @param value
	 */
	public void applyGlobalEffectAsEnchantment(IGlobalEffect effect, IEffectConsumer consumer, Map<String, String> value, IEffectSourceProvider effectSourceType) {
		IEffect construct = effect.construct(consumer, unlimited_duration, value);
		addEffect(construct, consumer, effectSourceType);
	}

	/**
	 * Applies global effects with unlimited duration
	 *
	 * @param map
	 * @param consumer
	 */
	public void applyGlobalEffectsAsEnchantments(Map<IGlobalEffect, EffectParams> map, IEffectConsumer consumer, IEffectSourceProvider effectSourceType) {
		map.forEach((e, l) ->
				applyGlobalEffectAsEnchantment(e, consumer, l, effectSourceType)
		);
	}


	public void removeGlobalEffectsAsEnchantments(Collection<IGlobalEffect> itemEffects, IActiveCharacter character, IEffectSourceProvider effectSourceProvider) {
		if (PluginConfig.DEBUG.isDevelop()) {
			character.sendMessage(Text.of(itemEffects.size() + " added echn. effects to remove queue."));
		}
		itemEffects.forEach((e) -> {
			removeEffect(e.getName(), character, effectSourceProvider);
		});
	}


	public boolean isGlobalEffect(String s) {
		return globalEffects.containsKey(s.toLowerCase());
	}

	@SuppressWarnings("unchecked")
	/**
	 * Called only in cases when entities dies, or players logs off
	 */
	public void removeAllEffects(IEffectConsumer<?> character) {
		Iterator<IEffectContainer<Object, IEffect<Object>>> iterator1 = character.getEffects().iterator();
		while (iterator1.hasNext()) {
			IEffectContainer<Object, IEffect<Object>> next = iterator1.next();
			Iterator<IEffect<Object>> iterator2 = next.getEffects().iterator();
			while (iterator2.hasNext()) {
				IEffect<Object> next1 = iterator2.next();
				pendingRemovals.add(next1);
				iterator2.remove();
			}
			iterator1.remove();
		}
	}
}


