package github.nighter.smartspawner.fork;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Essentials AFK lookup via reflection.
 * <p>
 * Reflection keeps EssentialsX out of the build script, so no upstream
 * gradle file has to change and the fork still compiles without it.
 */
final class AfkService {

    private static Plugin essentials;
    private static Method getUser;
    private static Method isAfk;
    private static boolean resolved;
    private static boolean usable;

    private AfkService() {
    }

    static boolean isAfk(Player player) {
        if (player == null || !resolve()) {
            return false;
        }
        try {
            Object user = getUser.invoke(essentials, player.getUniqueId());
            return user != null && (boolean) isAfk.invoke(user);
        } catch (ReflectiveOperationException | ClassCastException exception) {
            return false;
        }
    }

    private static boolean resolve() {
        if (resolved) {
            return usable;
        }
        resolved = true;
        try {
            Plugin plugin = Bukkit.getPluginManager().getPlugin("Essentials");
            if (plugin == null || !plugin.isEnabled()) {
                return false;
            }
            Method userMethod = plugin.getClass().getMethod("getUser", UUID.class);
            Class<?> userType = userMethod.getReturnType();
            Method afkMethod = userType.getMethod("isAfk");

            essentials = plugin;
            getUser = userMethod;
            isAfk = afkMethod;
            usable = true;
        } catch (ReflectiveOperationException exception) {
            usable = false;
        }
        return usable;
    }

    /** Forces a re-resolve, e.g. after Essentials was reloaded. */
    static void invalidate() {
        resolved = false;
        usable = false;
        essentials = null;
        getUser = null;
        isAfk = null;
    }
}
