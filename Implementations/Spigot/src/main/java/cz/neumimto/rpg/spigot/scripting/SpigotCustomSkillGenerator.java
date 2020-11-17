package cz.neumimto.rpg.spigot.scripting;

import cz.neumimto.rpg.common.skills.scripting.CustomSkillGenerator;
import cz.neumimto.rpg.spigot.effects.common.StunEffect;
import cz.neumimto.rpg.spigot.entities.players.ISpigotCharacter;
import cz.neumimto.rpg.spigot.skills.TargetedEntitySkill;
import cz.neumimto.rpg.spigot.skills.utils.Decorator;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.event.entity.EntityDamageEvent;

import javax.inject.Singleton;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
public class SpigotCustomSkillGenerator extends CustomSkillGenerator {

    @Override
    protected Object translateDamageType(String damageType) {

        for (EntityDamageEvent.DamageCause e : EntityDamageEvent.DamageCause.values()) {
            if (e.name().equalsIgnoreCase(damageType)) {
                return damageType;
            }
        }

        if ("PHYSICAL".equalsIgnoreCase(damageType)) {
            return EntityDamageEvent.DamageCause.ENTITY_ATTACK;
        }

        return null;
    }

    @Override
    protected String getDefaultEffectPackage() {
        return StunEffect.class.getPackage().getName();
    }

    @Override
    protected Type characterClassImpl() {
        return ISpigotCharacter.class;
    }

    @Override
    protected Class<?> targeted() {
        return TargetedEntitySkill.class;
    }

}
