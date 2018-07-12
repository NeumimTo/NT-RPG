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

package cz.neumimto.rpg.skills;

import cz.neumimto.core.ioc.Inject;
import cz.neumimto.rpg.configuration.Localizations;
import cz.neumimto.rpg.effects.EffectService;
import cz.neumimto.rpg.players.IActiveCharacter;

/**
 * Created by NeumimTo on 6.8.2015.
 */
public abstract class PassiveSkill extends AbstractSkill {

	@Inject
	protected EffectService effectService;

	protected String relevantEffectName;

	public PassiveSkill(String relevantEffectName) {
		this.relevantEffectName = relevantEffectName;
	}

	@Override
	public SkillResult onPreUse(IActiveCharacter character) {
		character.sendMessage(Localizations.CANT_USE_PASSIVE_SKILL);
		return SkillResult.FAIL;
	}

	private void update(IActiveCharacter IActiveCharacter) {
		ExtendedSkillInfo skill = IActiveCharacter.getSkill(getId());
		applyEffect(skill, IActiveCharacter);
	}

	@Override
	public void onCharacterInit(IActiveCharacter c, int level) {
		super.onCharacterInit(c, level);
		update(c);
	}

	@Override
	public void skillLearn(IActiveCharacter IActiveCharacter) {
		super.skillLearn(IActiveCharacter);
		update(IActiveCharacter);
	}

	@Override
	public void skillRefund(IActiveCharacter IActiveCharacter) {
		super.skillRefund(IActiveCharacter);
		ExtendedSkillInfo skillInfo = IActiveCharacter.getSkillInfo(this);
		if (skillInfo.getLevel() <= 0) {
			effectService.removeEffect(relevantEffectName, IActiveCharacter, this);
		} else {
			update(IActiveCharacter);
		}
	}

	public abstract void applyEffect(ExtendedSkillInfo info, IActiveCharacter character);
}
