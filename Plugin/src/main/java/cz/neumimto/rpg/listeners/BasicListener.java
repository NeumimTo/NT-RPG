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

package cz.neumimto.rpg.listeners;

import com.flowpowered.math.vector.Vector3i;
import cz.neumimto.core.ioc.Inject;
import cz.neumimto.rpg.IEntity;
import cz.neumimto.rpg.IEntityType;
import cz.neumimto.rpg.NEventContextKeys;
import cz.neumimto.rpg.ResourceLoader;
import cz.neumimto.rpg.configuration.PluginConfig;
import cz.neumimto.rpg.damage.DamageService;
import cz.neumimto.rpg.damage.ISkillDamageSource;
import cz.neumimto.rpg.effects.EffectService;
import cz.neumimto.rpg.effects.IEffect;
import cz.neumimto.rpg.entities.EntityService;
import cz.neumimto.rpg.events.*;
import cz.neumimto.rpg.exp.ExperienceService;
import cz.neumimto.rpg.inventory.InventoryService;
import cz.neumimto.rpg.inventory.runewords.RWService;
import cz.neumimto.rpg.players.CharacterService;
import cz.neumimto.rpg.players.ExperienceSource;
import cz.neumimto.rpg.players.ExperienceSources;
import cz.neumimto.rpg.players.IActiveCharacter;
import cz.neumimto.rpg.skills.ISkill;
import cz.neumimto.rpg.skills.NDamageType;
import cz.neumimto.rpg.skills.ProjectileProperties;
import cz.neumimto.rpg.skills.SkillService;
import cz.neumimto.rpg.utils.ItemStackUtils;
import cz.neumimto.rpg.utils.Utils;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.entity.damage.DamageType;
import org.spongepowered.api.event.cause.entity.damage.DamageTypes;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.IndirectEntityDamageSource;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.entity.living.humanoid.player.RespawnPlayerEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.world.chunk.UnloadChunkEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.entity.Hotbar;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

/**
 * Created by NeumimTo on 12.2.2015.
 */
@ResourceLoader.ListenerClass
public class BasicListener {

    @Inject
    private RWService rwService;

    @Inject
    private CharacterService characterService;

    @Inject
    private Game game;

    @Inject
    private InventoryService inventoryService;

    @Inject
    private EffectService effectService;

    @Inject
    private DamageService damageService;

    @Inject
    private EntityService entityService;

    @Inject
    private SkillService skillService;

    @Inject
    private ExperienceService experienceService;

    @Inject
    private CauseStackManager causeStackManager;

