/*
 *     Copyright (c) 2015, NeumimTo https://github.com/NeumimTo
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package cz.neumimto.rpg.sponge.gui;

import cz.neumimto.rpg.api.ResourceLoader;
import cz.neumimto.rpg.api.Rpg;
import cz.neumimto.rpg.api.classes.ClassService;
import cz.neumimto.rpg.api.configuration.AttributeConfig;
import cz.neumimto.rpg.api.configuration.PluginConfig;
import cz.neumimto.rpg.api.effects.EffectService;
import cz.neumimto.rpg.api.effects.EffectStatusType;
import cz.neumimto.rpg.api.effects.IEffect;
import cz.neumimto.rpg.api.effects.IEffectContainer;
import cz.neumimto.rpg.api.entity.players.classes.ClassDefinition;
import cz.neumimto.rpg.api.entity.players.classes.PlayerClassData;
import cz.neumimto.rpg.api.gui.IPlayerMessage;
import cz.neumimto.rpg.api.inventory.CannotUseItemReason;
import cz.neumimto.rpg.api.localization.Arg;
import cz.neumimto.rpg.api.localization.LocalizationKeys;
import cz.neumimto.rpg.api.localization.LocalizationService;
import cz.neumimto.rpg.api.persistance.model.CharacterBase;
import cz.neumimto.rpg.api.persistance.model.CharacterClass;
import cz.neumimto.rpg.api.skills.SkillData;
import cz.neumimto.rpg.api.skills.tree.SkillTree;
import cz.neumimto.rpg.common.effects.InternalEffectSourceProvider;
import cz.neumimto.rpg.common.inventory.crafting.runewords.ItemUpgrade;
import cz.neumimto.rpg.common.inventory.crafting.runewords.Rune;
import cz.neumimto.rpg.common.inventory.runewords.RuneWord;
import cz.neumimto.rpg.common.persistance.dao.IPlayerDao;
import cz.neumimto.rpg.common.utils.StringUtils;
import cz.neumimto.rpg.common.utils.model.CharacterListModel;
import cz.neumimto.rpg.sponge.SpongeRpgPlugin;
import cz.neumimto.rpg.sponge.damage.SpongeDamageService;
import cz.neumimto.rpg.sponge.effects.common.def.BossBarExpNotifier;
import cz.neumimto.rpg.sponge.effects.common.def.ManaBarNotifier;
import cz.neumimto.rpg.sponge.entities.players.ISpongeCharacter;
import cz.neumimto.rpg.sponge.entities.players.SpongeCharacterService;
import cz.neumimto.rpg.sponge.inventory.data.InventoryCommandItemMenuData;
import cz.neumimto.rpg.sponge.inventory.data.MenuInventoryData;
import cz.neumimto.rpg.sponge.inventory.data.SkillTreeInventoryViewControllsData;
import cz.neumimto.rpg.sponge.inventory.data.manipulators.SkillTreeNode;
import cz.neumimto.rpg.sponge.inventory.runewords.RWService;
import cz.neumimto.rpg.sponge.skills.SpongeSkillService;
import cz.neumimto.rpg.sponge.utils.TextHelper;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.item.inventory.type.GridInventory;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.LiteralText;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.util.Color;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;

import static cz.neumimto.rpg.sponge.gui.GuiHelper.back;
import static cz.neumimto.rpg.sponge.gui.GuiHelper.getItemLore;

/**
 * Created by NeumimTo on 6.8.2015.
 */
@Singleton
@ResourceLoader.ListenerClass
public class VanillaMessaging implements IPlayerMessage<ISpongeCharacter> {

    public static Map<SkillTreeControllsButton, SkillTreeInterfaceModel> controlls;

    @Inject
    private Game game;

    @Inject
    private LocalizationService localizationService;

    @Inject
    private ClassService classService;

    @Inject
    private EffectService effectService;

    @Inject
    private SpongeRpgPlugin plugin;

    @Inject
    private RWService rwService;

    @Inject
    private SpongeDamageService spongeDamageService;

    @Inject
    private SpongeCharacterService characterService;

    @Inject
    private SpongeSkillService skillService;

    @Inject
    private IPlayerDao playerDao;


