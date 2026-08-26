package github.nighter.smartspawner.fork;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Fork-only WorldGuard region flags.
 * <p>
 * Every flag defaults to {@code ALLOW}, so a server without these flags set
 * behaves exactly like upstream SmartSpawner. Without WorldGuard installed all
 * checks return {@code true}.
 * <p>
 * This class deliberately contains no WorldGuard imports - see
 * {@link WorldGuardFlags} for why.
 */
public final class ForkFlags {

    public static final String USE = "smartspawner-use";
    public static final String PLACE = "smartspawner-place";
    public static final String BREAK = "smartspawner-break";
    public static final String STACK = "smartspawner-stack";
    public static final String SELL = "smartspawner-sell";
    public static final String GENERATE = "smartspawner-generate";

    private static final String[] ALL = {USE, PLACE, BREAK, STACK, SELL, GENERATE};

    private static boolean available;

    private ForkFlags() {
    }

    /**
     * Call from {@code SmartSpawner#onLoad()} - WorldGuard only accepts custom
     * flags before it enables. Safe when WorldGuard is absent.
     */
    public static void register() {
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") == null) {
            return;
        }
        try {
            WorldGuardFlags.register(ALL);
            available = true;
            Bukkit.getLogger().info("[SmartSpawner/fork] WorldGuard flags registered.");
        } catch (Throwable throwable) {
            available = false;
            Bukkit.getLogger().warning("[SmartSpawner/fork] WorldGuard flags not registered: " + throwable);
        }
    }

    private static boolean active() {
        return available && ForkConfig.get().isWorldGuardFlagsEnabled();
    }

    /** Player-bound query. Returns true when WorldGuard is absent or the flag allows. */
    public static boolean test(Player player, Location location, String flagName) {
        if (!active() || player == null || location == null || location.getWorld() == null) {
            return true;
        }
        if (player.isOp() || player.hasPermission("worldguard.region.bypass")) {
            return true;
        }
        try {
            return WorldGuardFlags.test(player, location, flagName);
        } catch (Throwable throwable) {
            return true;
        }
    }

    /** Location-only query, no bypass permission. */
    public static boolean test(Location location, String flagName) {
        if (!active() || location == null || location.getWorld() == null) {
            return true;
        }
        try {
            return WorldGuardFlags.test(location, flagName);
        } catch (Throwable throwable) {
            return true;
        }
    }
}
