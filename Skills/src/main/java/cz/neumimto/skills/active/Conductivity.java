package cz.neumimto.skills.active;

import cz.neumimto.rpg.ResourceLoader;
import cz.neumimto.rpg.players.IActiveCharacter;
import cz.neumimto.rpg.skills.ActiveSkill;
import cz.neumimto.rpg.skills.ExtendedSkillInfo;
import cz.neumimto.rpg.skills.SkillModifier;
import cz.neumimto.rpg.skills.SkillNodes;
import cz.neumimto.rpg.skills.SkillResult;
import cz.neumimto.rpg.skills.SkillSettings;
import cz.neumimto.rpg.skills.SkillType;

/**
 * Created by NeumimTo on 1.8.2017.
 */
@ResourceLoader.Skill("ntrpg:conductivity")
public class Conductivity extends ActiveSkill {

	public Conductivity() {
		SkillSettings settings = new SkillSettings();
		settings.addNode(SkillNodes.DURATION, 10000, 500);
		settings.addNode(SkillNodes.RADIUS, 10, 1);
		settings.addNode(SkillNodes.RANGE, 15, 1);
		super.settings = settings;
		addSkillType(SkillType.CURSE);
		addSkillType(SkillType.DECREASED_RESISTANCE);
	}

	@Override
	public SkillResult cast(IActiveCharacter character, ExtendedSkillInfo info, SkillModifier modifier) {
		return null;
	}
}
