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

package cz.neumimto.rpg;

import static cz.neumimto.rpg.Log.info;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.inject.Inject;
import cz.neumimto.configuration.ConfigMapper;
import cz.neumimto.core.PluginCore;
import cz.neumimto.core.ioc.IoC;
import cz.neumimto.rpg.commands.CommandService;
import cz.neumimto.rpg.configuration.ClassTypeDefinition;
import cz.neumimto.rpg.configuration.PluginConfig;
import cz.neumimto.rpg.configuration.Settings;
import cz.neumimto.rpg.inventory.data.InventoryCommandItemMenuData;
import cz.neumimto.rpg.inventory.data.MenuInventoryData;
import cz.neumimto.rpg.inventory.data.NKeys;
import cz.neumimto.rpg.inventory.data.SkillTreeInventoryViewControllsData;
import cz.neumimto.rpg.inventory.data.manipulators.EffectsData;
import cz.neumimto.rpg.inventory.data.manipulators.ItemAttributesData;
import cz.neumimto.rpg.inventory.data.manipulators.ItemLevelData;
import cz.neumimto.rpg.inventory.data.manipulators.ItemMetaHeader;
import cz.neumimto.rpg.inventory.data.manipulators.ItemMetaTypeData;
import cz.neumimto.rpg.inventory.data.manipulators.ItemRarityData;
import cz.neumimto.rpg.inventory.data.manipulators.ItemSocketsData;
import cz.neumimto.rpg.inventory.data.manipulators.ItemStackUpgradeData;
import cz.neumimto.rpg.inventory.data.manipulators.ItemSubtypeData;
import cz.neumimto.rpg.inventory.data.manipulators.LoreDamageData;
import cz.neumimto.rpg.inventory.data.manipulators.LoreDurabilityData;
import cz.neumimto.rpg.inventory.data.manipulators.MinimalItemGroupRequirementsData;
import cz.neumimto.rpg.inventory.data.manipulators.MinimalItemRequirementsData;
import cz.neumimto.rpg.inventory.data.manipulators.SectionDelimiterData;
import cz.neumimto.rpg.inventory.data.manipulators.SkillBindData;
import cz.neumimto.rpg.inventory.data.manipulators.SkillTreeNode;
import cz.neumimto.rpg.inventory.items.ItemMetaType;
import cz.neumimto.rpg.inventory.items.ItemMetaTypeRegistry;
import cz.neumimto.rpg.inventory.items.ItemMetaTypes;
import cz.neumimto.rpg.inventory.items.subtypes.ItemSubtype;
import cz.neumimto.rpg.inventory.items.subtypes.ItemSubtypeRegistry;
import cz.neumimto.rpg.inventory.items.subtypes.ItemSubtypes;
import cz.neumimto.rpg.inventory.slotparsers.DefaultPlayerInvHandler;
import cz.neumimto.rpg.inventory.slotparsers.PlayerInvHandler;
import cz.neumimto.rpg.inventory.slotparsers.PlayerInvHandlerRegistry;
import cz.neumimto.rpg.inventory.sockets.SocketType;
import cz.neumimto.rpg.inventory.sockets.SocketTypeRegistry;
import cz.neumimto.rpg.inventory.sockets.SocketTypes;
import cz.neumimto.rpg.listeners.DebugListener;
import cz.neumimto.rpg.persistance.model.BaseCharacterAttribute;
import cz.neumimto.rpg.persistance.model.CharacterClass;
import cz.neumimto.rpg.persistance.model.CharacterSkill;
import cz.neumimto.rpg.players.CharacterBase;
import cz.neumimto.rpg.players.ExperienceSource;
import cz.neumimto.rpg.players.ExperienceSourceRegistry;
import cz.neumimto.rpg.players.ExperienceSources;
import cz.neumimto.rpg.players.attributes.Attribute;
import cz.neumimto.rpg.players.properties.attributes.AttributeRegistry;
import cz.neumimto.rpg.players.properties.attributes.ICharacterAttribute;
import cz.neumimto.rpg.skills.ISkill;
import cz.neumimto.rpg.skills.ISkillType;
import cz.neumimto.rpg.skills.NDamageType;
import cz.neumimto.rpg.skills.SkillService;
import cz.neumimto.rpg.skills.SkillTypeRegistry;
import cz.neumimto.rpg.skills.configs.SkillConfigLoader;
import cz.neumimto.rpg.skills.configs.SkillConfigLoaderRegistry;
import cz.neumimto.rpg.skills.configs.SkillConfigLoaders;
import cz.neumimto.rpg.skills.mods.SkillPreProcessorFactory;
import cz.neumimto.rpg.skills.mods.SkillPreProcessorFactoryRegistry;
import cz.neumimto.rpg.skills.mods.SkillPreprocessorFactories;
import cz.neumimto.rpg.skills.tree.SkillType;
import cz.neumimto.rpg.utils.EditorMappings;
import cz.neumimto.rpg.utils.FileUtils;
import cz.neumimto.rpg.utils.Placeholders;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.entity.damage.DamageType;
import org.spongepowered.api.event.game.GameRegistryEvent;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.SpongeExecutorService;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Resource;

