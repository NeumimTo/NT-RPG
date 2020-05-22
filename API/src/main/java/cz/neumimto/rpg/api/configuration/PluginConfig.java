/*
 *   Copyright (c) 2015, NeumimTo https://github.com/NeumimTo
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package cz.neumimto.rpg.api.configuration;

import com.electronwill.nightconfig.core.conversion.Conversion;
import com.electronwill.nightconfig.core.conversion.Converter;
import com.electronwill.nightconfig.core.conversion.Path;
import com.electronwill.nightconfig.core.conversion.PreserveNotNull;
import com.typesafe.config.Optional;
import cz.neumimto.rpg.api.logging.Log;
import cz.neumimto.rpg.api.utils.DebugLevel;

import java.util.*;

/**
 * Created by NeumimTo on 26.12.2014.
 */
public class PluginConfig {

    @Path("OVERRIDE_MOBS")
    public boolean OVERRIDE_MOBS = false;

    @Path("COMBAT_TIME")
    public long COMBAT_TIME = 20000L;

    @Path("REMOVE_PLAYERDATA_AFTER_PERMABAN")
    public boolean REMOVE_PLAYERDATA_AFTER_PERMABAN = false;

    @Path("DEBUG")
    public DebugLevel DEBUG = DebugLevel.BALANCE;

    @Path("PLAYER_AUTO_CHOOSE_LAST_PLAYED_CHAR")
    public boolean PLAYER_AUTO_CHOOSE_LAST_PLAYED_CHAR = true;

    @Path("SKILLGAIN_MESSAGES_AFTER_LOGIN")
    public boolean SKILLGAIN_MESSAGES_AFTER_LOGIN = true;

    @Path("PLAYER_LEARNED_SKILL_GLOBAL_MESSAGE")
    public boolean PLAYER_LEARNED_SKILL_GLOBAL_MESSAGE = true;

    @Path("PLAYER_UPGRADED_SKILL_GLOBAL_MESSAGE")
    public boolean PLAYER_UPGRADED_SKILL_GLOBAL_MESSAGE = true;

    @Path("PLAYER_REFUNDED_SKILL_GLOBAL_MESSAGE")
    public boolean PLAYER_REFUNDED_SKILL_GLOBAL_MESSAGE = true;

    @Path("ATTRIBUTEPOINTS_ON_START")
    public int ATTRIBUTEPOINTS_ON_START = 1;

    @Path("PLAYER_MAX_CHARS")
    public int PLAYER_MAX_CHARS = 5;

    @Path("JJS_ARGS")
    public String JJS_ARGS = "--optimistic-types=true";

    @Path("MANA_REGENERATION_RATE")
    public long MANA_REGENERATION_RATE = 1000;

    @Path("ALLOW_COMBAT_FOR_CHARACTERLESS_PLAYERS")
    public boolean ALLOW_COMBAT_FOR_CHARACTERLESS_PLAYERS = true;

    @Path("ALLOWED_RUNES_ITEMTYPES")
    @Conversion(SetToListConverter.class)
    public Set<String> ALLOWED_RUNES_ITEMTYPES = new HashSet<String>() {{
        add("minecraft:nether_star");
    }};

    @Path("AUTOREMOVE_NONEXISTING_RUNEWORDS")
    public boolean AUTOREMOVE_NONEXISTING_RUNEWORDS = false;

    @Path("PARTY_EXPERIENCE_MULTIPLIER")
    public double PARTY_EXPERIENCE_MULTIPLIER = 2;

    @Path("PARTY_EXPERIENCE_SHARE_DISTANCE")
    public double PARTY_EXPERIENCE_SHARE_DISTANCE = 25;

    @Path("MAX_PARTY_SIZE")
    public double MAX_PARTY_SIZE = -1;


    /* "If a player chooses a race and a class, where both those classes define damage value for one specific weapon, or "
            + "projectile" +
            " this option specifies how the weapon damage will be calculated." +
            "1 = sum" +
            "2 = take highest value")

     */
    @Path("WEAPON_MERGE_STRATEGY")
    public int WEAPON_MERGE_STRATEGY = 2;

    @Path("PLAYER_CHOOSED_SKILLTREE_SPECIALIZATION_GLOBAL_MESSAGE")
    public boolean PLAYER_CHOOSED_SKILLTREE_SPECIALIZATION_GLOBAL_MESSAGE;

    @Path("PATH_NODES_SEALED")
    public boolean PATH_NODES_SEALED = true;

    @Path("SHIFT_CANCELS_COMBO")
    public boolean SHIFT_CANCELS_COMBO = false;

    @Path("ENABLED_Q")
    public boolean ENABLED_Q;

    @Path("ENABLED_E")
    public boolean ENABLED_E;

    @Path("CLICK_COMBO_MAX_INVERVAL_BETWEEN_ACTIONS")
    public long CLICK_COMBO_MAX_INVERVAL_BETWEEN_ACTIONS = 1250;

    @Path("ITEM_LORE_EFFECT_NAME_COLOR")
    public String ITEM_LORE_EFFECT_NAME_COLOR = "BLUE";

    @Path("ITEM_LORE_EFFECT_COLON_COLOR")
    public String ITEM_LORE_EFFECT_COLON_COLOR = "DARK_GRAY";

    @Path("ITEM_LORE_EFFECT_VALUE_COLOR")
    public String ITEM_LORE_EFFECT_VALUE_COLOR = "LIGHT_PURPLE";

    @Path("ITEM_LORE_EFFECT_SECTION_COLOR")
    public String ITEM_LORE_EFFECT_SECTION_COLOR = "BLUE";

    @Path("ITEM_LORE_GROUP_MIN_LEVEL_COLOR")
    public String ITEM_LORE_GROUP_MIN_LEVEL_COLOR = "DARK_PURPLE";

