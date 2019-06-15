package cz.neumimto.skills.active;

import com.flowpowered.math.vector.Vector3d;
import cz.neumimto.SkillLocalization;
import cz.neumimto.Utils;
import cz.neumimto.rpg.ResourceLoader;
import cz.neumimto.rpg.api.Rpg;
import cz.neumimto.rpg.api.skills.PlayerSkillContext;
import cz.neumimto.rpg.api.skills.SkillResult;
import cz.neumimto.rpg.api.skills.mods.SkillContext;
import cz.neumimto.rpg.api.skills.tree.SkillType;
import cz.neumimto.rpg.api.skills.types.ActiveSkill;
import cz.neumimto.rpg.sponge.entities.players.ISpongeCharacter;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.weather.Weathers;

import javax.inject.Singleton;

/**
 * Created by NeumimTo on 3.8.2017.
 */
@Singleton
@ResourceLoader.Skill("ntrpg:astronomy")
public class Astronomy extends ActiveSkill<ISpongeCharacter> {

	private static final long start = 14000;
	private static final long end = 22000; //todo check for night, not possible currently

	@Override
	public void init() {
		super.init();
		setIcon(ItemTypes.CLOCK.getId());
		addSkillType(SkillType.CAN_CAST_WHILE_SILENCED);
		addSkillType(SkillType.UTILITY);
	}

	@Override
	public void cast(ISpongeCharacter character, PlayerSkillContext info, SkillContext skillContext) {
		Player character1 = character.getEntity();

		if (character1.getWorld().getWeather() == Weathers.CLEAR) {
			Vector3d position = character1.getLocation().getPosition();
			if (character1.getWorld().getHighestYAt(position.getFloorX(), position.getFloorZ()) > position.getFloorY()) {
				String translate = Rpg.get().getLocalizationService().translate(SkillLocalization.ASTRONOMY_CANNOT_SEE_THE_SKY);
				character.sendNotification(translate);
				skillContext.next(character, info, skillContext.result(SkillResult.CANCELLED));
				return;
			}
			ItemStack is = Utils.createTeleportationScroll(character.getLocation());
			character.getEntity().getInventory().offer(is);
			skillContext.next(character, info, skillContext.result(SkillResult.OK));
			return;
		}
		String translate = Rpg.get().getLocalizationService().translate(SkillLocalization.ASTRONOMY_CANNOT_SEE_THE_SKY);
		character.sendNotification(translate);
		skillContext.next(character, info, skillContext.result(SkillResult.CANCELLED));
		return;
	}
}
