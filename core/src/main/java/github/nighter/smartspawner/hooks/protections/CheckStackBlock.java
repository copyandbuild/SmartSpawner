package github.nighter.smartspawner.hooks.protections;

import github.nighter.smartspawner.SmartSpawner;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import github.nighter.smartspawner.fork.ForkGuard;

public class CheckStackBlock {
    public static boolean CanPlayerPlaceBlock(@NotNull final Player player, @NotNull Location location) {
        if (player.isOp() || player.hasPermission("*")) return true;
        // [fork]
        if (!ForkGuard.canPlace(player, location)) return false;

        for (ProtectionHook hook : SmartSpawner.getInstance().getIntegrationManager().getProtectionHooks()) {
            if (!hook.canStack(player, location)) return false;
        }
        return true;
    }
}
