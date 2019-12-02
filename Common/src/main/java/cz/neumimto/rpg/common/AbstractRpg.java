package cz.neumimto.rpg.common;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandManager;
import com.electronwill.nightconfig.core.conversion.ObjectConverter;
import com.electronwill.nightconfig.core.file.FileConfig;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import cz.neumimto.rpg.api.IResourceLoader;
import cz.neumimto.rpg.api.Rpg;
import cz.neumimto.rpg.api.RpgAddon;
import cz.neumimto.rpg.api.RpgApi;
import cz.neumimto.rpg.api.classes.ClassService;
import cz.neumimto.rpg.api.configuration.ClassTypeDefinition;
import cz.neumimto.rpg.api.configuration.PluginConfig;
import cz.neumimto.rpg.api.damage.DamageService;
import cz.neumimto.rpg.api.effects.IEffectService;
import cz.neumimto.rpg.api.entity.EntityService;
import cz.neumimto.rpg.api.entity.IPropertyService;
import cz.neumimto.rpg.api.entity.players.ICharacterService;
import cz.neumimto.rpg.api.entity.players.parties.PartyService;
import cz.neumimto.rpg.api.events.EventFactoryService;
import cz.neumimto.rpg.api.exp.IExperienceService;
import cz.neumimto.rpg.api.inventory.InventoryService;
import cz.neumimto.rpg.api.items.ItemService;
import cz.neumimto.rpg.api.localization.Arg;
import cz.neumimto.rpg.api.localization.LocalizationService;
import cz.neumimto.rpg.api.logging.Log;
import cz.neumimto.rpg.api.scripting.IScriptEngine;
import cz.neumimto.rpg.api.skills.SkillService;
import cz.neumimto.rpg.api.utils.FileUtils;
import cz.neumimto.rpg.api.utils.rng.PseudoRandomDistribution;
import cz.neumimto.rpg.common.commands.ACFBootstrap;

import javax.inject.Inject;
import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Stream;

public abstract class AbstractRpg implements RpgApi {

    @Inject
    private EventFactoryService eventFactory;

    @Inject
    private SkillService skillService;

    @Inject
    private LocalizationService localizationService;

    @Inject
    private PluginConfig pluginConfig;

    @Inject
    private DamageService damageService;

    @Inject
    private IEffectService effectService;

    @Inject
    private ClassService classService;

    @Inject
    private ItemService itemService;

    @Inject
    private InventoryService inventoryService;

    @Inject
    private IScriptEngine scriptEngine;

    @Inject
    private PartyService partyService;

    @Inject
    private IPropertyService propertyService;

    @Inject
    private EntityService entityService;

    @Inject
    private IResourceLoader iresourceLoader;

    @Inject
    private ICharacterService characterService;

    @Inject
    private IExperienceService experienceService;

    @Inject
    private Injector injector;
    
    private final String workingDirectory;

