package cz.neumimto.rpg.sponge.commands;

import static cz.neumimto.rpg.api.logging.Log.info;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.*;
import co.aikar.commands.sponge.contexts.OnlinePlayer;
import com.google.inject.Injector;
import cz.neumimto.rpg.api.Rpg;
import cz.neumimto.rpg.api.damage.DamageService;
import cz.neumimto.rpg.api.effects.EffectService;
import cz.neumimto.rpg.api.effects.IGlobalEffect;
import cz.neumimto.rpg.api.entity.EntityService;
import cz.neumimto.rpg.api.entity.players.IActiveCharacter;
import cz.neumimto.rpg.api.entity.players.classes.ClassDefinition;
import cz.neumimto.rpg.api.entity.players.classes.PlayerClassData;
import cz.neumimto.rpg.api.items.ClassItem;
import cz.neumimto.rpg.api.items.ItemClass;
import cz.neumimto.rpg.api.items.RpgItemType;
import cz.neumimto.rpg.api.logging.Log;
import cz.neumimto.rpg.api.persistance.model.CharacterBase;
import cz.neumimto.rpg.api.scripting.IScriptEngine;
import cz.neumimto.rpg.api.skills.*;
import cz.neumimto.rpg.api.skills.mods.SkillContext;
import cz.neumimto.rpg.api.skills.mods.SkillExecutorCallback;
import cz.neumimto.rpg.api.skills.types.IActiveSkill;
import cz.neumimto.rpg.api.utils.ActionResult;
import cz.neumimto.rpg.common.commands.AdminCommandFacade;
import cz.neumimto.rpg.common.commands.CommandProcessingException;
import cz.neumimto.rpg.common.entity.PropertyServiceImpl;
import cz.neumimto.rpg.common.persistance.dao.ClassDefinitionDao;
import cz.neumimto.rpg.sponge.entities.players.ISpongeCharacter;
import cz.neumimto.rpg.sponge.entities.players.SpongeCharacterService;
import cz.neumimto.rpg.sponge.inventory.SpongeItemService;
import cz.neumimto.rpg.sponge.utils.TextHelper;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@CommandAlias("nadmin|na")
public class SpongeAdminCommands extends BaseCommand {

    @Inject
    private AdminCommandFacade adminCommandFacade;

    @Inject
    private SpongeCharacterService characterService;

    @Inject
    private Injector injector;

    @Inject
    private EffectService effectService;

    @Inject
    private PropertyServiceImpl propertyService;

    @Inject
    private EntityService entityService;

    @Inject
    private SpongeItemService itemService;

    @Inject
    private DamageService damageService;

    @Subcommand("effect add")
    @Description("Adds effect, managed by rpg plugin, to the player")
    public void effectAddCommand(Player executor, OnlinePlayer target, IGlobalEffect effect, long duration, @Default("{}") String[] args) {
        String data = String.join("", args);

        IActiveCharacter character = characterService.getCharacter(target.player);

        try {
            adminCommandFacade.commandAddEffectToPlayer(data, effect, duration, character);
        } catch (CommandProcessingException e) {
            executor.sendMessage(Text.of(e.getMessage()));
        }
    }


    @Subcommand("experiences add")
    @Description("Adds N experiences of given source type to a character")
    public void addExperiencesCommand(Player executor, OnlinePlayer target, double amount, @Optional ClassDefinition classDefinition, @Optional String source) {
        ISpongeCharacter character = characterService.getCharacter(target.player);
        try {
            adminCommandFacade.commandAddExperiences(character, amount, classDefinition, source);
        } catch (CommandProcessingException e) {
            executor.sendMessage(Text.of(e.getMessage()));
        }
    }

    @Subcommand("skill")
    public void adminExecuteSkillCommand(Player executor, ISkill skill, @Flags("level") @Default("1") int level) {
        IActiveCharacter character = characterService.getCharacter(executor);
        if (character.isStub()) {
            throw new RuntimeException("Character is required even for an admin.");
        }
        SkillSettings defaultSkillSettings = skill.getSettings();

        Long l = System.nanoTime();

        PlayerSkillContext playerSkillContext = new PlayerSkillContext(null, skill, character);
        playerSkillContext.setLevel(level);
        SkillData skillData = new SkillData(skill.getId());
        skillData.setSkillSettings(defaultSkillSettings);
        playerSkillContext.setSkillData(skillData);
        playerSkillContext.setSkill(skill);

        SkillContext skillContext = new SkillContext((IActiveSkill) skill, playerSkillContext) {{
            wrappers.add(new SkillExecutorCallback() {
                @Override
                public void doNext(IActiveCharacter character, PlayerSkillContext info, SkillContext skillResult) {
                    Long e = System.nanoTime();
                    character.sendMessage("Exec Time: " + TimeUnit.MILLISECONDS.convert(e - l, TimeUnit.NANOSECONDS));
                }
            });
        }};
        skillContext.sort();
        skillContext.next(character, playerSkillContext, skillContext);
    }