import static cz.neumimto.rpg.Log.info;
import static cz.neumimto.rpg.Log.warn;

/**
 * Created by NeumimTo on 29.4.2015.
 */
@Plugin(id = "nt-rpg", version = "@VERSION@", name = "NT-Rpg", description = "RPG features for sponge", dependencies = {
		@Dependency(id = "nt-core", version = "1.13-SNAPSHOT-6"),
		@Dependency(id = "placeholderapi", version = "4.5", optional = true)
})
@Resource
public class NtRpgPlugin {

	public static String workingDir;
	public static File pluginjar;
	public static GlobalScope GlobalScope;
	public static SpongeExecutorService asyncExecutor;
	public static PluginConfig pluginConfig;

	@Inject
	public Logger logger;

	@Inject
	PluginContainer plugin;

	@Inject
	@ConfigDir(sharedRoot = false)
	private Path config;


	@Listener
	public void preinit(GamePreInitializationEvent e) {
		PluginCore.MANAGED_JPA_TYPES.add(CharacterBase.class);
		PluginCore.MANAGED_JPA_TYPES.add(BaseCharacterAttribute.class);
		PluginCore.MANAGED_JPA_TYPES.add(CharacterSkill.class);
		PluginCore.MANAGED_JPA_TYPES.add(CharacterClass.class);
		Sponge.getEventManager().registerListeners(this, new PersistenceHandler(this));
		new NKeys();
		DataRegistration.<InventoryCommandItemMenuData, InventoryCommandItemMenuData.Immutable>builder()
				.manipulatorId("custom_inventory_command")
				.dataName("Custom Inventory Command")
				.dataClass(InventoryCommandItemMenuData.class)
				.immutableClass(InventoryCommandItemMenuData.Immutable.class)
				.builder(new InventoryCommandItemMenuData.InventoryCommandItemMenuDataBuilder())
				.buildAndRegister(plugin);

		DataRegistration.<MenuInventoryData, MenuInventoryData.Immutable>builder()
				.manipulatorId("menu_inventory")
				.dataName("Menu Item")
				.dataClass(MenuInventoryData.class)
				.immutableClass(MenuInventoryData.Immutable.class)
				.builder(new MenuInventoryData.Builder())
				.buildAndRegister(plugin);


		DataRegistration.builder()
				.dataName("Item Effects")
				.manipulatorId("item_effects")
				.dataClass(EffectsData.class)
				.immutableClass(EffectsData.Immutable.class)
				.builder(new EffectsData.EffectDataBuilder())
				.buildAndRegister(plugin);


		DataRegistration.<ItemAttributesData, ItemAttributesData.Immutable>builder()
				.dataClass(ItemAttributesData.class)
				.immutableClass(ItemAttributesData.Immutable.class)
				.builder(new ItemAttributesData.Builder())
				.manipulatorId("ntrpg-itemattributes")
				.dataName("ItemAttributesData")
				.buildAndRegister(plugin);

		DataRegistration.<ItemLevelData, ItemLevelData.Immutable>builder()
				.dataClass(ItemLevelData.class)
				.immutableClass(ItemLevelData.Immutable.class)
				.builder(new ItemLevelData.Builder())
				.manipulatorId("ntrpg-itemleveldata")
				.dataName("ItemLevelData")
				.buildAndRegister(plugin);

		DataRegistration.<ItemRarityData, ItemRarityData.Immutable>builder()
				.dataClass(ItemRarityData.class)
				.immutableClass(ItemRarityData.Immutable.class)
				.builder(new ItemRarityData.Builder())
				.manipulatorId("ntrpg-rarity")
				.dataName("ItemRarityData")
				.buildAndRegister(plugin);

		DataRegistration.builder()
				.dataName("Item Sockets")
				.manipulatorId("item_sockets")
				.dataClass(ItemSocketsData.class)
				.immutableClass(ItemSocketsData.Immutable.class)
				.builder(new ItemSocketsData.Builder())
				.buildAndRegister(plugin);


		DataRegistration.<LoreDamageData, LoreDamageData.Immutable>builder()
				.dataClass(LoreDamageData.class)
				.immutableClass(LoreDamageData.Immutable.class)
				.builder(new LoreDamageData.Builder())
				.manipulatorId("ntrpg-loredamage")
				.dataName("LoreDamageData")
				.buildAndRegister(plugin);

		DataRegistration.<LoreDurabilityData, LoreDurabilityData.Immutable>builder()
				.dataClass(LoreDurabilityData.class)
				.immutableClass(LoreDurabilityData.Immutable.class)
				.builder(new LoreDurabilityData.Builder())
				.manipulatorId("ntrpg-loredurability")
				.dataName("LoreDurabilityData")
				.buildAndRegister(plugin);

		DataRegistration.<MinimalItemRequirementsData, MinimalItemRequirementsData.Immutable>builder()
				.dataClass(MinimalItemRequirementsData.class)
				.immutableClass(MinimalItemRequirementsData.Immutable.class)
				.builder(new MinimalItemRequirementsData.Builder())
				.manipulatorId("ntrpg-minimalrequirements")
				.dataName("MinimalItemRequirementsData")
				.buildAndRegister(plugin);

		DataRegistration.<SectionDelimiterData, SectionDelimiterData.Immutable>builder()
				.dataClass(SectionDelimiterData.class)
				.immutableClass(SectionDelimiterData.Immutable.class)
				.builder(new SectionDelimiterData.Builder())
				.manipulatorId("ntrpg-sectiondelimiter")
				.dataName("SectionDelimiterData")
				.buildAndRegister(plugin);

		DataRegistration.<ItemStackUpgradeData, ItemStackUpgradeData.Immutable>builder()
				.manipulatorId("itemstack_upgrade")
				.dataName("ItemStack Upgrade")
				.dataClass(ItemStackUpgradeData.class)
				.immutableClass(ItemStackUpgradeData.Immutable.class)
				.builder(new ItemStackUpgradeData.Builder())
				.buildAndRegister(plugin);

		DataRegistration.<SkillTreeInventoryViewControllsData, SkillTreeInventoryViewControllsData.Immutable>builder()
				.manipulatorId("skilltree_controlls")
				.dataName("SkillTree Controll Buttons")
				.dataClass(SkillTreeInventoryViewControllsData.class)
				.immutableClass(SkillTreeInventoryViewControllsData.Immutable.class)
				.builder(new SkillTreeInventoryViewControllsData.Builder())
				.buildAndRegister(plugin);

		DataRegistration.<SkillTreeNode, SkillTreeNode.Immutable>builder()
				.manipulatorId("skilltree_node")
				.dataName("SkillTree Node")
				.dataClass(SkillTreeNode.class)
				.immutableClass(SkillTreeNode.Immutable.class)
				.builder(new SkillTreeNode.Builder())
				.buildAndRegister(plugin);

		DataRegistration.<ItemMetaHeader, ItemMetaHeader.Immutable>builder()
				.manipulatorId("item_type_data")
				.dataName("Item Type data")
				.dataClass(ItemMetaHeader.class)
				.immutableClass(ItemMetaHeader.Immutable.class)
				.builder(new ItemMetaHeader.Builder())
				.buildAndRegister(plugin);


		DataRegistration.<MinimalItemGroupRequirementsData, MinimalItemGroupRequirementsData.Immutable>builder()
				.manipulatorId("item_minimal_group_requirements")
				.dataName("Item group requirements")
				.dataClass(MinimalItemGroupRequirementsData.class)
				.immutableClass(MinimalItemGroupRequirementsData.Immutable.class)
				.builder(new MinimalItemGroupRequirementsData.Builder())
				.buildAndRegister(plugin);

		DataRegistration.<ItemMetaTypeData, ItemMetaTypeData.Immutable>builder()
				.manipulatorId("item_meta_type")
				.dataName("Item meta type")
				.dataClass(ItemMetaTypeData.class)
				.immutableClass(ItemMetaTypeData.Immutable.class)
				.builder(new ItemMetaTypeData.Builder())
				.buildAndRegister(plugin);

		DataRegistration.<ItemSubtypeData, ItemSubtypeData.Immutable>builder()
				.manipulatorId("item_meta_subtype")
				.dataName("Item meta subtype")
				.dataClass(ItemSubtypeData.class)
				.immutableClass(ItemSubtypeData.Immutable.class)
				.builder(new ItemSubtypeData.Builder())
				.buildAndRegister(plugin);

		DataRegistration.<SkillBindData, SkillBindData.Immutable>builder()
				.manipulatorId("skill_bind")
				.dataName("SkillBind")
				.dataClass(SkillBindData.class)
				.immutableClass(SkillBindData.Immutable.class)
				.builder(new SkillBindData.Builder())
				.buildAndRegister(plugin);

		Sponge.getRegistry().registerModule(SocketType.class, new SocketTypeRegistry());
		Sponge.getRegistry().registerModule(Attribute.class, new AttributeCatalogTypeRegistry());
		Sponge.getRegistry().registerModule(PlayerInvHandler.class, new PlayerInvHandlerRegistry());
		Sponge.getRegistry().registerModule(ItemMetaType.class, new ItemMetaTypeRegistry());
		Sponge.getRegistry().registerModule(ItemSubtype.class, new ItemSubtypeRegistry());
		Sponge.getRegistry().registerModule(ISkillType.class, new SkillTypeRegistry());
		Sponge.getRegistry().registerModule(ExperienceSource.class, new ExperienceSourceRegistry());
		Sponge.getRegistry().registerModule(SkillConfigLoader.class, new SkillConfigLoaderRegistry());
		Sponge.getRegistry().registerModule(SkillPreProcessorFactory.class, new SkillPreProcessorFactoryRegistry());
	}

