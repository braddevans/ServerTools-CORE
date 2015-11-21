package info.servertools.core.util;

import net.minecraft.entity.player.EntityPlayerMP;

import java.util.Objects;

public final class PlayerUtils {

    public static void teleportPlayer(final EntityPlayerMP player, final Location location) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(location, "location");
        // TODO
        System.out.println("Teleport: " + player.getCommandSenderName() + " -> " + location);
    }

    private PlayerUtils() {}
}
