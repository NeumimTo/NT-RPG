package cz.neumimto.rpg.sponge.entities.players;

import cz.neumimto.rpg.api.skills.PlayerSkillContext;
import cz.neumimto.rpg.common.entity.players.PlayerNotInGameException;
import cz.neumimto.rpg.common.entity.players.PreloadCharacter;
import cz.neumimto.rpg.sponge.entities.players.party.SpongeParty;
import cz.neumimto.rpg.sponge.gui.SpongeSkillTreeViewModel;
import cz.neumimto.rpg.sponge.utils.TextHelper;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.chat.ChatTypes;

import java.util.*;

public class SpongePreloadCharacter extends PreloadCharacter<Player, SpongeParty> implements ISpongeCharacter {

    public SpongePreloadCharacter(UUID uuid) {
        super(uuid);
    }

    @Override
    public Player getPlayer() {
        Optional<Player> player = Sponge.getServer().getPlayer(getUUID());
        if (player.isPresent()) {
            return player.get();
        } else {
            throw new PlayerNotInGameException(String.format(
                    "Player object with uuid=%s has not been constructed yet. Calling PreloadCharacter.getCharacter in a wrong state", getUUID()), this);
        }
    }

    @Override
    public Player getEntity() {
        return getPlayer();
    }

    @Override
    public Map<String, SpongeSkillTreeViewModel> getSkillTreeViewLocation() {
        return Collections.emptyMap();
    }

    @Override
    public SpongeSkillTreeViewModel getLastTimeInvokedSkillTreeView() {
        return null;
    }

    @Override
    public boolean isSpellRotationActive() {
        return false;
    }

    @Override
    public void setSpellbook(ItemStack[][] itemStacks) {

    }

    @Override
    public ItemStack[][] getSpellbook() {
        return new ItemStack[0][];
    }

    @Override
    public void setSpellRotation(boolean active) {

    }

    @Override
    public Map<String, Integer> getAttributesTransaction() {
        return Collections.emptyMap();
    }

    @Override
    public void setAttributesTransaction(HashMap<String, Integer> map) {

    }

    @Override
    public UUID getUUID() {
        return uuid;
    }

    @Override
    public Map<String, PlayerSkillContext> getSkillsByName() {
        return Collections.emptyMap();
    }


    @Override
    public void sendNotification(String message) {
        getPlayer().sendMessage(ChatTypes.ACTION_BAR, TextHelper.parse(message));
    }

    @Override
    public String getPlayerAccountName() {
        return getPlayer().getName();
    }
}
