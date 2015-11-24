/*
 * This file is a part of ServerTools <http://servertools.info>
 *
 * Copyright (c) 2014 ServerTools
 * Copyright (c) 2014 contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

    public static List<EntityPlayerMP> getAllPlayers() {
        return server.getConfigurationManager().playerEntityList;
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
                .filter(player -> username.equalsIgnoreCase(player.getGameProfile().getName()))
                .findAny();
    }

    public static Optional<GameProfile> getGameProfile(final String username) {
        Objects.requireNonNull(username, "username");
        final Optional<EntityPlayerMP> player = getPlayerForUsername(username);
        if (player.isPresent()) {
            return Optional.of(player.get().getGameProfile());
        } else {
            return Optional.ofNullable(server.getPlayerProfileCache().getGameProfileForUsername(username));
        }
    }

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

    public static boolean isEffectiveOp(final GameProfile gameProfile) {
        return server.getConfigurationManager().canSendCommands(gameProfile);

    }

    public static boolean isSinglePlayer() {
        return server.isSinglePlayer();
    }

    public static boolean isMultiPlayer() {
        return !server.isSinglePlayer();
    }

    private ServerUtils() {}
}
