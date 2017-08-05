package cz.neumimto.skills.active;

import com.flowpowered.math.vector.Vector3d;
import cz.neumimto.rpg.ResourceLoader;
import cz.neumimto.SkillLocalization;
import cz.neumimto.rpg.players.IActiveCharacter;
import cz.neumimto.rpg.skills.*;
import cz.neumimto.rpg.utils.Utils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.blockray.BlockRay;
import org.spongepowered.api.util.blockray.BlockRayHit;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.TeleportHelper;
import org.spongepowered.api.world.World;

import java.util.Optional;

/**
 * Created by NeumimTo on 29.12.2015.
 */
@ResourceLoader.Skill
public class SkillTeleport extends ActiveSkill {

    public SkillTeleport() {
        setName("Teleport");
        SkillSettings settings = new SkillSettings();
        settings.addNode(SkillNodes.RANGE, 20, 20);
        super.settings = settings;
        super.setDescription(SkillLocalization.SKILL_TELEPORT_DESC);
        addSkillType(SkillType.TELEPORT);
    }

    @Override
    public SkillResult cast(IActiveCharacter character, ExtendedSkillInfo extendedSkillInfo, SkillModifier skillModifier) {
        Player player = character.getPlayer();
        Optional<BlockRayHit<World>> optHit = BlockRay.from(player).stopFilter(Utils.SKILL_TARGET_BLOCK_FILTER).build().end();
        if (optHit.isPresent()) {
            Vector3d lookPos = optHit.get().getBlockPosition().toDouble();
            Location<World> worldLocation = new Location<>(player.getWorld(), lookPos);
            TeleportHelper helper = Sponge.getGame().getTeleportHelper();
            Optional<Location<World>> safeLocation = helper.getSafeLocation(worldLocation);
            if (safeLocation.isPresent()) {
                player.setLocation(safeLocation.get());
            }
        }
        return SkillResult.OK;
    }
}
