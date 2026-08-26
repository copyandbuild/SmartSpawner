package github.nighter.smartspawner.fork;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.association.RegionAssociable;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Every direct reference to WorldGuard / WorldEdit lives in this class and
 * nowhere else.
 * <p>
 * That matters: without WorldGuard installed, merely touching a class that
 * mentions those types throws {@link NoClassDefFoundError}. {@link ForkFlags}
 * therefore stays free of such references and only reaches this class after it
 * confirmed WorldGuard is present, inside a {@code catch (Throwable)}.
 */
final class WorldGuardFlags {

    private static final Map<String, StateFlag> FLAGS = new HashMap<>();

    private WorldGuardFlags() {
    }

    static void register(String[] names) {
        FlagRegistry registry = com.sk89q.worldguard.WorldGuard.getInstance().getFlagRegistry();
        for (String name : names) {
            StateFlag flag = new StateFlag(name, true);
            try {
                registry.register(flag);
                FLAGS.put(name, flag);
            } catch (FlagConflictException conflict) {
                // Already registered (plugin reload) - reuse the existing one.
                Flag<?> existing = registry.get(name);
                if (existing instanceof StateFlag stateFlag) {
                    FLAGS.put(name, stateFlag);
                }
            }
        }
    }

    static boolean test(Player player, Location location, String name) {
        StateFlag flag = FLAGS.get(name);
        if (flag == null) {
            return true;
        }
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        return query().queryState(adapt(location), localPlayer, flag) != StateFlag.State.DENY;
    }

    static boolean test(Location location, String name) {
        StateFlag flag = FLAGS.get(name);
        if (flag == null) {
            return true;
        }
        return query().queryState(adapt(location), (RegionAssociable) null, flag) != StateFlag.State.DENY;
    }

    private static RegionQuery query() {
        RegionContainer container = com.sk89q.worldguard.WorldGuard.getInstance().getPlatform().getRegionContainer();
        return container.createQuery();
    }

    private static com.sk89q.worldedit.util.Location adapt(Location location) {
        return new com.sk89q.worldedit.util.Location(
                BukkitAdapter.adapt(location.getWorld()),
                location.getX(), location.getY(), location.getZ());
    }
}