	@Listener
	public void postInit(GameRegistryEvent.Register<SocketType> event) {
		event.register(SocketTypes.ANY);
		event.register(SocketTypes.GEM);
		event.register(SocketTypes.JEWEL);
		event.register(SocketTypes.RUNE);
	}

	@Listener
	public void postInit1(GameRegistryEvent.Register<PlayerInvHandler> event) {
		event.register(new DefaultPlayerInvHandler());
	}

	@Listener
	public void postInit2(GameRegistryEvent.Register<ItemMetaType> event) {
		event.register(ItemMetaTypes.CHARM);
		event.register(ItemMetaTypes.RUNEWORD);
	}

	@Listener
	public void postInit3(GameRegistryEvent.Register<ItemSubtype> event) {
		event.register(ItemSubtypes.ANY);
	}

	@Listener
	public void postInit4(GameRegistryEvent.Register<ISkillType> event) {
		for (SkillType skillType : SkillType.values()) {
			event.register(skillType);
		}
	}

	@Listener
	public void postInit5(GameRegistryEvent.Register<SkillConfigLoader> event) {
		event.register(SkillConfigLoaders.ATTRIBUTE);
		event.register(SkillConfigLoaders.ITEM_ACCESS);
		event.register(SkillConfigLoaders.PROPERTY);
		event.register(SkillConfigLoaders.SKILLTREE_PATH);
	}