    @Subcommand("reload")
    public void reload() {
        info("[RELOAD] Reading Settings.conf file: ");
        Rpg.get().reloadMainPluginConfig();
        info("[RELOAD] Reading Entity conf files: ");
        Rpg.get().getEntityService().reload();

        info("[RELOAD] Scripts ");
        IScriptEngine jsLoader = injector.getInstance(IScriptEngine.class);
        jsLoader.initEngine();
        jsLoader.reloadSkills();

        ClassDefinitionDao build = injector.getInstance(ClassDefinitionDao.class);
        try {
            info("[RELOAD] Checking class files: ");

            Set<ClassDefinition> classDefinitions = build.parseClassFiles();
            info("[RELOAD] Class files ok");

            info("[RELOAD] Saving current state of players");
            Set<CharacterBase> characterBases = new HashSet<>();
            for (Player player : Sponge.getServer().getOnlinePlayers()) {
                IActiveCharacter character = characterService.getCharacter(player);
                if (character.isStub()) {
                    continue;
                }
                characterBases.add(character.getCharacterBase());
            }

            Log.info("[RELOAD] Purging effect caches");
            effectService.purgeEffectCache();
            effectService.stopEffectScheduler();

            for (Player player : Sponge.getServer().getOnlinePlayers()) {
                IActiveCharacter character = characterService.getCharacter(player);
                if (character.isStub()) {
                    continue;
                }
                ISpongeCharacter preloadCharacter = characterService.buildDummyChar(player.getUniqueId());
                characterService.registerDummyChar(preloadCharacter);
            }

            for (CharacterBase characterBase : characterBases) {
                Log.info("[RELOAD] saving character " + characterBase.getLastKnownPlayerName());
                characterService.save(characterBase);
            }

            System.gc();

            effectService.startEffectScheduler();
            Rpg.get().getClassService().loadClasses();

            Comparator<CharacterBase> cmp = Comparator.comparing(CharacterBase::getUpdated);

            for (Player player : Sponge.getServer().getOnlinePlayers()) {
                List<CharacterBase> playersCharacters = characterService.getPlayersCharacters(player.getUniqueId());
                if (playersCharacters.isEmpty()) {
                    continue;
                }
                CharacterBase max = playersCharacters.stream().max(cmp).get();
                ISpongeCharacter activeCharacter = characterService.createActiveCharacter(player.getUniqueId(), max);
                characterService.setActiveCharacter(player.getUniqueId(), activeCharacter);
                characterService.invalidateCaches(activeCharacter);
                characterService.assignPlayerToCharacter(player.getUniqueId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Subcommand("add-class")
    public void addClassToCharacterCommand(Player executor, OnlinePlayer target, ClassDefinition klass) {
        ISpongeCharacter character = characterService.getCharacter(target.player);
        ActionResult actionResult = adminCommandFacade.addCharacterClass(character, klass);
        if (actionResult.isOk()) {
            executor.sendMessage(TextHelper.parse(Rpg.get().getLocalizationService().translate("class.set.ok")));
        } else {
            executor.sendMessage(TextHelper.parse(actionResult.getMessage()));
        }
    }


    @Subcommand("inspect property")
    public void inspectPropertyCommand(Player executor, OnlinePlayer target, String property) {
        try {
            int idByName = propertyService.getIdByName(property);
            IActiveCharacter character = characterService.getCharacter(target.player);
            executor.sendMessage(Text.of(TextColors.GOLD, "=================="));
            executor.sendMessage(Text.of(TextColors.GREEN, property));

            executor.sendMessage(Text.of(TextColors.GOLD, "Value", TextColors.WHITE, "/",
                    TextColors.AQUA, "Effective Value", TextColors.WHITE, "/",
                    TextColors.GRAY, "Cap",
                    TextColors.DARK_GRAY, " .##"));

            NumberFormat formatter = new DecimalFormat("#0.00");
            executor.sendMessage(Text.of(TextColors.GOLD, formatter.format(character.getProperty(idByName)), TextColors.WHITE, "/",
                    TextColors.AQUA, formatter.format(entityService.getEntityProperty(character, idByName)), TextColors.WHITE, "/",
                    TextColors.GRAY, formatter.format(propertyService.getMaxPropertyValue(idByName))));

            executor.sendMessage(Text.of(TextColors.GOLD, "=================="));
            executor.sendMessage(Text.of(TextColors.GRAY, "Memory/1 player: " + (character.getPrimaryProperties().length * 2 * 4) / 1024.0 + "kb"));

        } catch (Throwable t) {
            executor.sendMessage(Text.of("No such property"));
        }
    }

    @Subcommand("inspect item-damage")
    public void inspectItemDamageCommand(Player executor, OnlinePlayer oplayer) {
        Player player = oplayer.player;
        java.util.Optional<ItemStack> itemInHand = player.getItemInHand(HandTypes.MAIN_HAND);
        if (!itemInHand.isPresent()) {
            executor.sendMessage(Text.of(player.getName() + " has no item in main hand"));
            return;
        }
        ItemStack itemStack = itemInHand.get();
        java.util.Optional<RpgItemType> rpgItemType = itemService.getRpgItemType(itemStack);
        if (!rpgItemType.isPresent()) {
            executor.sendMessage(Text.of(player.getName() + " has no Managed item in main hand"));
            return;
        }
        RpgItemType fromItemStack = rpgItemType.get();
        ItemClass itemClass = fromItemStack.getItemClass();
        List<ItemClass> parents = new LinkedList<>();
        ItemClass parent = itemClass.getParent();
        List<Integer> o = new ArrayList<>();
        o.addAll(itemClass.getProperties());
        o.addAll(itemClass.getPropertiesMults());
        while (parent != null) {
            parents.add(parent);
            o.addAll(parent.getPropertiesMults());
            o.addAll(parent.getProperties());
            parent = parent.getParent();
        }
        parents.add(itemClass);
        Collections.reverse(parents);

        List<Text> a = new ArrayList<>();
        for (ItemClass wc : parents) {
            a.addAll(TO_TEXT.apply(wc));
        }
        for (Text text : a) {
            executor.sendMessage(text);
        }
        executor.sendMessage(Text.of(TextColors.GOLD, "=================="));


        IActiveCharacter character = characterService.getCharacter(player);
        executor.sendMessage(Text.of(TextColors.RED, "Damage: ", damageService.getCharacterItemDamage(character, fromItemStack)));
        executor.sendMessage(Text.of(TextColors.RED, "Details: "));
        executor.sendMessage(Text.of(TextColors.GRAY, " - From Item: ", character.getBaseWeaponDamage(fromItemStack)));

        Collection<PlayerClassData> values = character.getClasses().values();
        for (PlayerClassData value : values) {
            Set<ClassItem> weapons = value.getClassDefinition().getWeapons();
            for (ClassItem weapon : weapons) {
                if (weapon.getType() == fromItemStack) {
                    executor.sendMessage(Text.of(TextColors.GRAY, "  - From Class: " + weapon.getDamage()));
                }
            }
        }


        executor.sendMessage(Text.of(TextColors.GRAY, " - From ItemClass: "));
        Iterator<Integer> iterator = o.iterator();
        while (iterator.hasNext()) {
            int integer = iterator.next();
            String nameById = propertyService.getNameById(integer);

            if (nameById != null && !nameById.endsWith("_mult")) {
                iterator.remove();
            } else continue;

            executor.sendMessage(Text.of(TextColors.GRAY, "   - ", nameById, ":", entityService.getEntityProperty(character, integer)));
        }
        executor.sendMessage(Text.of(TextColors.GRAY, "   - Mult: "));
        iterator = o.iterator();
        while (iterator.hasNext()) {
            int integer = iterator.next();
            String nameById = propertyService.getNameById(integer);
            executor.sendMessage(Text.of(TextColors.GRAY, "   - ", nameById, ":", entityService.getEntityProperty(character, integer)));
        }
    }

    private Function<ItemClass, List<Text>> TO_TEXT = weaponClass -> {
        List<Text> list = new ArrayList<>();

        list.add(Text.of(TextColors.GOLD, weaponClass.getName()));
        for (Integer property : weaponClass.getProperties()) {
            list.add(Text.of(TextColors.GRAY, " -> ", propertyService.getNameById(property)));
        }
        for (Integer property : weaponClass.getPropertiesMults()) {
            list.add(Text.of(TextColors.GRAY, " -> ", propertyService.getNameById(property)));
        }
        return list;
    };

}