    public AbstractRpg(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    @Override
    public ItemService getItemService() {
        return itemService;
    }

    @Override
    public void broadcastLocalizableMessage(String message, Arg arg) {
        broadcastMessage(localizationService.translate(message, arg));
    }

    @Override
    public void broadcastLocalizableMessage(String message, String name, String localizableName) {
        broadcastMessage(localizationService.translate(message, name, localizableName));
    }

    @Override
    public EventFactoryService getEventFactory() {
        return eventFactory;
    }

    @Override
    public SkillService getSkillService() {
        return skillService;
    }

    @Override
    public LocalizationService getLocalizationService() {
        return localizationService;
    }

    @Override
    public PluginConfig getPluginConfig() {
        return pluginConfig;
    }
    
    @Override
    public ICharacterService getCharacterService() {
        return characterService;
    }

    @Override
    public EntityService getEntityService() {
        return entityService;
    }

    @Override
    public DamageService getDamageService() {
        return damageService;
    }

    @Override
    public IPropertyService getPropertyService() {
        return propertyService;
    }

    @Override
    public PartyService getPartyService() {
        return partyService;
    }

    @Override
    public String getWorkingDirectory() {
        return workingDirectory;
    }

    @Override
    public IResourceLoader getResourceLoader() {
        return iresourceLoader;
    }

    @Override
    public ClassService getClassService() {
        return classService;
    }

    @Override
    public IEffectService getEffectService() {
        return effectService;
    }

    @Override
    public IScriptEngine getScriptEngine() {
        return scriptEngine;
    }

    @Override
    public InventoryService getInventoryService() {
        return inventoryService;
    }

    @Override
    public IExperienceService getExperienceService() {
        return experienceService;
    }

    public void reloadMainPluginConfig() {
        File file = new File(getWorkingDirectory());
        if (!file.exists()) {
            file.mkdir();
        }
        File properties = new File(getWorkingDirectory(), "Settings.conf");
        if (!properties.exists()) {
            FileUtils.generateConfigFile(new PluginConfig(), properties);
        }

        try (FileConfig fileConfig = FileConfig.of(properties.getPath())) {
            fileConfig.load();
            PluginConfig pluginConfig = new PluginConfig();
            this.pluginConfig = new ObjectConverter().toObject(fileConfig, () -> pluginConfig);

            List<Map.Entry<String, ClassTypeDefinition>> list = new ArrayList<>(this.pluginConfig.CLASS_TYPES.entrySet());
            list.sort(Map.Entry.comparingByValue());

            Map<String, ClassTypeDefinition> result = new LinkedHashMap<>();
            for (Map.Entry<String, ClassTypeDefinition> entry : list) {
                result.put(entry.getKey(), entry.getValue());
            }
            this.pluginConfig.CLASS_TYPES = result;
        }
    }

    @Override
    public void init(Path workingDirPath, Object commandManager, Class[] commandClasses, RpgAddon defaultStorageImpl,
                     BiFunction<Map, Map<Class<?>, ?>, Module> fnInjProv, Consumer<Injector> injectorc) {
        int a = 0;
        PseudoRandomDistribution p = new PseudoRandomDistribution();
        PseudoRandomDistribution.C = new double[101];
        for (double i = 0.01; i <= 1; i += 0.01) {
            PseudoRandomDistribution.C[a] = p.c(i);
            a++;
        }

        AddonScanner.setDeployedDir(workingDirPath.resolve(".deployed"));
        AddonScanner.setAddonDir(workingDirPath.resolve("addons"));

        AddonScanner.prepareAddons();
        Set<RpgAddon> rpgAddons = AbstractResourceManager.discoverGuiceModules();
        AddonScanner.onlyReloads();

        Map bindings = new HashMap<>();
        bindings.putAll(defaultStorageImpl.getBindings());
        for (RpgAddon rpgAddon : rpgAddons) {
            bindings.putAll(rpgAddon.getBindings());
        }

        Map<Class<?>, ?> providers = new HashMap();
        Set<Class<?>> classesToLoad = AddonScanner.getClassesToLoad();
        Iterator<Class<?>> iterator = classesToLoad.iterator();
        try {
            while (iterator.hasNext()) {
                Class<?> next = iterator.next();
                if (RpgAddon.class.isAssignableFrom(next)) {
                    try {
                        RpgAddon addon = (RpgAddon) next.getConstructor().newInstance();
                        bindings.putAll(addon.getBindings());
                        Map map = new HashMap<>();
                        map.put("WORKINGDIR", workingDirPath.toAbsolutePath().toString());
                        providers = addon.getProviders(map);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            injector = Guice.createInjector(fnInjProv.apply(bindings, providers));
        } catch (Exception e) {
            Log.error("Could not create Guice Injector", e);
            return;
        }
        injectorc.accept(injector);

        URL pluginUrl = FileUtils.getPluginUrl();
        File file = new File(pluginUrl.getFile());
        Rpg.get().getResourceLoader().loadJarFile(file, true);

        for (RpgAddon rpgAddon : rpgAddons) {
            rpgAddon.processStageEarly(injector);
        }

        Rpg.get().getResourceLoader().initializeComponents();
        Locale locale = Locale.forLanguageTag(pluginConfig.LOCALE);
        try {
            getResourceLoader().reloadLocalizations(locale);
        } catch (Exception e) {
            Log.error("Could not read localizations in locale " + locale.toString() + " - " + e.getMessage());
        }

        getItemService().loadItemGroups(Paths.get(getWorkingDirectory()).resolve("ItemGroups.conf"));
        getInventoryService().load();
        getEventFactory().registerEventProviders();
        getExperienceService().load();

        getSkillService().init();
        getPropertyService().init(Paths.get(getWorkingDirectory() + "/Attributes.conf"), Paths.get(getWorkingDirectory() + File.separator + "properties_dump.info"));
        getPropertyService().reLoadAttributes(Paths.get(getWorkingDirectory() + "/Attributes.conf"));
        getPropertyService().loadMaximalServerPropertyValues(Paths.get(getWorkingDirectory(), "max_server_property_values.properties"));
        getScriptEngine().initEngine();
        getSkillService().load();
        getClassService().loadClasses();
        getEffectService().load();
        getEffectService().startEffectScheduler();
        getDamageService().init();

        BaseCommand[] objects = (BaseCommand[]) Stream.of(commandClasses).map(c -> injector.getInstance(c)).toArray();
        ACFBootstrap.initializeACF(((CommandManager) commandManager), objects);

        for (RpgAddon rpgAddon : rpgAddons) {
            rpgAddon.processStageLate(injector);
        }

    }

}
