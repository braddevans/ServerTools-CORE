package info.servertools.core.util;

import com.google.common.util.concurrent.ListenableFuture;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public final class ServerUtils {

    private static final MinecraftServer server = MinecraftServer.getServer();

    @SuppressWarnings("unchecked")
    public static List<EntityPlayerMP> getAllPlayers() {
        return (List<EntityPlayerMP>) server.getConfigurationManager().playerEntityList;
    }

    public static List<UUID> getAllUUIDs() {
        return getAllPlayers().stream()
                .map(EntityPlayerMP::getPersistentID)
                .collect(Collectors.toList());
    }

    public static List<String> getAllUsernames() {
        return getAllPlayers().stream()
                .map(player -> player.getGameProfile().getName())
                .collect(Collectors.toList());
    }

    public static Optional<EntityPlayerMP> getPlayerForUUID(final UUID uuid) {
        Objects.requireNonNull(uuid, "uuid");
        return getAllPlayers().stream()
                .filter(player -> uuid.equals(player.getPersistentID()))
                .findAny();
    }

    public static Optional<EntityPlayerMP> getPlayerForUsername(final String username) {
        Objects.requireNonNull(username, "username");
        return getAllPlayers().stream()
                .filter(player -> username.equals(player.getGameProfile().getName()))
                .findAny();
    }

    @SuppressWarnings("unchecked")
    public static <T> ListenableFuture<T> callFromMainThread(Callable<T> callable) {
        return server.callFromMainThread(callable);
    }

    public static ListenableFuture<?> callFromMainThread(Runnable runnable) {
        return callFromMainThread(Executors.callable(runnable));
    }

    public static boolean isOp(final GameProfile gameProfile) {
        Objects.requireNonNull(gameProfile, "gameProfile");
        return server.getConfigurationManager().getOppedPlayers().getEntry(gameProfile) != null;
    }

    public static boolean isSinglePlayerOwner(final String username) {
        Objects.requireNonNull(username, "username");
        return username.equals(server.getServerOwner());
    }

    public static boolean isSinglePlayer() {
        return server.isSinglePlayer();
    }

    public static boolean isMultiPlayer() {
        return !server.isSinglePlayer();
    }

    private ServerUtils() {}
}
