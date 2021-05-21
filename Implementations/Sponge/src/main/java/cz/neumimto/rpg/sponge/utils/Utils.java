

package cz.neumimto.rpg.sponge.utils;

import com.flowpowered.math.imaginary.Quaterniond;
import com.flowpowered.math.vector.Vector3d;
import cz.neumimto.rpg.api.entity.IEntity;
import cz.neumimto.rpg.api.utils.Console;
import cz.neumimto.rpg.api.utils.MathUtils;
import cz.neumimto.rpg.common.entity.PropertyServiceImpl;
import cz.neumimto.rpg.sponge.entities.players.ISpongeCharacter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.property.entity.EyeLocationProperty;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.blockray.BlockRay;
import org.spongepowered.api.util.blockray.BlockRayHit;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.EntityUniverse;

import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cz.neumimto.rpg.api.logging.Log.info;

/**
 * Created by NeumimTo on 25.7.2015.
 */
public class Utils {

    public static String LineSeparator = System.getProperty("line.separator");
    public static String Tab = "\t";
    public static Set<BlockType> transparentBlocks = new HashSet<>();
    public static Predicate<BlockRayHit<World>> SKILL_TARGET_BLOCK_FILTER =
            (Predicate<BlockRayHit<World>>)
                    a -> !isTransparent(a.getExtent()
                            .getBlockType(a.getBlockX(), a.getBlockY(), a.getBlockZ()));
    public static Pattern REGEXP_CLASS_MEMBER = Pattern.compile("^[a-z_]\\w*$");

    static {
        transparentBlocks.addAll(Arrays.asList(BlockTypes.AIR,
                BlockTypes.GRASS, BlockTypes.TALLGRASS, BlockTypes.GRASS, BlockTypes.BED,
                BlockTypes.WHEAT, BlockTypes.FLOWER_POT, BlockTypes.FIRE, BlockTypes.WATER, BlockTypes.LAVA, BlockTypes.FLOWING_WATER));
    }

    public static Set<Entity> getNearbyEntities(Location l, int radius) {
        int chunkRadius = radius < 16 ? 1 : (radius - (radius % 16)) / 16;
        HashSet<Entity> set = new HashSet<>();
        double pow = Math.pow(radius, 2);
        for (int chX = 0 - chunkRadius; chX <= chunkRadius; chX++) {
            for (int chZ = 0 - chunkRadius; chZ <= chunkRadius; chZ++) {
                Location chunkLoc = new Location(l.getExtent(), l.getBlockX() + (chX * 16), l.getBlockY(), l.getBlockZ() + (chZ * 16));
                for (Entity e : chunkLoc.getExtent().getEntities()) {
                    if (e.getLocation().getPosition().distanceSquared(l.getPosition()) <= pow) {
                        set.add(e);
                    }
                }
            }
        }
        return set;
    }

    public static Set<Entity> getNearbyEntitiesPrecise(Location l, int radius) {
        int chunkRadius = radius < 16 ? 1 : (radius - (radius % 16)) / 16;
        HashSet<Entity> set = new HashSet<>();
        for (int chX = 0 - chunkRadius; chX <= chunkRadius; chX++) {
            for (int chZ = 0 - chunkRadius; chZ <= chunkRadius; chZ++) {
                Location chunkLoc = new Location(l.getExtent(), l.getBlockX() + (chX * 16), l.getBlockY(), l.getBlockZ() + (chZ * 16));
                for (Entity e : chunkLoc.getExtent().getEntities()) {
                    if (e.getLocation().getPosition().distance(l.getPosition()) <= radius) {
                        set.add(e);
                    }
                }
            }
        }
        return set;
    }

    public static Optional<Entity> spawnProjectile(IEntity caster, EntityType type) {
        return Optional.empty(); //todo
    }

    public static boolean isTransparent(BlockType e) {
        return transparentBlocks.contains(e);
    }

