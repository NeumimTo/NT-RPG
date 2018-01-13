package cz.neumimto.rpg.inventory.runewords;

import com.google.common.base.Splitter;
import cz.neumimto.core.ioc.Inject;
import cz.neumimto.core.ioc.PostProcess;
import cz.neumimto.core.ioc.Singleton;
import cz.neumimto.rpg.GroupService;
import cz.neumimto.rpg.NtRpgPlugin;
import cz.neumimto.rpg.Pair;
import cz.neumimto.rpg.configuration.PluginConfig;
import cz.neumimto.rpg.effects.EffectService;
import cz.neumimto.rpg.effects.EffectSourceType;
import cz.neumimto.rpg.effects.IGlobalEffect;
import cz.neumimto.rpg.events.RebuildRunewordEvent;
import cz.neumimto.rpg.inventory.InventoryService;
import cz.neumimto.rpg.inventory.data.NKeys;
import cz.neumimto.rpg.inventory.data.manipulators.ItemSocketsData;
import cz.neumimto.rpg.players.ExtendedNClass;
import cz.neumimto.rpg.players.IActiveCharacter;
import cz.neumimto.rpg.players.groups.PlayerGroup;
import cz.neumimto.rpg.utils.ItemStackUtils;
import cz.neumimto.rpg.utils.Utils;
import cz.neumimto.rpg.utils.XORShiftRnd;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by NeumimTo on 29.10.2015.
 */
@Singleton
public class RWService {

	private final Pattern socket = Pattern.compile("\\{@\\}");
	private final Path file = Paths.get(NtRpgPlugin.workingDir, "Runes.conf");

	@Inject
	private RWDao dao;

	@Inject
	private EffectService effectService;

	@Inject
	private Logger logger;

	@Inject
	private Game game;

	@Inject
	private InventoryService inventoryService;

	@Inject
	private GroupService groupService;

	private Map<String, RuneWord> runewords = new HashMap();
	private Map<String, RuneWord> combinations = new HashMap<>();
	private Map<String, Rune> runes = new HashMap<>();
	private List<ItemType> allowedRuneItemTypes = new ArrayList<>();


