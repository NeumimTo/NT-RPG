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

package cz.neumimto.rpg.api.entity.players;

import cz.neumimto.rpg.api.configuration.AttributeConfig;
import cz.neumimto.rpg.api.effects.EffectType;
import cz.neumimto.rpg.api.effects.IEffect;
import cz.neumimto.rpg.api.effects.IEffectContainer;
import cz.neumimto.rpg.api.entity.EntityHand;
import cz.neumimto.rpg.api.entity.IEntity;
import cz.neumimto.rpg.api.entity.IEntityType;
import cz.neumimto.rpg.api.entity.IReservable;
import cz.neumimto.rpg.api.entity.players.classes.ClassDefinition;
import cz.neumimto.rpg.api.entity.players.classes.PlayerClassData;
import cz.neumimto.rpg.api.entity.players.party.IParty;
import cz.neumimto.rpg.api.gui.SkillTreeViewModel;
import cz.neumimto.rpg.api.inventory.ManagedSlot;
import cz.neumimto.rpg.api.inventory.RpgInventory;
import cz.neumimto.rpg.api.items.RpgItemStack;
import cz.neumimto.rpg.api.items.RpgItemType;
import cz.neumimto.rpg.api.persistance.model.CharacterBase;
import cz.neumimto.rpg.api.persistance.model.EquipedSlot;
import cz.neumimto.rpg.api.skills.ISkill;
import cz.neumimto.rpg.api.skills.PlayerSkillContext;
import cz.neumimto.rpg.api.skills.preprocessors.InterruptableSkillPreprocessor;
import cz.neumimto.rpg.api.skills.tree.SkillTreeSpecialization;

import java.util.*;

/**
 * Created by NeumimTo on 23.7.2015.
 */
public interface IActiveCharacter<T, P extends IParty> extends IEntity<T> {

    Map<String, PlayerClassData> getClasses();

    UUID getUUID();

    default PlayerClassData getClassByType(String type) {
        for (PlayerClassData value : getClasses().values()) {
            if (value.getClassDefinition().getClassType().equalsIgnoreCase(type)) {
                return value;
            }
        }
        return null;
    }

    default boolean hasEffectType(EffectType effectType) {
        for (IEffectContainer<Object, IEffect<Object>> container : getEffectMap().values()) {
            for (IEffect effect : container.getEffects()) {
                if (effect.getEffectTypes().contains(effectType)) {
                    return true;
                }
            }
        }
        return false;
    }

    Map<Class<?>, RpgInventory> getManagedInventory();

    P getParty();

    void setParty(P party);

    String getName();

    boolean isStub();

    float[] getPrimaryProperties();

    void setCharacterLevelProperty(int index, float value);

    float getCharacterPropertyWithoutLevel(int index);

    IReservable getMana();

    void setMana(IReservable mana);

    void setHealth(IReservable health);

    int getAttributePoints();

    void setAttributePoints(int attributePoints);

    int getAttributeValue(String name);

    default Integer getAttributeValue(AttributeConfig attribute) {
        return getAttributeValue(attribute.getId());
    }

    Map<String, Long> getCooldowns();

    default Long getCooldown(String action) {
        return getCooldowns().get(action);
    }

    boolean hasCooldown(String thing);

    double getBaseWeaponDamage(RpgItemType type);

    Set<RpgItemType> getAllowedArmor();

    boolean canWear(RpgItemType armor);

    Map<RpgItemType, Double> getAllowedWeapons();

    Map<String, Double> getProjectileDamages();

    CharacterBase getCharacterBase();

    PlayerClassData getPrimaryClass();

    double getBaseProjectileDamage(String id);

    IActiveCharacter updateItemRestrictions();

    Map<String, PlayerSkillContext> getSkills();

    PlayerSkillContext getSkillInfo(ISkill skill);

    boolean hasSkill(String name);

    int getLevel();

    PlayerSkillContext getSkillInfo(String s);

    boolean isSilenced();

    Map<String, PlayerSkillContext> getSkillsByName();

    void addSkill(String name, PlayerSkillContext info);

    PlayerSkillContext getSkill(String skillName);

    void removeAllSkills();

    boolean hasParty();

    boolean isInPartyWith(IActiveCharacter character);

    boolean isUsingGuiMod();

    void setUsingGuiMod(boolean b);

    boolean isPartyLeader();

    P getPendingPartyInvite();

    void setPendingPartyInvite(P party);

    boolean canUse(RpgItemType weaponItemType, EntityHand h);

    double getWeaponDamage();

    void setWeaponDamage(double damage);

    double getArmorValue();

    void setArmorValue(double value);

    boolean hasPreferedDamageType();

    String getDamageType();

    void setDamageType(String damageType);

    void updateLastKnownLocation(int x, int y, int z, String name);

    boolean isInvulnerable();

    void setInvulnerable(boolean b);

    RpgItemStack getMainHand();

    int getMainHandSlotId();

    void setMainHand(RpgItemStack customItem, int slot);

    RpgItemStack getOffHand();

    void setOffHand(RpgItemStack customItem);

    @Override
    default IEntityType getType() {
        return IEntityType.CHARACTER;
    }

    void sendMessage(String message);

    float[] getSecondaryProperties();

    void setSecondaryProperties(float[] arr);

    Map<String, Integer> getTransientAttributes();

    boolean hasClass(ClassDefinition configClass);

    List<Integer> getSlotsToReinitialize();

    void setSlotsToReinitialize(List<Integer> slotsToReinitialize);

    void addSkillTreeSpecialization(SkillTreeSpecialization specialization);

    void removeSkillTreeSpecialization(SkillTreeSpecialization specialization);

    boolean hasSkillTreeSpecialization(SkillTreeSpecialization specialization);

    Set<SkillTreeSpecialization> getSkillTreeSpecialization();

    Set<EquipedSlot> getSlotsCannotBeEquiped();

    double getExperienceBonusFor(String dimmension, String type);

    void addClass(PlayerClassData playerClassData);

    void restartAttributeGuiSession();

    default void getMinimalInventoryRequirements(Map<AttributeConfig, Integer> seed) {

        Map<Class<?>, RpgInventory> managedInventory = getManagedInventory();
        for (RpgInventory inv : managedInventory.values()) {

            for (ManagedSlot value : inv.getManagedSlots().values()) {
                Optional<RpgItemStack> content = value.getContent();
                if (content.isPresent()) {
                    RpgItemStack rpgItemStack = content.get();
                    Map<AttributeConfig, Integer> minimalAttributeRequirements = rpgItemStack.getMinimalAttributeRequirements();

                    for (Map.Entry<AttributeConfig, Integer> entry : minimalAttributeRequirements.entrySet()) {
                        seed.compute(entry.getKey(), (attribute, integer) -> Math.max(integer, entry.getValue()));
                    }
                }
            }
        }
    }

    boolean requiresDamageRecalculation();

    void setRequiresDamageRecalculation(boolean k);

    int getLastHotbarSlotInteraction();

    void setLastHotbarSlotInteraction(int last);

    void sendNotification(String message);

    default PlayerClassData getClassByName(String name) {
        return getClasses().get(name.toLowerCase());
    }

    void setChanneledSkill(InterruptableSkillPreprocessor o);

    Optional<InterruptableSkillPreprocessor> getChanneledSkill();

    Map<String, Integer> getAttributesTransaction();

    void setAttributesTransaction(HashMap<String, Integer> map);

    String getPlayerAccountName();

    Map<String, ? extends SkillTreeViewModel> getSkillTreeViewLocation();

    SkillTreeViewModel getLastTimeInvokedSkillTreeView();


}
