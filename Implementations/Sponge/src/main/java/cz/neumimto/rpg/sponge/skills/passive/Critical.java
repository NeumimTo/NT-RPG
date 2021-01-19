package cz.neumimto.rpg.sponge.skills.passive;

import cz.neumimto.rpg.api.ResourceLoader;
import cz.neumimto.rpg.api.effects.EffectService;
import cz.neumimto.rpg.api.effects.IEffectContainer;
import cz.neumimto.rpg.api.entity.players.IActiveCharacter;
import cz.neumimto.rpg.api.skills.PlayerSkillContext;
import cz.neumimto.rpg.api.skills.SkillNodes;
import cz.neumimto.rpg.api.skills.tree.SkillType;
import cz.neumimto.rpg.api.skills.types.PassiveSkill;
import cz.neumimto.rpg.sponge.effects.positive.CriticalEffect;
import cz.neumimto.rpg.sponge.model.CriticalEffectModel;
import org.spongepowered.api.event.cause.entity.damage.DamageTypes;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by ja on 6.7.2017.
 */
@Singleton
@ResourceLoader.Skill("ntrpg:critical")
public class Critical extends PassiveSkill {

    @Inject
    private EffectService effectService;

    public Critical() {
        super(CriticalEffect.name);
        settings.addNode(SkillNodes.CHANCE, 10);
        settings.addNode(SkillNodes.MULTIPLIER, 10);
        setDamageType(DamageTypes.ATTACK.getId());
        addSkillType(SkillType.PHYSICAL);
    }

    @Override
    public void applyEffect(PlayerSkillContext info, IActiveCharacter character) {
        CriticalEffectModel model = getModel(info);
        CriticalEffect dodgeEffect = new CriticalEffect(character, -1, model);
        effectService.addEffect(dodgeEffect, this);
    }

    @Override
    public void skillUpgrade(IActiveCharacter character, int level, PlayerSkillContext context) {
        PlayerSkillContext info = character.getSkill(getId());
        IEffectContainer<CriticalEffectModel, CriticalEffect> effect = character.getEffect(CriticalEffect.name);
        effect.updateValue(getModel(info), this);
        effect.updateStackedValue();
    }

    private CriticalEffectModel getModel(PlayerSkillContext info) {
        int chance = info.getIntNodeValue(SkillNodes.CHANCE);
        double mult = info.getFloatNodeValue(SkillNodes.MULTIPLIER);
        return new CriticalEffectModel(chance, mult);
    }
}
