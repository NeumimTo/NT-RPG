package cz.neumimto.rpg.sponge.skills.passive;

import cz.neumimto.rpg.api.ResourceLoader;
import cz.neumimto.rpg.api.effects.EffectService;
import cz.neumimto.rpg.api.entity.players.IActiveCharacter;
import cz.neumimto.rpg.api.skills.PlayerSkillContext;
import cz.neumimto.rpg.api.skills.tree.SkillType;
import cz.neumimto.rpg.api.skills.types.PassiveSkill;
import cz.neumimto.rpg.sponge.effects.ResoluteTechniqueEffect;
import org.spongepowered.api.event.cause.entity.damage.DamageTypes;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by NeumimTo on 6.7.2017.
 */
@Singleton
@ResourceLoader.Skill("ntrpg:resolutetechnique")
public class ResoluteTechnique extends PassiveSkill {

    @Inject
    private EffectService effectService;

    public ResoluteTechnique() {
        super(ResoluteTechniqueEffect.name);
        setDamageType(DamageTypes.ATTACK.getId());
        addSkillType(SkillType.PHYSICAL);
    }

    @Override
    public void applyEffect(PlayerSkillContext info, IActiveCharacter character) {
        ResoluteTechniqueEffect effect = new ResoluteTechniqueEffect(character, -1);
        effectService.addEffect(effect, this);
    }
}
