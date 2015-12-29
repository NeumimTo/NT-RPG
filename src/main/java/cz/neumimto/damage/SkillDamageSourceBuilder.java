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
package cz.neumimto.damage;

import cz.neumimto.players.IActiveCharacter;
import cz.neumimto.skills.ISkill;
import org.spongepowered.api.event.cause.entity.damage.source.common.AbstractDamageSourceBuilder;

/**
 * Created by NeumimTo on 29.12.2015.
 */
public class SkillDamageSourceBuilder extends AbstractDamageSourceBuilder<SkillDamageSource, SkillDamageSourceBuilder> {

    protected ISkill skill;
    protected IActiveCharacter caster;

    public ISkill getSkill() {
        return skill;
    }

    public void setSkill(ISkill skill) {
        this.skill = skill;
    }

    public IActiveCharacter getCaster() {
        return caster;
    }

    public void setCaster(IActiveCharacter caster) {
        this.caster = caster;
    }

    @Override
    public SkillDamageSource build() throws IllegalStateException {
        return new SkillDamageSource(this);
    }
}