    public void load() {
        controlls = new HashMap<>();
        PluginConfig pluginConfig = Rpg.get().getPluginConfig();
        for (String a : pluginConfig.SKILLTREE_BUTTON_CONTROLLS) {
            String[] split = a.split(",");

            SkillTreeControllsButton key = SkillTreeControllsButton.valueOf(split[0].toUpperCase());
            ItemType type = Sponge.getRegistry().getType(ItemType.class, split[1]).orElse(ItemTypes.BARRIER);

            controlls.put(key, new SkillTreeInterfaceModel(Integer.parseInt(split[3]), type, split[2], (short) 0));
        }
    }

    @Override
    public boolean isClientSideGui() {
        return false;
    }

    @Override
    public void sendCooldownMessage(ISpongeCharacter player, String message, double cooldown) {
        player.sendMessage(localizationService.translate(LocalizationKeys.ON_COOLDOWN, Arg.arg("skill", message).with("time", cooldown)));
    }

    @Override
    public void sendEffectStatus(ISpongeCharacter player, EffectStatusType type, IEffect effect) {

    }


    private Text getDetailedCharInfo(ISpongeCharacter character) {
        Text text = Text.builder("Level").color(TextColors.YELLOW).append(
                Text.builder("Race").color(TextColors.RED).append(
                        Text.builder("Guild").color(TextColors.AQUA).append(
                                Text.builder("Class").color(TextColors.GOLD).build()
                        ).build()).build()).build();
        return text;
    }

    @Override
    public void sendPlayerInfo(ISpongeCharacter character, ISpongeCharacter target) {
        character.getPlayer().sendMessage(getDetailedCharInfo(target));
    }

    @Override
    public void showExpChange(ISpongeCharacter character, String classname, double expchange) {
        IEffectContainer<Object, BossBarExpNotifier> barExpNotifier = character.getEffect(BossBarExpNotifier.name);
        BossBarExpNotifier effect = (BossBarExpNotifier) barExpNotifier;
        if (effect == null) {
            effect = new BossBarExpNotifier(character);
            effectService.addEffect(effect, InternalEffectSourceProvider.INSTANCE);
        }
        effect.notifyExpChange(character, classname, expchange);
    }

    @Override
    public void showLevelChange(ISpongeCharacter character, PlayerClassData clazz, int level) {
        Player player = character.getPlayer();
        player.sendMessage(Text.of("Level up: " + clazz.getClassDefinition().getName() + " - " + level));
    }

    @Override
    public void sendStatus(ISpongeCharacter character) {
        CharacterBase base = character.getCharacterBase();

        PaginationService paginationService = Sponge.getServiceManager().provide(PaginationService.class).get();
        PaginationList.Builder builder = paginationService.builder();
        builder.title(Text.of(character.getName(), Color.YELLOW));
        builder.padding(Text.of("═", Color.GRAY));

        List<Text> content = new ArrayList<>();
        Set<CharacterClass> characterClasses = base.getCharacterClasses();
        for (CharacterClass cc : characterClasses) {
            Text t = Text.builder().append(Text.of(StringUtils.capitalizeFirst(cc.getName()), Color.GREEN))
                    .append(Text.of(" - ", TextColors.GRAY))
                    .append(Text.of(cc.getSkillPoints(), TextColors.BLUE))
                    .append(Text.of(String.format("(%s)", cc.getUsedSkillPoints()), TextColors.GRAY))

                    .toText();
            content.add(t);
        }
        content.add(Text.builder().append(Text.of("AttributeConfig points: ", TextColors.GREEN))
                .append(Text.of(character.getCharacterBase().getAttributePoints(), TextColors.AQUA))
                .append(Text.of(String.format("(%s)", character.getCharacterBase().getAttributePointsSpent(), TextColors.GRAY))).toText());

        builder.contents(content);
        builder.sendTo(character.getPlayer());
    }


    @Override
    public void showClassInfo(ISpongeCharacter character, ClassDefinition cc) {
        Player player = character.getPlayer();
        Inventory i = GuiHelper.CACHED_MENUS.get("class_template" + cc.getName());
        player.openInventory(i);
    }

