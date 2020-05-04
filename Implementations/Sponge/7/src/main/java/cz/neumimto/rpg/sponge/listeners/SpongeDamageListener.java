package cz.neumimto.rpg.sponge.listeners;

import com.google.inject.Singleton;
import cz.neumimto.rpg.api.ResourceLoader;
import cz.neumimto.rpg.api.Rpg;
import cz.neumimto.rpg.api.configuration.PluginConfig;
import cz.neumimto.rpg.api.effects.IEffect;
import cz.neumimto.rpg.api.entity.IEntity;
import cz.neumimto.rpg.api.entity.IEntityType;
import cz.neumimto.rpg.api.entity.players.IActiveCharacter;
import cz.neumimto.rpg.api.items.RpgItemStack;
import cz.neumimto.rpg.api.skills.ISkill;
import cz.neumimto.rpg.common.damage.AbstractDamageListener;
import cz.neumimto.rpg.sponge.damage.SkillDamageSource;
import cz.neumimto.rpg.sponge.damage.SpongeDamageService;
import cz.neumimto.rpg.sponge.entities.SpongeEntityService;
import cz.neumimto.rpg.sponge.events.damage.*;
import cz.neumimto.rpg.sponge.inventory.SpongeItemService;
import cz.neumimto.rpg.sponge.skills.ProjectileProperties;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.ArmorEquipable;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.cause.entity.damage.DamageType;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.IndirectEntityDamageSource;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.filter.cause.First;

import javax.inject.Inject;
import java.util.Optional;


@Singleton
@ResourceLoader.ListenerClass
public class SpongeDamageListener extends AbstractDamageListener {

    @Inject
    private SpongeDamageService spongeDamageService;

    @Inject
    private SpongeEntityService entityService;

    @Inject
    private SpongeItemService itemService;

    @Inject
    private CauseStackManager causeStackManager;

    @Listener(order = Order.EARLY, beforeModifications = true)
    public void onEntityDamageEarly(DamageEntityEvent event, @First EntityDamageSource damageSource) {
        IEntity target = entityService.get(event.getTargetEntity());
        IEntity attacker;
        if (damageSource instanceof IndirectEntityDamageSource) {
            attacker = entityService.get(((IndirectEntityDamageSource) damageSource).getIndirectSource());
        } else attacker = entityService.get(damageSource.getSource());

        if (damageSource instanceof SkillDamageSource) {
            processSkillDamageEarly(event, (SkillDamageSource) damageSource, attacker, target);
        } else if (damageSource instanceof IndirectEntityDamageSource) {
            IndirectEntityDamageSource indirectEntityDamageSource = (IndirectEntityDamageSource) damageSource;
            Projectile projectile = (Projectile) indirectEntityDamageSource.getSource();
            ProjectileProperties projectileProperties = ProjectileProperties.cache.get(projectile);
            if (projectileProperties != null) {
                event.setCancelled(true);
                ProjectileProperties.cache.remove(projectile);
                projectileProperties.consumer.accept(event, attacker, target);
                return;
            }

            processProjectileDamageEarly(event, indirectEntityDamageSource, attacker, target, projectile);
        } else {
            processWeaponDamageEarly(event, damageSource, attacker, target);
        }
    }

    @Listener(order = Order.LATE)
    public void onEntityDamageLate(DamageEntityEvent event, @First EntityDamageSource damageSource) {
        IEntity target = entityService.get(event.getTargetEntity());
        IEntity attacker;
        if (damageSource instanceof IndirectEntityDamageSource) {
            attacker = entityService.get(((IndirectEntityDamageSource) damageSource).getIndirectSource());
        } else attacker = entityService.get(damageSource.getSource());

        if (damageSource instanceof SkillDamageSource) {
            processSkillDamageLate(event, (SkillDamageSource) damageSource, attacker, target);
        } else if (damageSource instanceof IndirectEntityDamageSource) {
            processProjectileDamageLate(event, (IndirectEntityDamageSource) damageSource, attacker, target, (Projectile) damageSource.getSource());
        } else {
            processWeaponDamageLate(event, damageSource, attacker, target);
        }
    }

