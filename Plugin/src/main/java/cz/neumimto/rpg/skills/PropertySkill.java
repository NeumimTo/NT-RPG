package cz.neumimto.rpg.skills;

import com.typesafe.config.Config;
import cz.neumimto.core.localization.Arg;
import cz.neumimto.core.localization.TextHelper;
import cz.neumimto.rpg.NtRpgPlugin;
import cz.neumimto.rpg.configuration.Localizations;
import cz.neumimto.rpg.gui.GuiHelper;
import cz.neumimto.rpg.players.IActiveCharacter;
import cz.neumimto.rpg.players.properties.DefaultProperties;
import cz.neumimto.rpg.players.properties.PropertyService;
import cz.neumimto.rpg.utils.Utils;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

public class PropertySkill extends AbstractSkill{

    private PropertyService propertyService;

    public PropertySkill(String name) {
        super();
        setLocalizableName(TextHelper.parse(name));
        setIcon(ItemTypes.BLAZE_POWDER);
        propertyService = NtRpgPlugin.GlobalScope.propertyService;
    }

    @Override
    public SkillResult onPreUse(IActiveCharacter character) {
        return SkillResult.CANCELLED;
    }

    @Override
    public void onCharacterInit(IActiveCharacter c, int level) {
        super.onCharacterInit(c, level);
        add(c,1, (integer, integer2) -> integer <= integer2);
    }

    @Override
    public void skillLearn(IActiveCharacter IActiveCharacter) {
        super.skillLearn(IActiveCharacter);
        add(IActiveCharacter,1, (integer, integer2) -> integer <= integer2);
    }

    @Override
    public void skillRefund(IActiveCharacter IActiveCharacter) {
        super.skillRefund(IActiveCharacter);
        add(IActiveCharacter,-1, (integer, integer2) -> integer <= integer2);
    }

    @Override
    public void skillUpgrade(IActiveCharacter IActiveCharacter, int level) {
        super.skillUpgrade(IActiveCharacter, level);
        add(IActiveCharacter,1, Objects::equals);
    }

    private void add(IActiveCharacter character,  int i, BiFunction<Integer, Integer, Boolean> fc) {
        ExtendedSkillInfo skill = character.getSkill(getName());
        PropertySkillData skillData = (PropertySkillData) skill.getSkillData();
        for (Wrapper property : skillData.properties) {
            if (fc.apply(property.level, skill.getTotalLevel())) {
                character.addProperty(property.level, property.value * i);
                if (property.propertyId == DefaultProperties.max_health) {
                    characterService.updateMaxHealth(character);
                } else if (property.propertyId == DefaultProperties.walk_speed) {
                    characterService.updateWalkSpeed(character);
                } else if (property.propertyId == DefaultProperties.max_mana) {
                    characterService.updateMaxMana(character);
                }
            }
        }
    }

    @Override
    public <T extends SkillData> void loadSkillData(T skillData, SkillTree context, SkillLoadingErrors errors, Config c) {
        PropertySkillData data = (PropertySkillData) skillData;
        List<? extends Config> properties = c.getConfigList("Properties");
        for (Config cprop : properties) {
            int level = cprop.getInt("level");
            float value = (float) cprop.getDouble("value");
            String name = cprop.getString("property-name");

            try {
                int idByName = NtRpgPlugin.GlobalScope.propertyService.getIdByName(name);
                Wrapper wrapper = new Wrapper(name,idByName, level, value);
                data.properties.add(wrapper);
            } catch (NullPointerException e) {
                errors.log("Unknown property name %s in %s", name, context.getId());
            }

        }
    }

    @Override
    public List<ItemStack> configurationToItemStacks(SkillData skillData) {
        PropertySkillData data = (PropertySkillData) skillData;
        List<ItemStack> arrayList = new ArrayList<>();
        for (Wrapper wrapper : data.properties) {
            ItemStack is = GuiHelper.itemStack(ItemTypes.PAPER);
            List<Text> lore = new ArrayList<>();
            is.offer(Keys.DISPLAY_NAME, TextHelper.makeText(Utils.configNodeToReadableString(wrapper.propertyName), TextColors.GREEN));
            lore.add(Localizations.SKILL_LEVEL.toText(Arg.arg("level", wrapper.level)));
            lore.add(Text.builder(Utils.configNodeToReadableString(wrapper.propertyName))
                    .style(TextStyles.BOLD)
                    .color(TextColors.GOLD)
                    .append(
                            Text.builder(": " + (wrapper.value < 0 ? "-" : "+") + wrapper.value)
                                    .style(TextStyles.BOLD)
                                    .color(wrapper.value < 0 ? TextColors.RED : TextColors.GREEN)
                                    .build()

                ).build());
            is.offer(Keys.ITEM_LORE, lore);
            arrayList.add(is);
        }
        return arrayList;
    }

    @Override
    public PropertySkillData constructSkillData() {
        return new PropertySkillData(getName());
    }


    public class Wrapper {
        final String propertyName;
        final int propertyId;
        final int level;
        final float value;

        public Wrapper(String propertyName, int propertyId, int level, float value) {
            this.propertyName = propertyName;
            this.propertyId = propertyId;
            this.level = level;
            this.value = value;
        }
    }

    public class PropertySkillData extends SkillData {

        List<Wrapper> properties = new ArrayList<>();

        public PropertySkillData(String skill) {
            super(skill);
        }

        public List<Wrapper> getProperties() {
            return properties;
        }
    }
}
