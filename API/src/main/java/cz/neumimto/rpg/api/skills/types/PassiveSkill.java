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

package cz.neumimto.rpg.api.skills.types;

import cz.neumimto.rpg.api.effects.EffectService;
import cz.neumimto.rpg.api.entity.players.IActiveCharacter;
import cz.neumimto.rpg.api.inventory.InventoryService;
import cz.neumimto.rpg.api.localization.Arg;
import cz.neumimto.rpg.api.localization.LocalizationKeys;
import cz.neumimto.rpg.api.skills.PlayerSkillContext;
import cz.neumimto.rpg.api.skills.SkillExecutionType;
import cz.neumimto.rpg.api.skills.SkillResult;
import cz.neumimto.rpg.api.skills.scripting.JsBinding;

import javax.inject.Inject;

/**
 * Created by NeumimTo on 6.8.2015.
 */
@JsBinding(JsBinding.Type.CLASS)
public abstract class PassiveSkill extends AbstractSkill<IActiveCharacter> {

    @Inject
    protected EffectService effectService;

    @Inject
    protected InventoryService inventoryService;

    protected String relevantEffectName;

    public PassiveSkill() {
    }

    public PassiveSkill(String name) {
        this.relevantEffectName = name;
    }

    @Override
    public SkillResult onPreUse(IActiveCharacter character, PlayerSkillContext esi) {
        PlayerSkillContext info = character.getSkillInfo(this);
        String msg = localizationService.translate(LocalizationKeys.CANT_USE_PASSIVE_SKILL,
                Arg.arg("skill", info.getSkillData().getSkillName()));
        character.sendMessage(msg);
        return SkillResult.CANCELLED;
    }

    private void update(IActiveCharacter IActiveCharacter) {
        inventoryService.initializeCharacterInventory(IActiveCharacter);
        PlayerSkillContext skill = IActiveCharacter.getSkill(getId());
        applyEffect(skill, IActiveCharacter);
    }

    @Override
    public void onCharacterInit(IActiveCharacter c, int level, PlayerSkillContext context) {
        super.onCharacterInit(c, level, context);
        update(c);
    }

    @Override
    public void skillLearn(IActiveCharacter IActiveCharacter, PlayerSkillContext context) {
        super.skillLearn(IActiveCharacter, context);
        update(IActiveCharacter);
    }

    @Override
    public void skillRefund(IActiveCharacter IActiveCharacter, PlayerSkillContext context) {
        super.skillRefund(IActiveCharacter, context);
        PlayerSkillContext skillInfo = IActiveCharacter.getSkillInfo(this);
        if (skillInfo.getLevel() <= 0) {
            effectService.removeEffect(relevantEffectName, IActiveCharacter, this);
        } else {
            update(IActiveCharacter);
        }
    }

    public abstract void applyEffect(PlayerSkillContext info, IActiveCharacter character);

    @Override
    public SkillExecutionType getSkillExecutionType() {
        return SkillExecutionType.PASSIVE;
    }
}
