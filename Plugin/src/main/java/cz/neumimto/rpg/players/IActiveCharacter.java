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

package cz.neumimto.rpg.players;

import cz.neumimto.core.localization.LocalizableParametrizedText;
import cz.neumimto.rpg.IEntity;
import cz.neumimto.rpg.IEntityType;
import cz.neumimto.rpg.inventory.RPGItemType;
import cz.neumimto.rpg.inventory.items.types.CustomItem;
import cz.neumimto.rpg.persistance.model.EquipedSlot;
import cz.neumimto.rpg.players.groups.ConfigClass;
import cz.neumimto.rpg.players.groups.Guild;
import cz.neumimto.rpg.players.groups.PlayerGroup;
import cz.neumimto.rpg.players.groups.Race;
import cz.neumimto.rpg.players.parties.Party;
import cz.neumimto.rpg.players.properties.attributes.ICharacterAttribute;
import cz.neumimto.rpg.skills.ExtendedSkillInfo;
import cz.neumimto.rpg.skills.ISkill;
import cz.neumimto.rpg.skills.SkillTreeSpecialization;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.entity.damage.DamageType;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by NeumimTo on 23.7.2015.
 */
public interface IActiveCharacter extends IEntity<Player> {

	Party getParty();

	void setParty(Party party);

	String getName();

	boolean isStub();

	float[] getCharacterProperties();

	void setProperties(float[] arr);

	void setCharacterLevelProperty(int index, float value);

	float getCharacterPropertyWithoutLevel(int index);

	double getMaxMana();

	void setMaxMana(float mana);

	void setMaxHealth(float maxHealth);

	void setHealth(float maxHealth);

	IReservable getMana();

	void setMana(IReservable mana);

	void setHealth(IReservable health);

	Map<EquipedSlot, CustomItem> getEquipedInventorySlots();

	double getExperiencs();

	void addExperiences(double exp, ExperienceSource source);

	Player getPlayer();

	void setPlayer(Player pl);

	void resetRightClicks();

	int getAttributePoints();

	void setAttributePoints(int attributePoints);

	Integer getAttributeValue(String name);

	default Integer getAttributeValue(ICharacterAttribute attribute) {
		return getAttributeValue(attribute.getId());
	}

	ExtendedNClass getPrimaryClass();

	void setPrimaryClass(ConfigClass clazz);

	Map<String, Long> getCooldowns();

	boolean hasCooldown(String thing);

	double getBaseWeaponDamage(RPGItemType type);

	Set<RPGItemType> getAllowedArmor();

	boolean canWear(RPGItemType armor);

	Map<ItemType, RPGItemWrapper> getAllowedWeapons();

	Map<EntityType, Double> getProjectileDamages();

	Set<ExtendedNClass> getClasses();

	ConfigClass getNClass(int index);

	Race getRace();

	void setRace(Race race);

	Guild getGuild();

	void setGuild(Guild guild);

	CharacterBase getCharacterBase();

	void setClass(ConfigClass nclass, int slot);

	double getBaseProjectileDamage(EntityType id);

	IActiveCharacter updateItemRestrictions();

	Map<String, ExtendedSkillInfo> getSkills();

	ExtendedSkillInfo getSkillInfo(ISkill skill);

	boolean hasSkill(String name);

	int getLevel();

	ExtendedSkillInfo getSkillInfo(String s);

	boolean isSilenced();

	void addSkill(String name, ExtendedSkillInfo info);

	ExtendedSkillInfo getSkill(String skillName);

	void getRemoveAllSkills();

	boolean hasParty();

	boolean isInPartyWith(IActiveCharacter character);

	boolean isUsingGuiMod();

	void setUsingGuiMod(boolean b);

	boolean isPartyLeader();

	Party getPendingPartyInvite();

	void setPendingPartyInvite(Party party);

	boolean canUse(RPGItemType weaponItemType, HandType h);

	double getWeaponDamage();

	void setWeaponDamage(double damage);

	double getArmorValue();

	void setArmorValue(double value);

	boolean hasPreferedDamageType();

	DamageType getDamageType();

	void setDamageType(DamageType damageType);

	void updateLastKnownLocation(int x, int y, int z, String name);

	boolean isInvulnerable();

	void setInvulnerable(boolean b);

	CustomItem getMainHand();

	int getMainHandSlotId();

	void setMainHand(CustomItem customItem, int slot);

	CustomItem getOffHand();

	void setOffHand(CustomItem customItem);

	@Override
	default IEntityType getType() {
		return IEntityType.CHARACTER;
	}

	@Override
	Player getEntity();

	void sendMessage(LocalizableParametrizedText message);

	float[] getCharacterLevelProperties();

	void setCharacterLevelProperties(float[] arr);

	Map<String, Integer> getTransientAttributes();

	MessageType getPreferedMessageType();

	void setPreferedMessageType(MessageType type);

	boolean hasClass(PlayerGroup configClass);

	List<Integer> getSlotsToReinitialize();

	void setSlotsToReinitialize(List<Integer> slotsToReinitialize);


	Map<String, SkillTreeViewModel> getSkillTreeViewLocation();

	SkillTreeViewModel getLastTimeInvokedSkillTreeView();

	void addSkillTreeSpecialization(SkillTreeSpecialization specialization);

	void removeSkillTreeSpecialization(SkillTreeSpecialization specialization);

	boolean hasSkillTreeSpecialization(SkillTreeSpecialization specialization);

	Set<EquipedSlot> getSlotsCannotBeEquiped();

    double getExperienceBonusFor(String name, EntityType type);

	default void sendMessage(Text t) {
		getPlayer().sendMessage(t);
	}
}