	@Listener
	public void postInit6(GameRegistryEvent.Register<ExperienceSource> event) {
		event.register(ExperienceSources.PVP);
		event.register(ExperienceSources.PVE);
		event.register(ExperienceSources.MINING);
		event.register(ExperienceSources.LOGGING);
		event.register(ExperienceSources.QUESTING);
		event.register(ExperienceSources.FISHING);
		event.register(ExperienceSources.FARMING);
	}

	@Listener
	public void postInit7(GameRegistryEvent.Register<DamageType> event) {
		event.register(NDamageType.DAMAGE_CHECK);
		event.register(NDamageType.ICE);
		event.register(NDamageType.LIGHTNING);
		event.register(NDamageType.MEELE_CRITICAL);
	}

	@Listener
	public void postInit8(GameRegistryEvent.Register<SkillPreProcessorFactory> event) {
		event.register(SkillPreprocessorFactories.UNCASTABLE);
		event.register(SkillPreprocessorFactories.ADJUSTED_SKILL_SETTINGS);
	}


	@Listener
	public void postInit9(GameRegistryEvent.Register<Attribute> event) {
		//?
	}

	@Listener
	public void onPluginLoad(GamePostInitializationEvent event) {
		long start = System.nanoTime();
		Log.logger = logger;
		try {
			workingDir = config.toString();
			URL url = FileUtils.getPluginUrl();
			pluginjar = new File(url.toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		reloadMainPluginConfig();
		IoC ioc = IoC.get();
		asyncExecutor = Sponge.getGame().getScheduler().createAsyncExecutor(NtRpgPlugin.this);

		ioc.registerInterfaceImplementation(Logger.class, logger);
		Game game = Sponge.getGame();
		Optional<PluginContainer> gui = game.getPluginManager().getPlugin("MinecraftGUIServer");
		if (gui.isPresent()) {
			//ioc.registerInterfaceImplementation(MinecraftGuiService.class, game.getServiceManager().provide(MinecraftGuiService.class).get());
		} else {
			Settings.ENABLED_GUI = false;
		}
		ioc.registerDependency(this);
		ioc.registerInterfaceImplementation(CauseStackManager.class, Sponge.getCauseStackManager());

		Path path = Paths.get(workingDir);
		ConfigMapper.init("NtRpg", path);
		ioc.registerDependency(ConfigMapper.get("NtRpg"));
		try {
			Files.createDirectories(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
		ioc.get(IoC.class, ioc);
		ResourceLoader rl = ioc.build(ResourceLoader.class);
		rl.loadJarFile(pluginjar, true);
		GlobalScope = ioc.build(GlobalScope.class);
		rl.loadExternalJars();

		if (pluginConfig.DEBUG.isBalance()) {
			Sponge.getEventManager().registerListeners(this, ioc.build(DebugListener.class));
		}
		IoC.get().build(CommandService.class).registerStandartCommands();
		IoC.get().build(Init.class).it();

		Sponge.getRegistry().registerModule(ISkill.class, IoC.get().build(SkillService.class));

		try {
			Class.forName("me.rojo8399.placeholderapi.PlaceholderService");
			Placeholders build = IoC.get().build(Placeholders.class);
			build.init();
			info("Placeholders Enabled");
		} catch (ClassNotFoundException e) {
			info("Placeholders Disabled");
		}

		ioc.postProcess();

		EditorMappings.dump();
		double elapsedTime = (System.nanoTime() - start) / 1000000000.0;
		info("NtRpg plugin successfully loaded in " + elapsedTime + " seconds");
	}

	public void reloadMainPluginConfig() {
		File file = new File(NtRpgPlugin.workingDir);
		if (!file.exists()) {
			file.mkdir();
		}
		File properties = new File(NtRpgPlugin.workingDir, "Settings.conf");
		if (!properties.exists()) {
			FileUtils.generateConfigFile(new PluginConfig(), properties);
		}
		try {
			ObjectMapper<PluginConfig> mapper = ObjectMapper.forClass(PluginConfig.class);
			HoconConfigurationLoader hcl = HoconConfigurationLoader.builder().setPath(properties.toPath()).build();
			pluginConfig = mapper.bind(new PluginConfig()).populate(hcl.load());

			List<Map.Entry<String, ClassTypeDefinition>> list = new ArrayList<>(pluginConfig.CLASS_TYPES.entrySet());
			list.sort(Map.Entry.comparingByValue());

			Map<String, ClassTypeDefinition> result = new LinkedHashMap<>();
			for (Map.Entry<String, ClassTypeDefinition> entry : list) {
				result.put(entry.getKey(), entry.getValue());
			}
			pluginConfig.CLASS_TYPES = result;
		} catch (ObjectMappingException | IOException e) {
			e.printStackTrace();
		}
	}
}
