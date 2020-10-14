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

package cz.neumimto.rpg.sponge.entities.players;

import cz.neumimto.rpg.api.Rpg;
import cz.neumimto.rpg.api.entity.CommonProperties;
import cz.neumimto.rpg.api.entity.IReservable;
import org.spongepowered.api.data.key.Keys;

/**
 * Created by NeumimTo on 30.12.2014.
 */
class SpongeCharacterHealth implements IReservable {

    private final SpongeCharacter activeCharacter;

    SpongeCharacterHealth(SpongeCharacter activeCharacter) {
        this.activeCharacter = activeCharacter;
    }

    @Override
    public double getMaxValue() {
        return activeCharacter.getPlayer().get(Keys.MAX_HEALTH).get();
    }

    @Override
    public void setMaxValue(double f) {
        activeCharacter.getPlayer().offer(Keys.MAX_HEALTH, f);
    }

    //todo useservice instead
    //todo implement reserved amounts
    @Override
    public void setReservedAmnout(float f) {
        activeCharacter.setProperty(CommonProperties.reserved_health, f);
    }

    @Override
    public double getReservedAmount() {
        return Rpg.get().getEntityService().getEntityProperty(activeCharacter, CommonProperties.reserved_health);
    }

    @Override
    public double getValue() {
        return activeCharacter.getPlayer().get(Keys.HEALTH).get();
    }

    @Override
    public void setValue(double f) {
        activeCharacter.getPlayer().offer(Keys.HEALTH, f);
    }

    @Override
    public double getRegen() {
        return Rpg.get().getEntityService().getEntityProperty(activeCharacter, CommonProperties.health_regen);
    }

    @Override
    public void setRegen(float f) {
        activeCharacter.setProperty(CommonProperties.health_regen, f);
    }
}