    @Listener(order = Order.BEFORE_POST)
    public void onAttack(InteractEntityEvent.Primary event) {
        if (event.isCancelled()) {
            return;
        }
        if (!Utils.isLivingEntity(event.getTargetEntity())) {
            return;
        }
        Optional<Player> first = event.getCause().first(Player.class);
        IActiveCharacter character = null;
        if (first.isPresent()) {
            character = characterService.getCharacter(first.get().getUniqueId());
            if (character.isStub()) {
                return;
            }
            Hotbar h = character.getPlayer().getInventory().query(QueryOperationTypes.INVENTORY_TYPE.of(Hotbar.class));
            int slotIndex = h.getSelectedSlotIndex();
            inventoryService.onLeftClick(character, h.getSelectedSlotIndex(), h.getSlot(new SlotIndex(slotIndex)).get());
        }

        IEntity entity = entityService.get(event.getTargetEntity());

        if (entity.getType() == IEntityType.CHARACTER) {
            IActiveCharacter target = characterService.getCharacter(event.getTargetEntity().getUniqueId());
            if (target.isStub() && !PluginConfig.ALLOW_COMBAT_FOR_CHARACTERLESS_PLAYERS) {
                event.setCancelled(true);
                return;
            }
            if (first.isPresent()) {
                if (character.getParty() == target.getParty() && !character.getParty().isFriendlyfire()) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @Listener
    public void onRightClick(InteractEntityEvent.Secondary event, @First(typeFilter = Player.class) Player pl) {

        Optional<ItemStack> itemInHand = pl.getItemInHand(HandTypes.MAIN_HAND);
        if (itemInHand.isPresent()) {
            ItemStack itemStack = itemInHand.get();
            if (ItemStackUtils.any_armor.contains(itemStack.getType())) {
                event.setCancelled(true); //restrict armor equip on rightclick
                return;
            } else {
                IActiveCharacter character = characterService.getCharacter(pl.getUniqueId());
                if (character.isStub()) {
                    return;
                }
                Hotbar h = pl.getInventory().query(QueryOperationTypes.INVENTORY_TYPE.of(Hotbar.class));
                int slotIndex = h.getSelectedSlotIndex();
                inventoryService.onRightClick(character, 0, h.getSlot(new SlotIndex(slotIndex)).get());
            }
        }

    }

    @Listener
    public void onBlockClick(InteractBlockEvent.Primary event) {
        Optional<Player> first = event.getCause().first(Player.class);
        if (first.isPresent()) {
            Player pl = first.get();
            IActiveCharacter character = characterService.getCharacter(pl.getUniqueId());
            if (character.isStub()) {
                return;
            }
            Hotbar h = pl.getInventory().query(QueryOperationTypes.INVENTORY_TYPE.of(Hotbar.class));
            int slotIndex = h.getSelectedSlotIndex();
            inventoryService.onLeftClick(character, slotIndex, h.getSlot(new SlotIndex(slotIndex)).get());
        }
    }

    @Listener
    public void onBlockRightClick(InteractBlockEvent.Secondary event, @First(typeFilter = Player.class) Player pl) {

        IActiveCharacter character = characterService.getCharacter(pl.getUniqueId());
        Optional<ItemStack> itemInHand = pl.getItemInHand(HandTypes.MAIN_HAND);
        if (itemInHand.isPresent() && ItemStackUtils.any_armor.contains(itemInHand.get().getType())) {
            event.setCancelled(true); //restrict armor equip on rightclick
            return;
        }
        if (character.isStub()) {
            return;
        }
        Hotbar h = pl.getInventory().query(QueryOperationTypes.INVENTORY_TYPE.of(Hotbar.class));
        int slotIndex = h.getSelectedSlotIndex();
        inventoryService.onRightClick(character, slotIndex, h.getSlot(new SlotIndex(slotIndex)).get());
    }


    @Listener
    public void onChunkDespawn(UnloadChunkEvent event) {
        entityService.remove(event.getTargetChunk().getEntities(Utils::isLivingEntity));
    }


    @Listener
    public void onWeaponDamage(DamageEntityEvent event, @First(typeFilter = EntityDamageSource.class) EntityDamageSource entityDamageSource) {

        if (entityDamageSource.getType() == NDamageType.DAMAGE_CHECK) {

            return;
        }

        Entity targetEntity = event.getTargetEntity();
        Entity source = entityDamageSource.getSource();
        if (source.get(Keys.HEALTH).isPresent()) {
            targetEntity.offer(Keys.INVULNERABILITY_TICKS, 0);
            //attacker
            IEntity entity = entityService.get(source);
            double newdamage = event.getBaseDamage();

            IActiveCharacter character = null;
            if (entity.getType() == IEntityType.CHARACTER) {
                character = (IActiveCharacter) entity;
                if (entityDamageSource.getType() == DamageTypes.ATTACK) {
                    INEntityWeaponDamageEvent e;
                    Hotbar hotbar = character.getPlayer().getInventory().query(Hotbar.class);
                /*    if (hotbar.getSelectedSlotIndex() != character.getSelectedHotbarSlot()) {
                        character.updateSelectedHotbarSlot();
                        damageService.recalculateCharacterWeaponDamage(character);
                    }
                  */  newdamage = character.getWeaponDamage();
                    newdamage *= damageService.getEntityBonusDamage(character, entityDamageSource.getType());
                    e = new CharacterWeaponDamageEvent(character, entityService.get(targetEntity), newdamage);
                    Sponge.getGame().getEventManager().post(e);
                    if (e.isCancelled() || e.getDamage() <= 0) {
                        event.setCancelled(true);
                        return;
                    }
                    event.setBaseDamage(e.getDamage());
                }
            } else {
                if (!PluginConfig.OVERRIDE_MOBS) {
                    newdamage = entityService.getMobDamage(source);
                }
                newdamage *= damageService.getEntityBonusDamage(entity, entityDamageSource.getType());
                if (entityDamageSource.getType() == DamageTypes.ATTACK) {
                    INEntityWeaponDamageEvent e =
                            new INEntityWeaponDamageEvent(entityService.get(source), entityService.get(targetEntity), newdamage);
                    Sponge.getGame().getEventManager().post(e);
                    if (e.isCancelled() || e.getDamage() <= 0) {
                        event.setCancelled(true);
                        return;
                    }
                    event.setBaseDamage(e.getDamage());
                }
            }
            //todo
            //defende
                /*
                if (targetEntity.getHotbarObjectType() == EntityTypes.PLAYER) {
                    IActiveCharacter tcharacter = characterService.getCharacter(targetEntity.getUniqueId());
                    double armor = tcharacter.getArmorValue();
                    final double damagefactor = damageService.DamageArmorReductionFactor.apply(newdamage, armor);
                    event.setBaseDamage(ce.getDamage());
                    event.setDamage(DamageModifier.builder().cause(Cause.ofNullable(null)).type(DamageModifierTypes.ARMOR).build(), input -> input
                    * ce.getDamagefactor());
                }*/
        }
    }

    @Listener
    public void onIndirectEntityDamage(DamageEntityEvent event,
            @Root(typeFilter = IndirectEntityDamageSource.class)
                    IndirectEntityDamageSource indirectEntityDamageSource) {

        Projectile projectile = (Projectile) indirectEntityDamageSource.getSource();
        IEntity shooter = entityService.get((Entity) projectile.getShooter());
        IEntity target = entityService.get(event.getTargetEntity());
        ProjectileProperties projectileProperties = ProjectileProperties.cache.get(projectile);
        if (projectileProperties != null) {
            event.setCancelled(true);
            ProjectileProperties.cache.remove(projectile);
            projectileProperties.consumer.accept(event, shooter, target);
            return;
        }

        double projectileDamage = 0;
        if (shooter.getType() == IEntityType.CHARACTER) {
            IActiveCharacter c = (IActiveCharacter) shooter;
            projectileDamage = damageService.getCharacterProjectileDamage(c, projectile.getType());
        } else if (shooter.getType() == IEntityType.MOB) {
            if (!PluginConfig.OVERRIDE_MOBS) {
                projectileDamage = entityService.getMobDamage(shooter.getEntity());
            }
        }

        ProjectileHitEvent event1 = new ProjectileHitEvent(shooter, target, projectileDamage, projectile);
        Sponge.getGame().getEventManager().post(event1);
        if (event1.isCancelled() || event1.getProjectileDamage() <= 0) {
            event.setCancelled(true);
            return;
        }

        event.setBaseDamage(event1.getProjectileDamage());
    }

    @Listener
    public void onSkillDamage(DamageEntityEvent event,
            @Root(typeFilter = ISkillDamageSource.class)
                    ISkillDamageSource iSkillDamageSource) {
        IEntity caster = iSkillDamageSource.getCaster();
        ISkill skill = iSkillDamageSource.getSkill();
        DamageType type = iSkillDamageSource.getType();
        IEffect effect = iSkillDamageSource.getEffect();
        if (caster.getType() == IEntityType.CHARACTER) {
            IActiveCharacter c = (IActiveCharacter) caster;
            if (c.hasPreferedDamageType()) {
                type = c.getDamageType();
            }
        }
        IEntity targetchar = entityService.get(event.getTargetEntity());
        double finalDamage = event.getOriginalDamage() * damageService.getEntityBonusDamage(caster, type);


        try (CauseStackManager.StackFrame frame = causeStackManager.pushCauseFrame()) {
            if (effect != null) {
                EventContext build = EventContext.builder().add(NEventContextKeys.EFFECT, effect).build();
                causeStackManager.pushCause(Cause.of(build, effect));
            }

            SkillDamageEvent event1 = new SkillDamageEvent(caster, targetchar, skill, finalDamage, type);
            if (skill != null) {
                EventContext build = EventContext.builder().add(NEventContextKeys.SKILL, skill).build();
                causeStackManager.pushCause(Cause.of(build, skill));
            }

            Sponge.getGame().getEventManager().post(event1);
            if (event1.isCancelled() || event1.getDamage() <= 0) {
                event.setCancelled(true);
                return;
            }

            finalDamage = event1.getDamage();
            double target_resistence = damageService.getEntityResistance(targetchar, type);
            finalDamage *= target_resistence;
            SkillDamageEventLate event2 = new SkillDamageEventLate(caster, targetchar, skill, finalDamage, target_resistence, type);
            event2.setCause(causeStackManager.getCurrentCause());


            Sponge.getGame().getEventManager().post(event2);
            if (event2.isCancelled() || event2.getDamage() <= 0) {
                event.setCancelled(true);
                return;
            }
            event.setBaseDamage(event2.getDamage() * event2.getTargetResistance());
        }
    }


    @Listener
    public void onRespawn(RespawnPlayerEvent event) {
        Entity type = event.getTargetEntity();
        if (type.getType() == EntityTypes.PLAYER) {
            IActiveCharacter character = characterService.getCharacter(type.getUniqueId());
            if (character.isStub()) {
                return;
            }
            characterService.respawnCharacter(character, event.getTargetEntity());

        }
    }


    @Listener
    public void onBlockBreak(ChangeBlockEvent.Break event, @First(typeFilter = Player.class) Player player) {
        if (event.isCancelled()) {
            return;
        }

        IActiveCharacter character = characterService.getCharacter(player.getUniqueId());
        for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
            BlockType type = transaction.getOriginal().getState().getType();
            Double d = experienceService.getMinningExperiences(type);
            if (d != null) {
                characterService.addExperiences(character, d, ExperienceSources.MINING);
            } else {
                d = experienceService.getLoggingExperiences(type);
                if (d != null) {
                    characterService.addExperiences(character, d, ExperienceSources.LOGGING);
                }
            }
        }
    }


    //TODO sponge bug as of 7.1-BETA-15
    /*
    @Listener
    public void onItemForge(ClickInventoryEvent event, @First Player player) {
        Container i = event.getTargetInventory();
        if (i.getArchetype() == InventoryArchetypes.ANVIL) {
            if (i instanceof BlockCarrier) {
                BlockCarrier b = (BlockCarrier) i;
                //on item insert
                if (isSocketingViable(b.getLocation())) {
                    Optional<ItemStack> peek = event.getTargetInventory().query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotIndex.of(0))).peek();
                    Optional<ItemStack> peek1 = event.getTargetInventory().query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotIndex.of(1))).peek();
                    if (peek.isPresent() && peek1.isPresent()) {

                        ItemStack rune = peek1.get();
                        if (!rwService.isRune(rune)) {
                            event.getTargetInventory().query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotIndex.of(2))).set(ItemStack.empty());
                            return;
                        }
                        ItemStack itemStack = peek.get();
                        ItemStack copy = itemStack.copy();
                        ItemUpgradeTransactionResult result = rwService.insertToNextEmptySocket(copy, rune);
                        if (result != ItemUpgradeTransactionResult.OK) {
                            event.getTargetInventory().query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotIndex.of(2))).set(ItemStack.empty());
                        } else {
                            event.getTargetInventory().query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotIndex.of(2))).set(copy);
                        }
                    } else {
                        event.getTargetInventory().query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotIndex.of(2))).set(ItemStack.empty());
                    }
                }
                for (SlotTransaction slotTransaction : event.getTransactions()) {
                    Slot slot = slotTransaction.getSlot();
                }
            }
        }
    }
    */


    private boolean isSocketingViable(Location<World> location) {
        World extent = location.getExtent();
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) {
                    continue;
                }
                Vector3i vector3i = Vector3i.from(location.getBlockX() + i, location.getBlockY(), location.getBlockZ() + j);
                if (extent.getBlock(vector3i).getType() != BlockTypes.REDSTONE_WIRE) {
                    return false;
                }
            }
        }
        return true;
    }

}
