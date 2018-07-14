package cz.neumimto.skills.active;

import cz.neumimto.Decorator;
import cz.neumimto.core.ioc.Inject;
import cz.neumimto.rpg.ResourceLoader;
import cz.neumimto.rpg.entities.EntityService;
import cz.neumimto.rpg.players.IActiveCharacter;
import cz.neumimto.rpg.skills.ActiveSkill;
import cz.neumimto.rpg.skills.ExtendedSkillInfo;
import cz.neumimto.rpg.skills.SkillModifier;
import cz.neumimto.rpg.skills.SkillNodes;
import cz.neumimto.rpg.skills.SkillResult;
import cz.neumimto.rpg.skills.SkillSettings;
import cz.neumimto.rpg.skills.SkillType;
import org.spongepowered.api.item.ItemTypes;

/**
 * Created by NeumimTo on 6.8.2017.
 */
@ResourceLoader.Skill("ntrpg:groupheal")
public class GroupHeal extends ActiveSkill {

	@Inject
	private EntityService entityService;

	public void init() {
		super.init();
		SkillSettings settings = new SkillSettings();
		settings.addNode(SkillNodes.RADIUS, 10, 10);
		settings.addNode(SkillNodes.HEALED_AMOUNT, 10, 10);
		setSettings(settings);
		addSkillType(SkillType.HEALING);
		addSkillType(SkillType.AOE);
		setIcon(ItemTypes.PAPER);
	}

	@Override
	public SkillResult cast(IActiveCharacter character, ExtendedSkillInfo info, SkillModifier modifier) {
		float amnt = getFloatNodeValue(info, SkillNodes.HEALED_AMOUNT);
		if (character.hasParty()) {
			double rad = Math.pow(getDoubleNodeValue(info, SkillNodes.RADIUS), 2);
			for (IActiveCharacter a : character.getParty().getPlayers()) {
				if (a.getLocation().getPosition().distanceSquared(character.getLocation().getPosition()) <= rad) {
					entityService.healEntity(a, amnt, this);
					Decorator.healEffect(a.getLocation());
				}
			}
		} else {
			entityService.healEntity(character, amnt, this);
			Decorator.healEffect(character.getEntity().getLocation().add(0,1,0));
		}

		return SkillResult.OK;
	}
}