    @Path("ITEM_LORE_ORDER")
    public List<String> ITEM_LORE_ORDER = Arrays.asList("META",
            "EFFECTS",
            "ATTRIBUTES",
            "SOCKETS",
            "REQUIREMENTS");

    @Path("CLASS_ITEM_DAMAGE_PROCESSOR")
    @Conversion(ItemDamageProcessorConverter.class)
    public ItemDamageProcessor CLASS_ITEM_DAMAGE_PROCESSOR = new Max();


    @Path("ITEM_DAMAGE_PROCESSOR")
    @Conversion(ItemDamageProcessorConverter.class)
    public ItemDamageProcessor ITEM_DAMAGE_PROCESSOR = new Sum();


    @Path("SKILLTREE_RELATIONS")
    public List<String> SKILLTREE_RELATIONS = new ArrayList<String>() {{
        add("|,minecraft:stick,|,0");
        add("/,minecraft:stick,/,0");
        add("\\\\,minecraft:stick,\\\\,0");
        add("-,minecraft:stick,-,0");
    }};

    //spigot only and once sponge moves its ass to 15.x
    @Path("SKILLTREE_GUI")
    public List<String> SKILLTREE_GUI = new ArrayList<String>() {{

        add("-,minecraft:cobblestone,1"); //horizontal
        add("I,minecraft:cobblestone,2"); //vertical
        add("<,minecraft:cobblestone,3");
        add(">,minecraft:cobblestone,4");
        add("^,minecraft:cobblestone,5");
        add("V,minecraft:cobblestone,6");
        add("+,minecraft:cobblestone,7"); //cross
        add("┐,minecraft:cobblestone,11");
        add("┘,minecraft:cobblestone,12"); //duh
        add("┌,minecraft:cobblestone,13");
        add("└,minecraft:cobblestone,14");
        add("┴,minecraft:cobblestone,15");
        add("┬,minecraft:cobblestone,16");
        add("┤,minecraft:cobblestone,17");
        add("├,minecraft:cobblestone,18");

        //    add("0,minecraft:gray_stained_glass_pane,19");//empty
    }};

    @Path("SKILLTREE_BUTTON_CONTROLLS")
    public List<String> SKILLTREE_BUTTON_CONTROLLS = new ArrayList<String>() {{
        add("North,minecraft:diamond_hoe,Up,1");
        add("West,minecraft:diamond_hoe,Right,2");
        add("East,minecraft:diamond_hoe,Left,3");
        add("South,minecraft:diamond_hoe,Down,4");
    }};

    @Path("ITEM_RARITY")
    public List<String> ITEM_RARITY = new ArrayList<String>() {{
        add("0,Common");
        add("1,&9Rare");
        add("2,&eUnique");
        add("3,&5Legendary");
    }};

    @Path("AUTODISCOVER_ITEMS")
    public boolean AUTODISCOVER_ITEMS = true;


    @Path("CREATE_FIRST_CHAR_AFTER_LOGIN")
    public boolean CREATE_FIRST_CHAR_AFTER_LOGIN = true;

    @Path("LOCALE")
    public String LOCALE = "en";

    @Path("MAX_CLICK_COMBO_LENGTH")
    public int MAX_CLICK_COMBO_LENGTH = 6;

    @Path("PRIMARY_CLASS_TYPE")
    public String PRIMARY_CLASS_TYPE = "Primary";

    @Path("RESPECT_CLASS_SELECTION_ORDER")
    public boolean RESPECT_CLASS_SELECTION_ORDER = true;

    @Path("CLASS_TYPES")
    @Conversion(ClassTypesDeserializer.class)
    public Map<String, ClassTypeDefinition> CLASS_TYPES = new LinkedHashMap<String, ClassTypeDefinition>() {{
        put("Race", new ClassTypeDefinition("GREEN", "DARK_GREEN", "GREEN", false, 1, 11111));
        put("Primary", new ClassTypeDefinition("YELLOW", "GOLD", "YELLOW", true, 2, 11112));
        put("Profession", new ClassTypeDefinition("GRAY", "BLACK", "GRAY", true, 3, 11113));
    }};

    @Path("RESPEC_ATTRIBUTES")
    public boolean RESPEC_ATTRIBUTES = false;

    @Path("DISABLED_WORLDS")
    @Conversion(SetToListConverter.class)
    public Set<String> DISABLED_WORLDS = new HashSet<>();

    @Path("ITEM_COOLDOWNS")
    public Boolean ITEM_COOLDOWNS = Boolean.TRUE;

    @PreserveNotNull
    @Path("SPELLBOOK_ROTATION_COOLDOWN")
    public long SPELLBOOK_COOLDOWN = 1000L;

    private static class ItemDamageProcessorConverter implements Converter<ItemDamageProcessor, String> {

        @Override
        public String convertFromField(ItemDamageProcessor value) {
            return value.getClass().getCanonicalName();
        }

        @Override
        public ItemDamageProcessor convertToField(String value) {
            try {
                return (ItemDamageProcessor) Class.forName(value).newInstance();
            } catch (Exception e) {
            }
            Log.error("Unknown item damage processor definition - Could not find class " + value + ". SEtting to Max()");
            return new Max();
        }
    }

    private static class SetToListConverter implements Converter<Set, List> {
        @Override
        public Set convertToField(List value) {
            HashSet hashSet = new HashSet();
            if (value != null) {
                hashSet.addAll(value);
            }
            return hashSet;
        }

        @Override
        public List convertFromField(Set value) {
            List list = new ArrayList();
            if (value != null) {
                list.addAll(value);
            }
            return list;
        }
    }
}