	@PostProcess(priority = 10000)
	public void load() {
		File p = file.toFile();
		if (!p.exists()) {
			try {
				p.createNewFile();
				Files.write(p.toPath(), "Runes:{},\nRuneWords:[]".getBytes());
				return;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		for (Rune rune : dao.getAllRunes(p)) {
			runes.put(rune.getName().toLowerCase(), rune);
		}
		Set<RuneWordTemplate> allRws = dao.getAllRws(p);
		for (RuneWordTemplate runeWord : allRws) {
			registerRuneword(getRuneword(runeWord));
		}
		for (String s : PluginConfig.ALLOWED_RUNES_ITEMTYPES) {
			Optional<ItemType> type = Sponge.getGame().getRegistry().getType(ItemType.class, s);
			if (type.isPresent()) {
				allowedRuneItemTypes.add(type.get());
			}
		}
	}

	protected RuneWord getRuneword(RuneWordTemplate template) {
		template.getEffects().keySet().stream().filter(Utils.not(effectService::isGlobalEffect)).forEach(e -> logger.warn("Runeword " + template + " defined non existing global effect:" + e));
		RuneWord rw = new RuneWord();
		rw.setName(template.getName());
		rw.setRunes(template.getRunes().stream()./*filter(this::existsRune).*/map(this::getRune).collect(Collectors.toList()));
		rw.setMinLevel(template.getMinLevel());

		rw.setAllowedItems(template.getAllowedItems()
				.stream()
				.map(a -> game.getRegistry().getType(ItemType.class, a))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toSet()));

		rw.setEffects(template.getEffects().entrySet().stream()
				.filter(l -> effectService.isGlobalEffect(l.getKey()))
				.map(a -> new Pair<>(effectService.getGlobalEffect(a.getKey()), a.getValue()))
				.collect(HashMap::new, (map, a) -> map.put(a.key, a.value), HashMap::putAll));

		return rw;
	}

	public RuneWord getRuneword(String name) {
		return runewords.get(name.toLowerCase());
	}

	public List<Text> addRune(List<Text> lore, Rune rune) {
		if (!hasEmptySocket(lore))
			return lore;
		String s = lore.get(1).toPlain();
		s = s.replaceFirst(socket.pattern(), rune.getName() + " ");
		lore.set(1, Text.of(s));
		return lore;
	}


	public int getSocketCount(List<Text> lore) {
		if (lore.size() < 2)
			return 0;
		String s = lore.get(1).toPlain();
		int c = 0;
		Matcher matcher = socket.matcher(s);
		while (matcher.find())
			c++;
		return c;
	}

	public boolean hasEmptySocket(List<Text> lore) {
		if (lore.size() < 2)
			return false;
		String s = lore.get(1).toPlain();
		Matcher matcher = socket.matcher(s);
		return matcher.find();
	}

	public RuneWord runeWordByCombinationAfterInsert(List<Text> lore) {
		if (lore.size() < 2)
			return null;
		String s = lore.get(1).toPlain();
		return combinations.get(s);
	}

	public boolean existsRune(String rune) {
		return runes.containsKey(rune.toLowerCase());
	}

	public boolean existsRuneword(String rw) {
		return runewords.containsKey(rw.toLowerCase());
	}

	public Rune getRune(String rune) {
		return runes.get(rune.toLowerCase());
	}

	public Map<String, Rune> getRunes() {
		return runes;
	}

	public ItemStack toItemStack(Rune r) {
		XORShiftRnd rnd = new XORShiftRnd();

		return null;
	}


	public ItemStack createSockets(ItemStack itemStack, int i) {
		Optional<Map<Text, Text>> textTextMap = itemStack.get(NKeys.ITEM_SOCKETS);
		Optional<ItemSocketsData> orCreate = itemStack.getOrCreate(ItemSocketsData.class);
		//orCreate.get().set(NKeys.ITEM_SOCKETS, )
		inventoryService.updateLore(itemStack);
		return itemStack;
	}

	public ItemStack insertRune(ItemStack itemStack, String currentRune) {
		Optional<List<Text>> texts = itemStack.get(Keys.ITEM_LORE);
		List<Text> t = texts.get();
		Text text = t.get(1);
		String s = text.toPlain();
		String s1 = s.replaceFirst("\\{@\\}", currentRune);
		t.set(1, Text.of(TextColors.RED, s1));
		itemStack.offer(Keys.ITEM_LORE, t);
		return itemStack;
	}

	public ItemStack findRuneword(ItemStack i) {
		Optional<List<Text>> texts = i.get(Keys.ITEM_LORE);
		if (texts.isPresent()) {
			List<Text> t = texts.get();
			if (t.size() < 2) {
				return i;
			}
			Text text = t.get(1);
			String s = text.toPlain();
			RuneWord rw = combinations.get(s);
			if (rw != null) {
				if (rw.getAllowedItems().contains(i.getType())) {
					if (rw.getAllowedItems().isEmpty()) {
						return reBuildRuneword(i, rw);
					} else {
						if (rw.getAllowedItems().contains(i.getType())) {
							return reBuildRuneword(i, rw);
						}
					}
				}
			}
		}
		return i;
	}

	public ItemStack reBuildRuneword(ItemStack i, RuneWord rw) {
		if (rw == null) {
			if (PluginConfig.AUTOREMOVE_NONEXISTING_RUNEWORDS) {
				i.offer(Keys.DISPLAY_NAME, Text.of(i.getType().getName()));
				i.offer(Keys.ITEM_LORE, Collections.<Text>emptyList());
				//i.offer(new CustomItemData());
			}
			return i;
		}
		i.offer(Keys.HIDE_ATTRIBUTES, true);
		i.offer(Keys.HIDE_MISCELLANEOUS, true);
		refreshItemLore(i, rw);
		RebuildRunewordEvent event = new RebuildRunewordEvent(rw, i);
		Sponge.getEventManager().post(event);
		i = event.getItemStack();
		return i;
	}


	public boolean canBeInserted(RuneWordTemplate template, ItemStack itemStack) {
		List<String> allowedItems = template.getAllowedItems();
		ItemType i = itemStack.getType();
		if (allowedItems.contains(itemStack.toString())) {
			return true;
		}
		for (String allowedItem : allowedItems) {
			if (allowedItem.startsWith("any")) {
				String[] split = allowedItem.split(" ");
				if (split.length == 1) {
					return true;
				}
				if (split.length == 2) {
					return ItemStackUtils.checkType(i, split[1]);
				}

			}
		}

		return false;
	}

	public boolean canUse(RuneWord rw, IActiveCharacter character) {
		if (rw == null) {
			return true;
		}
		if (character.isStub())
			return false;

		if (rw.getMinLevel() > 0 && character.getPrimaryClass() == null) {
			return false;
		}

		if (rw.getMinLevel() > character.getPrimaryClass().getLevel()) {
			return false;
		}

		for (PlayerGroup playerGroup : rw.getAllowedGroups()) {

			if (playerGroup.getType() == EffectSourceType.RACE) {

				if (character.getRace() == playerGroup) {
					return false;
				}
			} else if (playerGroup.getType() == EffectSourceType.CLASS)

				for (ExtendedNClass extendedNClass : character.getClasses()) {
					if (extendedNClass.getConfigClass() == playerGroup) {
						return false;
					}
				}

		}

		return true;
	}
	private Map<String, String> toMap(Map<IGlobalEffect, String> a) {
		Map<String, String> map = new HashMap<>();
		if (a != null) {
			for (Map.Entry<IGlobalEffect, String> q : a.entrySet()) {
				map.put(q.getKey().getName(), q.getValue());
			}
		}
		return map;
	}

	public void refreshItemLore(ItemStack itemStack, RuneWord runeword) {

	}

	private ItemStack setLore(ItemStack itemStack, RuneWord runeWord) {
		List<Text> arrayList = itemStack.get(Keys.ITEM_LORE).get();
		Iterable<String> a = Splitter.fixedLength(10).split(runeWord.getLore());
		for (String s : a) {
			arrayList.add(Text.builder(s).color(TextColors.GOLD).style(TextStyles.ITALIC).build());
		}
		itemStack.offer(Keys.ITEM_LORE, arrayList);
		return itemStack;
	}

	public List<ItemType> getAllowedRuneItemTypes() {
		return allowedRuneItemTypes;
	}

	public RuneWord findByCombination(String runes) {
		return combinations.get(runes);
	}

	public void registerRuneword(RuneWord runeWord) {
		runewords.put(runeWord.getName(), runeWord);
		if (runeWord.getRunes() != null && !runeWord.getRunes().isEmpty()) {
			combinations.put(runeWord.getRunes().stream().map(Rune::getName).collect(Collectors.joining("")), runeWord);
		}
	}

	public Map<String, RuneWord> getRunewords() {
		return runewords;
	}
}
