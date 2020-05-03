package cz.neumimto.effects.negative;

import cz.neumimto.rpg.api.Rpg;
import cz.neumimto.rpg.api.effects.EffectBase;
import cz.neumimto.rpg.api.effects.IEffect;
import cz.neumimto.rpg.api.entity.IEntity;
import cz.neumimto.rpg.api.skills.scripting.JsBinding;
import cz.neumimto.rpg.sponge.damage.SkillDamageSource;
import cz.neumimto.rpg.sponge.entities.ISpongeEntity;
import cz.neumimto.rpg.sponge.entities.players.ISpongeCharacter;
import org.spongepowered.api.entity.living.Living;

/**
 * Created by NeumimTo on 6.8.2017.
 */
@JsBinding(JsBinding.Type.CLASS)
public class PandemicEffect extends EffectBase {

    public static final String name = "Pandemic";

    private ISpongeCharacter caster;
    private float damage;
    private SkillDamageSource damageSource;

    public PandemicEffect(ISpongeCharacter caster, IEntity iEntity, float damage, long duration, long period) {
        super(name, iEntity);
        this.caster = caster;
        this.damage = damage;
        setDuration(duration);
        setPeriod(period);

    }

    @Override
    public void onTick(IEffect self) {
        Living entity = ((ISpongeEntity) getConsumer()).getEntity();
        if (entity.damage(damage, damageSource)) {
            ;
            //todo some particles
            Rpg.get().getEntityService().healEntity(caster, damage, damageSource.getSkill());
        }
    }

    public SkillDamageSource getDamageSource() {
        return damageSource;
    }

    public void setDamageSource(SkillDamageSource damageSource) {
        this.damageSource = damageSource;
    }
}
