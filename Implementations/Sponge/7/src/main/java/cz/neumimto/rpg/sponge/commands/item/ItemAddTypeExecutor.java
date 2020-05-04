package cz.neumimto.rpg.sponge.commands.item;

import cz.neumimto.rpg.api.Rpg;
import cz.neumimto.rpg.api.items.ItemMetaType;
import cz.neumimto.rpg.api.localization.LocalizationKeys;
import cz.neumimto.rpg.sponge.inventory.SpongeInventoryService;
import cz.neumimto.rpg.sponge.utils.TextHelper;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.Optional;

public class ItemAddTypeExecutor implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Player player = (Player) src;
        Optional<ItemMetaType> type = args.getOne("type");
        if (type.isPresent()) {
            Optional<ItemStack> itemInHand = player.getItemInHand(HandTypes.MAIN_HAND);
            if (itemInHand.isPresent()) {
                ItemStack itemStack = itemInHand.get();
                SpongeInventoryService inventoryService = (SpongeInventoryService) Rpg.get().getInventoryService();
                inventoryService.createItemMetaSectionIfMissing(itemStack);
                inventoryService.setItemMetaType(itemStack, type.get());
                inventoryService.updateLore(itemStack);
                player.setItemInHand(HandTypes.MAIN_HAND, itemStack);
                return CommandResult.builder().affectedItems(1).build();
            }
            String translate = Rpg.get().getLocalizationService().translate(LocalizationKeys.NO_ITEM_IN_HAND);
            player.sendMessage(TextHelper.parse(translate));
            return CommandResult.empty();
        }
        return CommandResult.empty();
    }
}
