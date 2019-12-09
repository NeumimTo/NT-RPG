package cz.neumimto.rpg.sponge.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Flags;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.sponge.contexts.OnlinePlayer;
import cz.neumimto.rpg.api.entity.players.IActiveCharacter;
import cz.neumimto.rpg.api.entity.players.parties.PartyService;
import cz.neumimto.rpg.common.commands.PartyCommandFacade;
import cz.neumimto.rpg.sponge.entities.players.ISpongeCharacter;
import cz.neumimto.rpg.sponge.entities.players.SpongeCharacterService;
import org.spongepowered.api.entity.living.player.Player;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@CommandAlias("party|nparty")
public class SpongePartyCommands extends BaseCommand {

    @Inject
    private SpongeCharacterService characterService;

    @Inject
    private PartyCommandFacade partyCommandFacade;

    @Inject
    private PartyService partyService;

    @Subcommand("accept")
    public void acceptPartyInviteCommand(Player executor) {
        ISpongeCharacter character = characterService.getCharacter(executor);
        partyCommandFacade.acceptPartyInvite(character);
    }

    @Subcommand("invite")
    public void inviteToPartyCommand(Player executor, @Flags("target") OnlinePlayer target) {
        IActiveCharacter character = characterService.getCharacter(executor);
        partyService.sendPartyInvite(character.getParty(), characterService.getCharacter(target.player));
    }

    @Subcommand("create")
    public void createPartyCommand(Player executor) {
        IActiveCharacter character = characterService.getCharacter(executor);
        partyCommandFacade.createParty(character);
    }

    @Subcommand("kick")
    public void kickFromPartyCommand(Player executor, @Flags("target") OnlinePlayer target) {
        IActiveCharacter character = characterService.getCharacter(executor);
        IActiveCharacter character2 = characterService.getCharacter(target.player);
        partyService.kickCharacterFromParty(character.getParty(), character2);
    }
}
