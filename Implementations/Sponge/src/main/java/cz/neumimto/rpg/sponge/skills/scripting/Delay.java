package cz.neumimto.rpg.sponge.skills.scripting;

import cz.neumimto.rpg.api.skills.scripting.JsBinding;
import cz.neumimto.rpg.common.skills.scripting.SkillComponent;
import cz.neumimto.rpg.sponge.SpongeRpgPlugin;
import org.spongepowered.api.Sponge;

import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

@JsBinding(JsBinding.Type.OBJECT)
@SkillComponent(
        value = "Puts a task into a scheduled execution",
        params = {
                @SkillComponent.Param("function - code to run later"),
                @SkillComponent.Param("delay - time in milliseconds")
        },
        usage = "delay(function() { ... }, delay)"
)
public class Delay implements BiConsumer<Runnable, Long> {
    @Override
    public void accept(Runnable r, Long l) {
        Sponge.getScheduler().createTaskBuilder().execute(r).delay(l, TimeUnit.MILLISECONDS).submit(SpongeRpgPlugin.getInstance());
    }
}