    private void processWeaponDamageEarly(DamageEntityEvent event, EntityDamageSource source, IEntity attacker, IEntity target) {
        double newdamage = event.getBaseDamage();

        RpgItemStack rpgItemStack = null;
        if (attacker.getType() == IEntityType.CHARACTER) {
            IActiveCharacter character = (IActiveCharacter) attacker;
            if (character.requiresDamageRecalculation()) {
                RpgItemStack mainHand = character.getMainHand();
                spongeDamageService.recalculateCharacterWeaponDamage(character, mainHand);
                character.setRequiresDamageRecalculation(false);
            }
            newdamage = character.getWeaponDamage();
            rpgItemStack = character.getMainHand();
        } else {
            Living attackerEntity = (Living) attacker.getEntity();
            PluginConfig pluginConfig = Rpg.get().getPluginConfig();
            if (!pluginConfig.OVERRIDE_MOBS && entityService.handleMobDamage(attackerEntity.getWorld().getName(), attackerEntity.getUniqueId())) {
                newdamage = entityService.getMobDamage(attackerEntity);
            }
            if (attackerEntity instanceof ArmorEquipable) {
                Optional<RpgItemStack> rpgItemStack1 = ((ArmorEquipable) attackerEntity).getItemInHand(HandTypes.MAIN_HAND)
                        .map(itemStack -> itemService.getRpgItemStack(itemStack)).get();
                if (rpgItemStack1.isPresent()) {
                    rpgItemStack = rpgItemStack1.get();
                }
            }
        }
        newdamage *= spongeDamageService.getDamageHandler().getEntityDamageMult(attacker, source.getType().getName());

        SpongeEntityWeaponDamageEarlyEvent e = Rpg.get().getEventFactory().createEventInstance(SpongeEntityWeaponDamageEarlyEvent.class);
        e.setTarget(target);
        e.setDamage(newdamage);
        e.setWeapon(rpgItemStack);

        e.setCause(causeStackManager.getCurrentCause());

        if (Rpg.get().postEvent(e)) {
            event.setCancelled(true);
            return;
        }

        if (e.getDamage() <= 0) {
            event.setCancelled(true);
            return;
        }

        event.setBaseDamage(e.getDamage());
    }

    private void processWeaponDamageLate(DamageEntityEvent event, EntityDamageSource source, IEntity attacker, IEntity target) {
        double newdamage = event.getBaseDamage();
        newdamage *= spongeDamageService.getDamageHandler().getEntityResistance(target, source.getType().getName());

        RpgItemStack rpgItemStack = null;
        if (attacker.getType() == IEntityType.CHARACTER) {
            IActiveCharacter character = (IActiveCharacter) attacker;
            rpgItemStack = character.getMainHand();
        } else {
            Living entity = (Living) attacker.getEntity();
            if (entity instanceof ArmorEquipable) {
                Optional<RpgItemStack> rpgItemStack1 = ((ArmorEquipable) entity).getItemInHand(HandTypes.MAIN_HAND)
                        .map(itemStack -> itemService.getRpgItemStack(itemStack)).get();
                if (rpgItemStack1.isPresent()) {
                    rpgItemStack = rpgItemStack1.get();
                }
            }
        }

        SpongeEntityWeaponDamageLateEvent e = Rpg.get().getEventFactory().createEventInstance(SpongeEntityWeaponDamageLateEvent.class);
        e.setTarget(target);
        e.setDamage(newdamage);
        e.setWeapon(rpgItemStack);

        e.setCause(causeStackManager.getCurrentCause());

        if (Rpg.get().postEvent(e)) {
            event.setCancelled(true);
            return;
        }

        if (e.getDamage() <= 0) {
            event.setCancelled(true);
            return;
        }

        event.setBaseDamage(e.getDamage());
    }

