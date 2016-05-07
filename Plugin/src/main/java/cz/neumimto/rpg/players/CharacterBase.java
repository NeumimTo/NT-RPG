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

package cz.neumimto.rpg.players;

import cz.neumimto.rpg.TimestampEntity;
import cz.neumimto.rpg.persistance.converters.UUID2String;

import javax.persistence.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by NeumimTo on 27.1.2015.
 */

@Entity
@Table(name = "CharacterBase",
        indexes = {@Index(columnList = "uuid")})
public class CharacterBase extends TimestampEntity {

    protected int attributePoints;
    protected int skillPoints;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    //todo locking
    // @Version
    private long version;
    @Convert(converter = UUID2String.class)
    private UUID uuid;
    @Column(length = 40)
    private String name;
    private String info;
    private int usedAttributePoints;

    private int usedSkillPoints;

    private boolean canResetskills;

    private String race, primaryClass;

    //TODO ehcache!
    private int guildid;

    @Column(length = 16)
    private String lastKnownPlayerName;

    @Column(name = "last_reset_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastReset;

    //todo lazy
    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "skill", length = 32)
    @Column(name = "expire_time")
    @CollectionTable(name = "cooldowns", joinColumns = @JoinColumn(name = "CharacterBase_id", referencedColumnName = "id"))
    private Map<String, Long> cooldowns = new ConcurrentHashMap<>();

    //todo lazy
    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "skill", length = 32)
    @Column(name = "level")
    @CollectionTable(name = "skills", joinColumns = @JoinColumn(name = "CharacterBase_id", referencedColumnName = "id"))
    private Map<String, Integer> skills = new HashMap<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "class")
    @CollectionTable(joinColumns = @JoinColumn(name = "CharacterBase_id", referencedColumnName = "id"))
    private Map<String, Double> classes = new HashMap<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "attribute")
    @CollectionTable(joinColumns = @JoinColumn(name = "CharacterBase_id", referencedColumnName = "id"))
    private Map<String, Integer> attributes = new HashMap<>();

    private int X;

    private int Y;

    private int Z;

    private String world;

    public int getAttributePoints() {
        return attributePoints;
    }

    public void setAttributePoints(int attributePoints) {
        this.attributePoints = attributePoints;
    }

    public int getSkillPoints() {
        return skillPoints;
    }

    public void setSkillPoints(int skillPoints) {
        this.skillPoints = skillPoints;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getRace() {
        return race;
    }

    public void setRace(String race) {
        this.race = race;
    }

    public int getGuildid() {
        return guildid;
    }

    public void setGuildid(int guildid) {
        this.guildid = guildid;
    }

    public String getLastKnownPlayerName() {
        return lastKnownPlayerName;
    }

    public void setLastKnownPlayerName(String lastKnownPlayerName) {
        this.lastKnownPlayerName = lastKnownPlayerName;
    }

    public Map<String, Double> getClasses() {
        return classes;
    }

    public void setClasses(Map<String, Double> classes) {
        this.classes = classes;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public Map<String, Long> getCooldowns() {
        return cooldowns;
    }

    public void setCooldowns(Map<String, Long> cooldowns) {
        this.cooldowns = cooldowns;
    }

    public Map<String, Integer> getSkills() {
        return skills;
    }

    public void setSkills(Map<String, Integer> skills) {
        this.skills = skills;
    }

    public int getUsedAttributePoints() {
        return usedAttributePoints;
    }

    public void setUsedAttributePoints(int usedAttributePoints) {
        this.usedAttributePoints = usedAttributePoints;
    }

    public int getUsedSkillPoints() {
        return usedSkillPoints;
    }

    public void setUsedSkillPoints(int usedSkillPoints) {
        this.usedSkillPoints = usedSkillPoints;
    }

    public boolean isCanResetskills() {
        return canResetskills;
    }

    public void setCanResetskills(boolean canResetskills) {
        this.canResetskills = canResetskills;
    }

    public String getPrimaryClass() {
        return primaryClass;
    }

    public void setPrimaryClass(String primaryClass) {
        this.primaryClass = primaryClass;
    }

    public Date getLastReset() {
        return lastReset;
    }

    public void setLastReset(Date lastReset) {
        this.lastReset = lastReset;
    }

    public Map<String, Integer> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Integer> attributes) {
        this.attributes = attributes;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public int getX() {
        return X;
    }

    public void setX(int x) {
        X = x;
    }

    public int getY() {
        return Y;
    }

    public void setY(int y) {
        Y = y;
    }

    public int getZ() {
        return Z;
    }

    public void setZ(int z) {
        Z = z;
    }

    public String getWorld() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

}