    @Override
    public void sendListOfCharacters(final ISpongeCharacter player, CharacterBase currentlyCreated) {
        PaginationService paginationService = Sponge.getServiceManager().provide(PaginationService.class).get();
        PaginationList.Builder builder = paginationService.builder();
        Rpg.get().getAsyncExecutor().execute(() -> {

            List<CharacterBase> playersCharacters = characterService.getPlayersCharacters(player.getPlayer().getUniqueId());
            List<CharacterListModel> list = new ArrayList<>();
            for (CharacterBase playersCharacter : playersCharacters) {
                Set<CharacterClass> characterClasses = playersCharacter.getCharacterClasses();
                int charLevel = 0;
                for (CharacterClass characterClass : characterClasses) {
                    ClassDefinition classDefinitionByName = classService.getClassDefinitionByName(characterClass.getName());
                    if (classDefinitionByName == null) {
                        continue;
                    }
                    if (classDefinitionByName.getClassType().equalsIgnoreCase(Rpg.get().getPluginConfig().PRIMARY_CLASS_TYPE)) {
                        charLevel = characterClass.getLevel();
                        break;
                    }
                }
                String collect = playersCharacter.getCharacterClasses().stream().map(CharacterClass::getName).collect(Collectors.joining(", "));
                list.add(new CharacterListModel(
                        playersCharacter.getName(),
                        collect,
                        charLevel
                ));
            }


            List<Text> content = new ArrayList<>();
            builder.linesPerPage(10);
            builder.padding(Text.builder("=").color(TextColors.DARK_GRAY).build());

            String current = player.getName();

            list.forEach(a -> {
                Text.Builder b = Text.builder(" -")
                        .color(TextColors.GRAY);
                if (!a.getCharacterName().equalsIgnoreCase(current)) {
                    b.append(Text.builder(" [").color(TextColors.DARK_GRAY).build())
                            .append(Text.builder("SELECT").color(TextColors.GREEN)
                                    .onClick(TextActions.runCommand("/character switch " + a.getCharacterName())).build())
                            .append(Text.builder("] - ").color(TextColors.DARK_GRAY).build());
                } else {
                    b.append(Text.builder(" [").color(TextColors.DARK_GRAY).build())
                            .append(Text.builder("*").color(TextColors.RED).build())
                            .append(Text.builder("] - ").color(TextColors.DARK_GRAY).build());
                }
                b.append(Text.builder(a.getCharacterName()).color(TextColors.GRAY).append(Text.of(" ")).build());

                int level = a.getPrimaryClassLevel();
                int m = 0;

                b.append(Text.builder(a.getConcatClassNames()).color(TextColors.AQUA).append(Text.of(" ")).build());

                b.append(Text.builder("Level: ").color(TextColors.DARK_GRAY).append(
                        Text.builder(level + "").color(level == m ? TextColors.RED : TextColors.DARK_PURPLE).build()).build());

                content.add(b.build());
            });
            builder.title(Text.of("Characters", TextColors.WHITE))
                    .contents(content);
            builder.sendTo(player.getEntity());

        });
    }

    @Override
    public void sendListOfRunes(ISpongeCharacter character) {
        PaginationService paginationService = Sponge.getServiceManager().provide(PaginationService.class).get();
        PaginationList.Builder builder = paginationService.builder();

        List<Text> content = new ArrayList<>();
        List<Rune> r = new ArrayList<>(rwService.getRunes().values());
        for (Rune rune : r) {
            LiteralText.Builder b = Text.builder(rune.getName()).color(TextColors.GOLD);
            content.add(b.build());
        }
        builder.contents(content);
        builder.linesPerPage(10);
        builder.padding(Text.builder("=").color(TextColors.DARK_GRAY).build());
        builder.sendTo(character.getPlayer());


    }

    private ItemStack createItemRepresentingGroup(ClassDefinition p) {
        ItemStack s = GuiHelper.itemStack(p.getItemType());
        s.offer(new MenuInventoryData(true));
        s.offer(Keys.DISPLAY_NAME, Text.of(p.getName(), TextColors.DARK_PURPLE));
        if (p.getDescription() != null) {
            List<Text> list = new ArrayList<>();
            for (String s1 : p.getDescription()) {
                list.add(Text.builder(s1).color(TextColors.GOLD).style(TextStyles.ITALIC).build());
            }
            s.offer(Keys.ITEM_LORE, list);
        }
        return s;
    }