    public static Living getTargetedEntity(ISpongeCharacter character, int range) {
        Player player = character.getPlayer();

        Vector3d r = player.getRotation();
        Vector3d dir = Quaterniond.fromAxesAnglesDeg(r.getX(), -r.getY(), r.getZ()).getDirection();
        Vector3d vec3d = player.getProperty(EyeLocationProperty.class).get().getValue();
        Optional<EntityUniverse.EntityHit> e = player
                .getWorld()
                .getIntersectingEntities(vec3d, dir, range,
                        entityHit -> entityHit.getEntity() != character.getEntity() && isLivingEntity(entityHit.getEntity()))
                .stream().reduce((a, b) -> a.getDistance() < b.getDistance() ? a : b);

        if (e.isPresent()) {
            Optional<BlockRayHit<World>> end = BlockRay.from(player)
                    .distanceLimit(range)
                    .stopFilter(SKILL_TARGET_BLOCK_FILTER)
                    .build()
                    .end();
            if (!end.isPresent()) {
                return (Living) e.get().getEntity();
            } else {
                Entity entity = e.get().getEntity();
                Location<World> location = entity.getLocation();
                if (end.get().getBlockPosition()
                        .distanceSquared(location.getBlockX(), location.getBlockZ(), location.getBlockZ()) <= 2) {
                    return (Living) e.get().getEntity();
                }
            }
        }
        return null;
    }

    public static void hideProjectile(Projectile projectile) {
        projectile.offer(Keys.INVISIBLE, true);
    }

    public static String newLine(String s) {
        return Tab + s + LineSeparator;
    }

    /**
     * Resets stats of vanilla player object back to default state, Resets max hp, speed
     *
     * @param player
     */
    public static void resetPlayerToDefault(Player player) {
        player.offer(Keys.MAX_HEALTH, 20.0D);
        player.offer(Keys.WALKING_SPEED, PropertyServiceImpl.WALKING_SPEED);
    }

    /**
     * Inline negation of method references
     */
    public static <T> Predicate<T> not(Predicate<T> t) {
        return t.negate();
    }

    public static boolean isLivingEntity(Entity entity) {
        if (entity.isRemoved()) {
            return false;
        }
        Optional<Double> aDouble = entity.get(Keys.HEALTH);
        if (aDouble.isPresent()) {
            return aDouble.get() > 0;
        }
        return false;
    }

    public static void broadcastMessage(Text message, Player source, int radius) {
        double s = Math.pow(radius, 2);
        Collection<Player> onlinePlayers = Sponge.getServer().getOnlinePlayers();
        for (Player onlinePlayer : onlinePlayers) {
            if (onlinePlayer.getLocation().getPosition().distanceSquared(source.getLocation().getPosition()) <= s) {
                onlinePlayer.sendMessage(message);
            }
        }
    }


    public static String extractClassMember(String string) {
        Matcher matcher = REGEXP_CLASS_MEMBER.matcher(string);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    public static String configNodeToReadableString(String t) {
        String a = t.replaceAll("_", " ");
        a = a.substring(0, 1).toUpperCase() + a.substring(1);
        return a;
    }

    public static void executeCommandBatch(CommandSource commandSource, Map<String, String> variables, List<String> commandTemplates) {
        for (String commandTemplate : commandTemplates) {
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                commandTemplate = commandTemplate.replaceAll("\\{\\{" + entry.getKey() + "}}", entry.getValue());
            }
            try {
                info(Console.GREEN_BOLD + " Running Command (as a console): " + Console.YELLOW + commandTemplate);
                Sponge.getCommandManager().process(commandSource, commandTemplate);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static Location getLocationRelative(String location, Location original) {
        String[] split = location.split(";");
        String s = split[0];
        double x = original.getBlockX();
        x = getSinglePoint(s, x);

        s = split[1];
        double y = original.getBlockY();
        y = getSinglePoint(s, y);

        double z = original.getBlockZ();
        z = getSinglePoint(s, z);


        return new Location(original.getExtent(), x, y, z);
    }

    public static Location getLocationRelative(String location) {
        String[] split = location.split(";");
        return new Location(Sponge.getServer().getWorld(split[0]).get(),
                Double.parseDouble(split[1]),
                Double.parseDouble(split[2]),
                Double.parseDouble(split[3])
        );
    }

    private static double getSinglePoint(String s, double n) {
        if (s.startsWith("~")) {
            return n + Double.parseDouble(MathUtils.extractNumber(s));
        } else {
            return Double.parseDouble(MathUtils.extractNumber(s));
        }
    }
}
