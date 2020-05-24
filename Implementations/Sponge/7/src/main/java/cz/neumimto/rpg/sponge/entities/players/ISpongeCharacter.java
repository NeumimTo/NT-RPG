package cz.neumimto.rpg.sponge.entities.players;

import cz.neumimto.rpg.api.entity.IEntity;
import cz.neumimto.rpg.api.entity.players.IActiveCharacter;
import cz.neumimto.rpg.sponge.entities.ISpongeEntity;
import cz.neumimto.rpg.sponge.entities.players.party.SpongeParty;
import cz.neumimto.rpg.sponge.gui.SpongeSkillTreeViewModel;
import cz.neumimto.rpg.sponge.utils.TextHelper;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.Map;

public interface ISpongeCharacter extends IActiveCharacter<Player, SpongeParty>, ISpongeEntity<Player>, IEntity<Player> {

    @Override
    default void sendMessage(String message) {
        getPlayer().sendMessage(TextHelper.parse(message));
    }

    default Player getPlayer() {
        return Sponge.getServer().getPlayer(getUUID()).orElse(null);
    }

    default Player getEntity() {
        return getPlayer();
    }


    Map<String, SpongeSkillTreeViewModel> getSkillTreeViewLocation();

    SpongeSkillTreeViewModel getLastTimeInvokedSkillTreeView();

    boolean isSpellRotationActive();

    void setSpellbook(ItemStack[][] itemStacks);

    ItemStack[][] getSpellbook();

    void setSpellRotation(boolean active);


}
