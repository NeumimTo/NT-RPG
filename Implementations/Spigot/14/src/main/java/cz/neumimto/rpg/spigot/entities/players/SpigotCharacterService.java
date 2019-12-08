package cz.neumimto.rpg.spigot.entities.players;

import cz.neumimto.rpg.api.entity.players.IActiveCharacter;
import cz.neumimto.rpg.api.persistance.model.CharacterBase;
import cz.neumimto.rpg.api.persistance.model.CharacterSkill;
import cz.neumimto.rpg.common.entity.PropertyServiceImpl;
import cz.neumimto.rpg.common.entity.players.AbstractCharacterService;
import cz.neumimto.rpg.common.entity.players.CharacterMana;
import cz.neumimto.rpg.spigot.SpigotRpgPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;
import javax.inject.Singleton;

@Singleton
public class SpigotCharacterService extends AbstractCharacterService<ISpigotCharacter> {

    @Override
    protected ISpigotCharacter createCharacter(UUID player, CharacterBase characterBase) {
        SpigotCharacter iActiveCharacter = new SpigotCharacter(player, characterBase, PropertyServiceImpl.LAST_ID);
        iActiveCharacter.setMana(new CharacterMana(iActiveCharacter));
        iActiveCharacter.setHealth(new SpigotCharacterHealth(iActiveCharacter));
        return iActiveCharacter;
    }


    @Override
    public ISpigotCharacter buildDummyChar(UUID uuid) {
        return null;
    }

    @Override
    public void registerDummyChar(ISpigotCharacter dummy) {

    }

    @Override
    public boolean assignPlayerToCharacter(UUID uniqueId) {
        return false;
    }

    @Override
    public int canCreateNewCharacter(UUID uniqueId, String name) {
        return 0;
    }

    @Override
    public void removePersistantSkill(CharacterSkill characterSkill) {

    }

    @Override
    protected void scheduleNextTick(Runnable r) {
        Bukkit.getScheduler().runTaskLater(SpigotRpgPlugin.getInstance(), r, 1L);
    }

    public IActiveCharacter getCharacter(Player target) {
        return getCharacter(target.getUniqueId());
    }
}