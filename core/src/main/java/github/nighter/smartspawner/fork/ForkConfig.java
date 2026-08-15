package github.nighter.smartspawner.fork;

import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Fork-only configuration holder.
 * <p>
 * This class exists so that fork specific behaviour (things that will never be
 * merged upstream) lives in its own file: {@code plugins/SmartSpawner/fork.yml}.
 * Upstream files (config.yml, language files, ...) stay untouched, so
 * {@code git pull} from upstream never conflicts on them.
 * <p>
 * The instance is resolved lazily via {@link JavaPlugin#getProvidingPlugin(Class)},
 * so the main plugin class does not need to be modified either.
 */
public final class ForkConfig {

    private static final String FILE_NAME = "fork.yml";

    private static volatile ForkConfig instance;

    private final JavaPlugin plugin;

    private boolean worldFilterEnabled;
    private Set<String> allowedWorlds = Set.of();

    private ForkConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    /** Lazily created singleton. Safe to call from any listener. */
    public static ForkConfig get() {
        ForkConfig local = instance;
        if (local == null) {
            synchronized (ForkConfig.class) {
                local = instance;
                if (local == null) {
                    local = new ForkConfig(JavaPlugin.getProvidingPlugin(ForkConfig.class));
                    instance = local;
                }
            }
        }
        return local;
    }

    /** Drops the cached instance so the next {@link #get()} re-reads fork.yml. */
    public static void invalidate() {
        synchronized (ForkConfig.class) {
            instance = null;
        }
    }

    public void reload() {
        File file = new File(plugin.getDataFolder(), FILE_NAME);
        if (!file.exists()) {
            try {
                plugin.saveResource(FILE_NAME, false);
            } catch (IllegalArgumentException ex) {
                plugin.getLogger().warning("[fork] " + FILE_NAME + " missing from jar, using defaults.");
            }
        }

        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        this.worldFilterEnabled = cfg.getBoolean("natural_spawner.drop_world_filter.enabled", false);

        List<String> configured = cfg.getStringList("natural_spawner.drop_world_filter.worlds");
        Set<String> normalized = new HashSet<>(configured.size());
        for (String world : configured) {
            if (world != null && !world.isBlank()) {
                normalized.add(world.toLowerCase(Locale.ROOT));
            }
        }
        this.allowedWorlds = Set.copyOf(normalized);
    }

    /**
     * @return true if a broken natural/vanilla spawner may drop in this world.
     *         When the filter is disabled this always returns true, i.e. the
     *         plugin behaves exactly like upstream.
     */
    public boolean isVanillaDropAllowed(Player player) {
        return player != null && isVanillaDropAllowed(player.getWorld());
    }

    /** World based overload, useful when no player is involved. */
    public boolean isVanillaDropAllowed(World world) {
        if (!worldFilterEnabled) {
            return true;
        }
        if (world == null) {
            return false;
        }
        return allowedWorlds.contains(world.getName().toLowerCase(Locale.ROOT));
    }
}