    private void processSkillDamageEarly(DamageEntityEvent event, SkillDamageSource source, IEntity attacker, IEntity target) {
        ISkill skill = source.getSkill();
        DamageType type = source.getType();
        IEffect effect = source.getEffect();
        if (attacker.getType() == IEntityType.CHARACTER) {
            IActiveCharacter c = (IActiveCharacter) attacker;
            if (c.hasPreferedDamageType()) {
                type = spongeDamageService.damageTypeById(c.getDamageType());
            }
        }
        double newdamage = event.getBaseDamage() * spongeDamageService.getDamageHandler().getEntityDamageMult(attacker, type.getName());

        try (CauseStackManager.StackFrame frame = causeStackManager.pushCauseFrame()) {
            if (effect != null) {
                causeStackManager.pushCause(effect);
            }
            if (skill != null) {
                causeStackManager.pushCause(skill);
            }

            SpongeEntitySkillDamageEarlyEvent e = Rpg.get().getEventFactory().createEventInstance(SpongeEntitySkillDamageEarlyEvent.class);
            e.setTarget(target);
            e.setDamage(newdamage);
            e.setSkill(skill);

            e.setCause(causeStackManager.getCurrentCause());

            if (Rpg.get().postEvent(e)) {
                event.setCancelled(true);
                return;
            }

            if (e.getDamage() <= 0) {
                event.setCancelled(true);
                return;
            }

            event.setBaseDamage(e.getDamage());
        }
    }

    private void processSkillDamageLate(DamageEntityEvent event, SkillDamageSource source, IEntity attacker, IEntity target) {
        ISkill skill = source.getSkill();
        DamageType type = source.getType();
        IEffect effect = source.getEffect();
        double newdamage = event.getBaseDamage();

        try (CauseStackManager.StackFrame frame = causeStackManager.pushCauseFrame()) {
            if (effect != null) {
                causeStackManager.pushCause(effect);
            }
            if (skill != null) {
                causeStackManager.pushCause(skill);
            }
            newdamage *= spongeDamageService.getDamageHandler().getEntityResistance(target, type.getName());

            SpongeEntitySkillDamageLateEvent e = Rpg.get().getEventFactory().createEventInstance(SpongeEntitySkillDamageLateEvent.class);
            e.setTarget(target);
            e.setDamage(newdamage);
            e.setSkill(skill);

            e.setCause(causeStackManager.getCurrentCause());

            if (Rpg.get().postEvent(e)) {
                event.setCancelled(true);
                return;
            }

            if (e.getDamage() <= 0) {
                event.setCancelled(true);
                return;
            }

            event.setBaseDamage(e.getDamage());
        }
    }

    private void processProjectileDamageEarly(DamageEntityEvent event, IndirectEntityDamageSource source, IEntity attacker, IEntity target, Projectile projectile) {
        double newdamage = event.getBaseDamage();
        if (attacker.getType() == IEntityType.CHARACTER) {
            IActiveCharacter c = (IActiveCharacter) attacker;
            newdamage = spongeDamageService.getCharacterProjectileDamage(c, projectile.getType());
        } else if (attacker.getType() == IEntityType.MOB) {
            Living attackerEntity = (Living) attacker.getEntity();
            PluginConfig pluginConfig = Rpg.get().getPluginConfig();
            if (!pluginConfig.OVERRIDE_MOBS && entityService.handleMobDamage(attackerEntity.getWorld().getName(), attackerEntity.getUniqueId())) {
                newdamage = entityService.getMobDamage(attackerEntity);
            }
        }

        SpongeEntityProjectileDamageEarlyEvent e = Rpg.get().getEventFactory().createEventInstance(SpongeEntityProjectileDamageEarlyEvent.class);
        e.setTarget(target);
        e.setDamage(newdamage);
        e.setProjectile(projectile);

        e.setCause(causeStackManager.getCurrentCause());

        if (Rpg.get().postEvent(e)) {
            event.setCancelled(true);
            return;
        }

        if (e.getDamage() <= 0) {
            event.setCancelled(true);
            return;
        }

        event.setBaseDamage(e.getDamage());
    }

    private void processProjectileDamageLate(DamageEntityEvent event, IndirectEntityDamageSource source, IEntity attacker, IEntity target, Projectile projectile) {
        double newdamage = event.getBaseDamage();
        newdamage *= spongeDamageService.getDamageHandler().getEntityResistance(target, source.getType().getName());

        SpongeEntityProjectileDamageLateEvent e = Rpg.get().getEventFactory().createEventInstance(SpongeEntityProjectileDamageLateEvent.class);
        e.setTarget(target);
        e.setDamage(newdamage);
        e.setProjectile(projectile);

        e.setCause(causeStackManager.getCurrentCause());

        if (Rpg.get().postEvent(e)) {
            event.setCancelled(true);
            return;
        }

        if (e.getDamage() <= 0) {
            event.setCancelled(true);
            return;
        }

        event.setBaseDamage(e.getDamage());
    }

}