    @Override
    public void displayGroupArmor(ClassDefinition cc, ISpongeCharacter target) {
        String key = "class_allowed_items_armor_" + cc.getName();
        Inventory i = GuiHelper.CACHED_MENUS.get(key);
        Player player = target.getPlayer();
        player.openInventory(i);
    }

    @Override
    public void displayGroupWeapon(ClassDefinition cc, ISpongeCharacter target) {
        String key = "class_allowed_items_weapons_" + cc.getName();
        Inventory i = GuiHelper.CACHED_MENUS.get(key);
        Player player = target.getPlayer();
        player.openInventory(i);
    }


    @Override
    public void displayAttributes(ISpongeCharacter target, ClassDefinition cls) {

        Inventory.Builder builder = Inventory
                .builder();
        Text invName = translate(LocalizationKeys.ATTRIBUTES);
        Inventory i = builder.of(InventoryArchetypes.DOUBLE_CHEST)
                .property(InventoryTitle.of(invName))
                .build(plugin);

        i.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(0, 0))).offer(back(cls));

        int x = 1;
        int y = 1;
        for (Map.Entry<AttributeConfig, Integer> a : cls.getStartingAttributes().entrySet()) {
            AttributeConfig key = a.getKey();
            Integer value = a.getValue();
            i.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(x, y)))
                    .offer(createAttributeItem(key, value));
            //somehow format them in square-like structure
            if (x == 7) {
                x = 1;
                y++;
            } else {
                x++;
            }
        }
        target.getPlayer().openInventory(i);
    }

    public void displayRuneword(ISpongeCharacter character, RuneWord rw, boolean linkToRWList) {
        Inventory i = Inventory.builder().of(InventoryArchetypes.DOUBLE_CHEST).build(plugin);
        if (linkToRWList) {
            if (character.getPlayer().hasPermission("ntrpg.runewords.list")) {
                i.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(0, 0))).offer(back("runes", translate(LocalizationKeys.RUNE_LIST)));
            }
        }

        List<ItemStack> commands = new ArrayList<>();
        if (!rw.getAllowedItems().isEmpty()) {
            ItemStack is = GuiHelper.itemStack(ItemTypes.IRON_PICKAXE);
            is.offer(Keys.DISPLAY_NAME, translate(LocalizationKeys.RUNEWORD_ITEMS_MENU));
            is.offer(Keys.ITEM_LORE,
                    Collections.singletonList(
                            translate(LocalizationKeys.RUNEWORD_ITEMS_MENU_TOOLTIP, Arg.arg("runeword", rw.getName()))
                    )
            );
            is.offer(new InventoryCommandItemMenuData("runeword " + rw.getName() + " allowed-items"));
            commands.add(is);
        }

        if (!rw.getAllowedGroups().isEmpty()) {
            ItemStack is = GuiHelper.itemStack(ItemTypes.LEATHER_HELMET);
            is.offer(Keys.DISPLAY_NAME, translate(LocalizationKeys.RUNEWORD_ALLOWED_GROUPS_MENU));
            is.offer(Keys.ITEM_LORE,
                    Collections.singletonList(
                            translate(LocalizationKeys.RUNEWORD_ALLOWED_GROUPS_MENU_TOOLTIP, Arg.arg("runeword", rw.getName()))
                    )
            );
            is.offer(Keys.HIDE_ATTRIBUTES, true);
            is.offer(new InventoryCommandItemMenuData("runeword " + rw.getName() + " allowed-classes"));
            commands.add(is);
        }

        if (!rw.getAllowedGroups().isEmpty()) {
            ItemStack is = GuiHelper.itemStack(ItemTypes.REDSTONE);
            is.offer(Keys.DISPLAY_NAME, translate(LocalizationKeys.RUNEWORD_BLOCKED_GROUPS_MENU));
            is.offer(Keys.ITEM_LORE,
                    Collections.singletonList(
                            translate(LocalizationKeys.RUNEWORD_BLOCKED_GROUPS_MENU_TOOLTIP, Arg.arg("runeword", rw.getName()))
                    )
            );
            is.offer(new InventoryCommandItemMenuData("runeword " + rw.getName() + " blocked-classes"));
            commands.add(is);
        }

        for (int q = 0; q < commands.size(); q++) {
            i.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(q + 2, 2))).offer(commands.get(q));
        }

        if (character.getPlayer().hasPermission("ntrpg.runewords.combination.list")) {
            int x = 1;
            int y = 4;
            if (rw.getRunes().size() <= 7) {
                for (ItemUpgrade rune : rw.getRunes()) {
					/*ItemStack is = rwService.createRune(rune);
					is.offer(new MenuInventoryData(true));
					i.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(x, y))).offer(is);
					x++;
					*/
                }
            } else {
                ItemStack is = ItemStack.of(rwService.getAllowedRuneItemTypes().get(0), rw.getRunes().size());
                is.offer(new MenuInventoryData(true));
                StringBuilder s = null;
                for (ItemUpgrade rune : rw.getRunes()) {
                    s.append(rune.getName());
                }
                is.offer(Keys.DISPLAY_NAME, Text.of(s.toString(), TextColors.GOLD));
                i.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(x, y))).offer(is);
            }
        }

        character.getPlayer().openInventory(i);
    }

    public void displayRunewordBlockedGroups(ISpongeCharacter character, RuneWord rw) {
        character.getPlayer().openInventory(displayGroupRequirements(character, rw, rw.getAllowedGroups()));
    }

    public void displayRunewordRequiredGroups(ISpongeCharacter character, RuneWord rw) {

    }

    public void displayRunewordAllowedGroups(ISpongeCharacter character, RuneWord rw) {

    }

    public void displayRunewordAllowedItems(ISpongeCharacter character, RuneWord rw) {
        Inventory i = Inventory.builder().of(InventoryArchetypes.DOUBLE_CHEST).build(plugin);
        i.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(0, 0)))
                .offer(back("runeword " + rw.getName(), translate(LocalizationKeys.RUNEWORD_DETAILS_MENU)));
        int x = 1;
        int y = 2;
        for (String type : rw.getAllowedItems()) {
            ItemType itemType = Sponge.getRegistry().getType(ItemType.class, type).orElse(ItemTypes.STONE);
            i.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(x, y))).offer(GuiHelper.itemStack(itemType));
            if (x == 7) {
                x = 1;
                y++;
            } else {
                x++;
            }
        }

    }

    private Inventory displayGroupRequirements(ISpongeCharacter character, RuneWord rw, Set<ClassDefinition> groups) {
        Inventory i = Inventory.builder().of(InventoryArchetypes.DOUBLE_CHEST).build(plugin);
        i.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(0, 0)))
                .offer(back("runeword " + rw.getName(), translate(LocalizationKeys.RUNEWORD_DETAILS_MENU)));

        List<ItemStack> list = new ArrayList<>();
        for (ClassDefinition classDefinition : groups) {
            list.add(runewordRequirementsToItemStack(character, classDefinition));
        }
        int x = 1;
        int y = 2;
        for (ItemStack itemStack : list) {
            i.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(x, y))).offer(itemStack);
            if (x == 7) {
                x = 1;
                y++;
            } else {
                x++;
            }
        }
        return i;
    }

    private ItemStack runewordRequirementsToItemStack(ISpongeCharacter character, ClassDefinition classDefinition) {
        ItemStack is = createItemRepresentingGroup(classDefinition);
        TextColor color = hasGroup(character, classDefinition);
        is.offer(Keys.DISPLAY_NAME, Text.of(color, classDefinition.getName()));
        return is;
    }

    private TextColor hasGroup(ISpongeCharacter character, ClassDefinition classDefinition) {
        return character.hasClass(classDefinition) ? TextColors.GREEN : TextColors.RED;
    }

    private ItemStack createAttributeItem(AttributeConfig key, Integer value) {
        ItemStack of = GuiHelper.itemStack(key.getItemType());
        of.offer(Keys.DISPLAY_NAME, Text.of(TextColors.DARK_RED, key.getName()));

        of.offer(new MenuInventoryData(true));
        List<Text> lore = new ArrayList<>();
        lore.add(translate(LocalizationKeys.INITIAL_VALUE, Arg.arg("value", value)));
        if (key.getDescription() != null) {
            lore.addAll(getItemLore(key.getDescription()));
        }
        of.offer(Keys.ITEM_LORE, lore);
        return of;
    }


    @Override
    public void displayMana(ISpongeCharacter character) {
        IEffectContainer<Object, ManaBarNotifier> barExpNotifier = character.getEffect(ManaBarNotifier.name);
        ManaBarNotifier effect = (ManaBarNotifier) barExpNotifier;
        if (effect == null) {
            effect = new ManaBarNotifier(character);
            effectService.addEffect(effect, InternalEffectSourceProvider.INSTANCE);
        }
        effect.notifyManaChange();

    }

    @Override
    public void sendCannotUseItemNotification(ISpongeCharacter character, String item, CannotUseItemReason reason) {
        if (reason == CannotUseItemReason.CONFIG) {
            character.getPlayer()
                    .sendMessage(ChatTypes.ACTION_BAR, Text.of(TextColors.RED, translate(LocalizationKeys.CANNOT_USE_ITEM_CONFIGURATION_REASON)));
        } else if (reason == CannotUseItemReason.LEVEL) {
            character.getPlayer().sendMessage(ChatTypes.ACTION_BAR, Text.of(TextColors.RED, translate(LocalizationKeys.CANNOT_USE_ITEM_LEVEL_REASON)));
        } else if (reason == CannotUseItemReason.LORE) {
            character.getPlayer().sendMessage(ChatTypes.ACTION_BAR, Text.of(TextColors.RED, translate(LocalizationKeys.CANNOT_USE_ITEM_LORE_REASON)));
        }
    }


    @Override
    public void openSkillTreeMenu(ISpongeCharacter player) {
        SkillTree skillTree = player.getLastTimeInvokedSkillTreeView().getSkillTree();
        if (player.getSkillTreeViewLocation().get(skillTree.getId()) == null) {
            SpongeSkillTreeViewModel skillTreeViewModel = new SpongeSkillTreeViewModel();
            for (SpongeSkillTreeViewModel treeViewModel : player.getSkillTreeViewLocation().values()) {
                treeViewModel.setCurrent(false);
            }
            player.getSkillTreeViewLocation().put(skillTree.getId(), skillTreeViewModel);
            skillTreeViewModel.setSkillTree(skillTree);
        }
        Inventory skillTreeInventoryViewTemplate = GuiHelper.createSkillTreeInventoryViewTemplate(player, skillTree);
        createSkillTreeView(player, skillTreeInventoryViewTemplate);
        player.getPlayer().openInventory(skillTreeInventoryViewTemplate);

    }


    @Override
    public void moveSkillTreeMenu(ISpongeCharacter character) {
        Optional<Container> openInventory = character.getPlayer().getOpenInventory();
        if (openInventory.isPresent()) {
            createSkillTreeView(character, openInventory.get().query(GridInventory.class).first());
        }
    }

    @Override
    public void displaySkillDetailsInventoryMenu(ISpongeCharacter character, SkillTree tree, String command) {
        Inventory skillDetailInventoryView = GuiHelper.createSkillDetailInventoryView(character, tree, tree.getSkillById(command));
        character.getPlayer().openInventory(skillDetailInventoryView);
    }

    @Override
    public void displayInitialProperties(ClassDefinition byName, ISpongeCharacter player) {
        ItemStack back = GuiHelper.back(byName);

    }

    private void createSkillTreeView(ISpongeCharacter character, Inventory skillTreeInventoryViewTemplate) {

        SpongeSkillTreeViewModel skillTreeViewModel = character.getLastTimeInvokedSkillTreeView();
        SkillTree skillTree = skillTreeViewModel.getSkillTree();
        short[][] skillTreeMap = skillTreeViewModel.getSkillTree().getSkillTreeMap();
        int y = skillTree.getCenter().value + skillTreeViewModel.getLocation().value; //y
        int x = skillTree.getCenter().key + skillTreeViewModel.getLocation().key; //x

        if (skillTreeMap == null) {
            throw new IllegalStateException("No AsciiMap defined for skilltree: " + skillTree.getId());
        }

        int columns = skillTreeMap[0].length;
        int rows = skillTreeMap.length;

        SpongeSkillTreeViewModel.InteractiveMode interactiveMode = skillTreeViewModel.getInteractiveMode();
        ItemStack md = GuiHelper.interactiveModeToitemStack(character, interactiveMode);
        skillTreeInventoryViewTemplate.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(8, 1))).clear();
        skillTreeInventoryViewTemplate.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(8, 1))).offer(md);


        for (int k = -3; k <= 3; k++) { //x
            for (int l = -3; l <= 3; l++) { //y
                Inventory query = skillTreeInventoryViewTemplate
                        .query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(l + 3, k + 3)));
                query.clear();
                if (x + k >= 0 && x + k < rows) {
                    if (l + y >= 0 && l + y < columns) {

                        short id = skillTreeMap[x + k][l + y];
                        ItemStack itemStack = null;
                        if (id > 0) {
                            SkillTreeInterfaceModel guiModelById = skillService.getGuiModelById(id);
                            if (guiModelById != null) {
                                itemStack = guiModelById.toItemStack();
                            } else {
                                SkillData skillById = skillTree.getSkillById(id);

                                if (skillById == null) {
                                    itemStack = GuiHelper.itemStack(ItemTypes.BARRIER);
                                    itemStack.offer(Keys.DISPLAY_NAME, Text.of("UNKNOWN SKILL ID: " + id));
                                    itemStack.offer(new MenuInventoryData(true));
                                } else {
                                    itemStack = GuiHelper.skillToItemStack(character, skillById, skillTree, skillTreeViewModel);
                                    itemStack.offer(new SkillTreeInventoryViewControllsData(SkillTreeControllsButton.NODE));
                                    itemStack.offer(new MenuInventoryData(true));
                                    itemStack.offer(new SkillTreeNode(skillById.getSkill().getId()));
                                }
                            }
                        }
                        if (itemStack == null) {
                            itemStack = ItemStack.of(ItemTypes.STAINED_GLASS_PANE, 1);
                            itemStack.offer(Keys.DISPLAY_NAME, Text.EMPTY);
                            itemStack.offer(Keys.DYE_COLOR, DyeColors.GRAY);
                            itemStack.offer(Keys.HIDE_MISCELLANEOUS, true);
                            itemStack.offer(new MenuInventoryData(true));
                        }
                        query.offer(itemStack);
                    } else {
                        //	SlotPos slotPos = new SlotPos(l + 3, k + 3);
                        query.offer(GuiHelper.createSkillTreeInventoryMenuBoundary());
                    }
                } else {
                    query.offer(GuiHelper.createSkillTreeInventoryMenuBoundary());
                }
            }
        }
    }

    @Override
    public void sendClassesByType(ISpongeCharacter character, String type) {
        Inventory i = GuiHelper.createMenuInventoryClassesByTypeView(type);

        character.getPlayer().openInventory(i);
    }

    @Override
    public void sendClassTypes(ISpongeCharacter character) {
        Player player = character.getPlayer();
        Inventory inventory = GuiHelper.createMenuInventoryClassTypesView();
        player.openInventory(inventory);
    }

    @Override
    public void displayCharacterMenu(ISpongeCharacter cc) {
        Player player = cc.getPlayer();
        Inventory inventory = GuiHelper.createCharacterMenu(cc);
        player.openInventory(inventory);
    }

    @Override
    public void displayCharacterAttributes(ISpongeCharacter character) {
        character.setAttributesTransaction(new HashMap<>());
    }

    @Override
    public void displayCurrentClicks(ISpongeCharacter character, String combo) {
        String split = combo.replaceAll(".", "$0 ");
        LiteralText build = Text.builder(split).color(TextColors.GREEN).style(TextStyles.UNDERLINE)
                .append(Text.builder("_").color(TextColors.GRAY).build()).build();

        character.getPlayer().sendMessage(ChatTypes.ACTION_BAR, build);
    }

    @Override
    public void displayCharacterArmor(ISpongeCharacter character, int page) {
        Inventory i = GuiHelper.getCharacterAllowedArmor(character, page);
        character.getPlayer().openInventory(i);
    }

    @Override
    public void displayCharacterWeapons(ISpongeCharacter character, int page) {
        Inventory i = GuiHelper.getCharacterAllowedWeapons(character, page);
        character.getPlayer().openInventory(i);
    }

    @Override
    public void displaySpellbook(ISpongeCharacter character) {
        throw new RuntimeException("Not implemented");
    }

    private Text translate(String key) {
        return TextHelper.parse(localizationService.translate(key));
    }

    private Text translate(String key, Arg arg) {
        return TextHelper.parse(localizationService.translate(key, arg));
    }
}

