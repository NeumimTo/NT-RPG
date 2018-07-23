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

package cz.neumimto.rpg.persistance;

import com.typesafe.config.*;
import cz.neumimto.core.ioc.Inject;
import cz.neumimto.core.ioc.Singleton;
import cz.neumimto.rpg.Pair;
import cz.neumimto.rpg.ResourceLoader;
import cz.neumimto.rpg.gui.SkillTreeInterfaceModel;
import cz.neumimto.rpg.skills.*;
import cz.neumimto.rpg.utils.CatalogId;
import cz.neumimto.rpg.utils.Utils;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Created by NeumimTo on 24.7.2015.
 */
@Singleton
public class SkillTreeDao {

    @Inject
    SkillService skillService;

    @Inject
    Logger logger;

    public Map<String, SkillTree> getAll() {
        Path dir = ResourceLoader.skilltreeDir.toPath();
        Map<String, SkillTree> map = new HashMap<>();
        try (DirectoryStream<Path> paths = Files.newDirectoryStream(dir, "*.conf")) {
            paths.forEach(path -> {
                logger.info("Loading skilltree from a file " + path.getFileName());
                Config config = ConfigFactory.parseFile(path.toFile());
                SkillTree skillTree = new SkillTree();
                try {
                    skillTree.setDescription(config.getString("Description"));
                } catch (ConfigException e) {
                    skillTree.setDescription("");
                    logger.warn("Missing \"Description\" node");
                }
                try {
                    skillTree.setId(config.getString("Name"));
                } catch (ConfigException e) {
                    logger.warn("Missing \"Name\" skipping to another file");
                    return;
                }
                skillTree.getSkills().put(StartingPoint.name.toPlain(), StartingPoint.SKILL_DATA);
                try {
                    Config sub = config.getObject("Skills").toConfig();
                    createConfigSkills(sub, skillTree);
                    loadSkills(sub, skillTree);
                } catch (ConfigException e) {
                    logger.warn("Missing \"Skills\" section. No skills defined");

                }

                try {
                    List<String> asciiMap = config.getStringList("AsciiMap");
                    java.util.Optional<String> max = asciiMap.stream().max(Comparator.comparingInt(String::length));
                    if (max.isPresent()) {
                        int length = max.get().length();
                        int rows = asciiMap.size();

                        short[][] array = new short[rows][length];

                        int i = 0;
                        int j = 0;
                        StringBuilder num = new StringBuilder();
                        for (String s : asciiMap) {
                            for (char c1 : s.toCharArray()) {
                                if (Character.isDigit(c1)){
                                    num.append(c1);
                                    continue;
                                } else if (c1 == 'X') {
                                    skillTree.setCenter(new Pair<>(i,j));
                                    j++;
                                    continue;
                                }
                                if (!num.toString().equals("")) {
                                    array[i][j] = Short.parseShort(num.toString());
                                    j++;
                                }
                                SkillTreeInterfaceModel guiModelByCharacter = skillService.getGuiModelByCharacter(c1);
                                if (guiModelByCharacter != null){
                                    array[i][j] = guiModelByCharacter.getId();
                                }
                                num = new StringBuilder();
                                j++;
                            }
                            j=0;
                            i++;
                        }
                        skillTree.setSkillTreeMap(array);
                    }
                } catch (ConfigException | ArrayIndexOutOfBoundsException ignored) {
                    logger.error("Could not read ascii map in the skilltree " + skillTree.getId(), ignored);
                    skillTree.setSkillTreeMap(new short[][]{});
                }
                map.put(skillTree.getId(), skillTree);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    private void createConfigSkills(Config sub, SkillTree skillTree) {
        for (Map.Entry<String, ConfigValue> entry : sub.root().entrySet()) {
            String id = entry.getKey();
            Optional<ISkill> byId = skillService.getById(id);
            if (byId.isPresent()) {
                ConfigObject value = (ConfigObject) entry.getValue();
                Config c = value.toConfig();

                try {
                    String type = c.getString("type");
                    String sid = c.getString("id");
                    String name = c.getString("name");
                    SkillConfigLoader type1 = Sponge.getRegistry().getType(SkillConfigLoader.class, type)
                            .orElseThrow(() -> new IllegalArgumentException("Unknown skill type " + type + " in a skiltree " + skillTree.getId()));

                    ISkill build = type1.build(name);
                    build.setLocalizableName(Text.of(name));
                    injectCatalogId(build, sid);
                    skillService.registerAdditionalCatalog(build);


                } catch (ConfigException.Missing ignored) {}
            }
        }
    }

    private void injectCatalogId(ISkill skill, String name) {
        Optional<Field> first = Stream.of(skill.getClass().getFields()).filter(field -> field.isAnnotationPresent(CatalogId.class))
                .findFirst();
        Field field = first.get();
        field.setAccessible(true);
        try {
            field.set(skill, name);
        } catch (IllegalAccessException ignored) {
            //wonthappen
        }
    }

    private void loadSkills(Config sub, SkillTree skillTree) {
        for (Map.Entry<String, ConfigValue> entry : sub.root().entrySet()) {

            ConfigObject value = (ConfigObject) entry.getValue();
            Config c = value.toConfig();


            SkillData info = getSkillInfo(entry.getKey(), skillTree);

            try {
                info.setMaxSkillLevel(c.getInt("MaxSkillLevel"));
            } catch (ConfigException e) {
                info.setMaxSkillLevel(1);
                logger.warn("Missing \"MaxSkillLevel\" node for a skill \""+info.getSkillId()+"\", setting to 1");
            }
            try {
                String combination = c.getString("Combination");
                combination = combination.trim();
                if (!"".equals(combination)) {
                    info.setCombination(combination);
                }
            } catch (ConfigException e) {
            }

            try {
                info.setMinPlayerLevel(c.getInt("MinPlayerLevel"));
            } catch (ConfigException e) {
                info.setMinPlayerLevel(1);
                logger.warn("Missing \"MinPlayerLevel\" node for a skill \""+info.getSkillId()+"\", setting to 1");
            }

            try {
                info.setLevelGap(c.getInt("LevelGap"));
            } catch (ConfigException e) {
                info.setLevelGap(0);
                logger.warn("Missing \"LevelGap\" node for a skill \""+info.getSkillId()+"\", setting to 1");
            }

            try {
                for (String conflicts : c.getStringList("Conflicts")) {
                    info.getConflicts().add(getSkillInfo(conflicts, skillTree));
                }
            } catch (ConfigException ignored) {}

            try {
                for (String conflicts : c.getStringList("SoftDepends")) {
                    SkillData i = getSkillInfo(conflicts, skillTree);
                    info.getSoftDepends().add(i);
                    i.getDepending().add(info);
                }
            } catch (ConfigException ignored) {}


            try {
                for (String conflicts : c.getStringList("HardDepends")) {
                    SkillData i = getSkillInfo(conflicts, skillTree);
                    info.getHardDepends().add(i);
                    i.getDepending().add(info);
                }
            } catch (ConfigException ignored) {}

            try {
                info.setSkillTreeId(c.getInt("SkillTreeId"));
            } catch (ConfigException ignored) {
                logger.info(" - Skill " + info.getSkillId() + " missing SkillTreeId, it wont be possible to reference this skill in the ascii map");
            }

            try {
                Config settings = c.getConfig("SkillSettings");
                SkillSettings skillSettings = new SkillSettings();
                for (Map.Entry<String, ConfigValue> e : settings.entrySet()) {
                    if (e.getKey().endsWith(SkillSettings.bonus)) {
                        continue;
                    }
                    String val = e.getValue().render();
                    if (Utils.isNumeric(val)) {
                        float norm = Float.parseFloat(val);
                        String name = e.getKey();
                        skillSettings.addNode(name, norm);
                        name = name + SkillSettings.bonus;
                        float bonus = 0f;
                        try {
                            bonus = Float.parseFloat(settings.getString(name));
                        } catch (ConfigException ignored) {}
                        skillSettings.addNode(name, bonus);
                    } else {
                        skillSettings.addObjectNode(e.getKey(), val);
                    }
                }
                addRequiredIfMissing(skillSettings);
                info.setSkillSettings(skillSettings);
            } catch (ConfigException ignored) {}



            SkillLoadingErrors errors = new SkillLoadingErrors(skillTree.getId());
            try {
                info.getSkill().loadSkillData(info, skillTree, errors, c);
            } catch (ConfigException e) {

            }
            for (String s : errors.getErrors()) {
                logger.info(s);
            }


            skillTree.getSkills().put(info.getSkillId(), info);


        }
    }



    private void addRequiredIfMissing(SkillSettings skillSettings) {
        Map.Entry<String, Float> q = skillSettings.getFloatNodeEntry(SkillNodes.HPCOST.name());
        if (q == null) {
            skillSettings.addNode(SkillNodes.HPCOST, 0, 0);
        }
        q = skillSettings.getFloatNodeEntry(SkillNodes.MANACOST.name());
        if (q == null) {
            skillSettings.addNode(SkillNodes.MANACOST, 0, 0);
        }
        q = skillSettings.getFloatNodeEntry(SkillNodes.COOLDOWN.name());
        if (q == null) {
            skillSettings.addNode(SkillNodes.COOLDOWN, 0, 0);
        }
    }

    private SkillData getSkillInfo(String name, SkillTree tree) {
        SkillData info = tree.getSkills().get(name);
        if (info == null) {
            ISkill skill = skillService.getById(name)
                .orElseThrow(() -> new IllegalStateException("Could not find a skill " + name + " referenced in the skilltree " + tree.getId()));

            info = skill.constructSkillData();
            info.setSkill(skill);
            tree.getSkills().put(name, info);
        }
        return info;
    }
}
