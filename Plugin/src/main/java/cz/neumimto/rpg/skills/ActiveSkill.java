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

import cz.neumimto.rpg.configuration.Localizations;
import cz.neumimto.rpg.players.IActiveCharacter;

/**
 * Created by NeumimTo on 26.7.2015.
 */
public abstract class ActiveSkill extends AbstractSkill {

	@Override
	public SkillResult onPreUse(IActiveCharacter character) {
		ExtendedSkillInfo info = character.getSkillInfo(this);

		if (character.isSilenced() && !getSkillTypes().contains(SkillType.CAN_CAST_WHILE_SILENCED)) {
			character.sendMessage(Localizations.PLAYER_IS_SILENCED);
			return SkillResult.CASTER_SILENCED;
		}
		//todo
		return cast(character, info, null);
	}

	public abstract SkillResult cast(IActiveCharacter character, ExtendedSkillInfo info, SkillModifier modifier);
}
