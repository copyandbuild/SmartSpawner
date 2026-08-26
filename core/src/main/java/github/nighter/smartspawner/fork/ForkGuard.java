package github.nighter.smartspawner.fork;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * Single entry point the upstream classes call into.
 * <p>
 * Every upstream touch is one line calling a method here, so an upstream
 * {@code git pull} can only ever conflict on that single line.
 */
public final class ForkGuard {

    private ForkGuard() {
    }

    // --- WorldGuard region flags -------------------------------------------

    public static boolean canUse(Player player, Location location) {
        return ForkFlags.test(player, location, ForkFlags.USE);
    }

    public static boolean canPlace(Player player, Location location) {
        return ForkFlags.test(player, location, ForkFlags.PLACE);
    }

    public static boolean canBreak(Player player, Location location) {
        return ForkFlags.test(player, location, ForkFlags.BREAK);
    }

    public static boolean canStack(Player player, Location location) {
        return ForkFlags.test(player, location, ForkFlags.STACK);
    }

    public static boolean canSell(Player player, Location location) {
        if (ForkConfig.get().isBlockSellWhileAfk() && AfkService.isAfk(player)) {
            return false;
        }
        return ForkFlags.test(player, location, ForkFlags.SELL);
    }

    // --- Loot generation ----------------------------------------------------

    /**
     * Hard preconditions for loot generation at this spawner, independent of
     * any player in range.
     */
    public static boolean canGenerate(Location spawnerLocation) {
        ForkConfig config = ForkConfig.get();
        if (config.isRequirePlayerOnline() && Bukkit.getOnlinePlayers().isEmpty()) {
            return false;
        }
        if (config.isRequireChunkLoaded()) {
            if (spawnerLocation == null) {
                return false;
            }
            World world = spawnerLocation.getWorld();
            if (world == null || !world.isChunkLoaded(spawnerLocation.getBlockX() >> 4, spawnerLocation.getBlockZ() >> 4)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Does this player keep spawners running?
     * <p>
     * Two reasons why not:
     * <ul>
     *   <li>the player is AFK (replaces the old external NuviraMCSpawner plugin)</li>
     *   <li>the player stands in a region with {@code smartspawner-generate: deny},
     *       so parking in a spawn/AFK area does not keep spawners ticking</li>
     * </ul>
     * The region is the one the PLAYER is standing in, not the spawner's.
     */
    public static boolean countsAsActive(Player player) {
        if (player == null) {
            return false;
        }
        if (ForkConfig.get().isIgnoreAfkPlayers() && AfkService.isAfk(player)) {
            return false;
        }
        return ForkFlags.test(player.getLocation(), ForkFlags.GENERATE);
    }

    /** Called from the fork reload path. */
    public static void invalidate() {
        ForkConfig.invalidate();
        AfkService.invalidate();
    }
}
