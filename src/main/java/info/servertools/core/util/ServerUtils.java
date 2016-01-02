/*
 * This file is a part of ServerTools <http://servertools.info>
 *
 * Copyright (c) 2015 ServerTools
 * Copyright (c) 2015 contributors
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

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.MathHelper;

import net.minecraftforge.common.DimensionManager;

import com.google.common.util.concurrent.ListenableFuture;
import com.mojang.authlib.GameProfile;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

/**
 * A variety of utility functions for interacting with the Minecraft server.
 */
@SuppressWarnings("unused")
public final class ServerUtils {

    private static final MinecraftServer server = MinecraftServer.getServer();

    /**
     * Get a list of all {@linkplain EntityPlayerMP players} on the server
     *
     * @return A list of players
     */
    public static List<EntityPlayerMP> getAllPlayers() {
        return server.getConfigurationManager().playerEntityList;
    }

    /**
     * Get a list of {@linkplain UUID uuids} for every {@linkplain EntityPlayerMP player} on the server
     *
     * @return A list of uuids
     */
    public static List<UUID> getAllUUIDs() {
        return getAllPlayers().stream()
                .map(EntityPlayerMP::getPersistentID)
                .collect(Collectors.toList());
    }

    /**
     * Get a list of usernames for every {@linkplain EntityPlayerMP player} on the server
     *
     * @return A list of usernames
     */
    public static List<String> getAllUsernames() {
        return getAllPlayers().stream()
                .map(EntityPlayer::getName)
                .collect(Collectors.toList());
    }

    /**
     * Get a player by their {@link UUID}
     *
     * @param uuid The player's uuid
     *
     * @return The player, if present
     */
    public static Optional<EntityPlayerMP> getPlayerForUUID(final UUID uuid) {
        Objects.requireNonNull(uuid, "uuid");
        return getAllPlayers().stream()
                .filter(player -> uuid.equals(player.getPersistentID()))
                .findAny();
    }

    /**
     * Get a player by their username
     *
     * @param username The player's username
     *
     * @return The player, if present
     */
    public static Optional<EntityPlayerMP> getPlayerForUsername(final String username) {
        Objects.requireNonNull(username, "username");
        return getAllPlayers().stream()
                .filter(player -> username.equalsIgnoreCase(player.getName()))
                .findFirst();
    }

    /**
     * Get a player's {@link GameProfile} by their username.
     * <p>
     * If the player is not on the server, Mojang's servers will be contacted to try and find it.
     * </p>
     *
     * @param username The player's username
     *
     * @return The player's GameProfile, if found
     */
    public static Optional<GameProfile> getGameProfile(final String username) {
        Objects.requireNonNull(username, "username");
        final Optional<EntityPlayerMP> player = getPlayerForUsername(username);
        if (player.isPresent()) {
            return Optional.of(player.get().getGameProfile());
        } else {
            return Optional.ofNullable(server.getPlayerProfileCache().getGameProfileForUsername(username));
        }
    }

    /**
     * Run a task on the main server thread
     *
     * @param callable The task to run
     * @param <T>      The return type of the task
     *
     * @return A {@link ListenableFuture} for the task
     */
    public static <T> ListenableFuture<T> callFromMainThread(Callable<T> callable) {
        return server.callFromMainThread(callable);
    }

    /**
     * Run a task on the main server thread
     *
     * @param runnable The task to run
     *
     * @return A {@link ListenableFuture} for the task
     */
    public static ListenableFuture<?> callFromMainThread(Runnable runnable) {
        return callFromMainThread(Executors.callable(runnable));
    }

    /**
     * Get if a player is an op on the server. This does not check if the player is the single player owner,
     * for that use {@link #isSinglePlayerOwner(String)}. This only checks the entries that are found in the server's {@literal ops.json} file.
     *
     * @param gameProfile The player's GameProfile
     *
     * @return {@code true} if the player is an op
     */
    public static boolean isOp(final GameProfile gameProfile) {
        Objects.requireNonNull(gameProfile, "gameProfile");
        return server.getConfigurationManager().getOppedPlayers().getEntry(gameProfile) != null;
    }

    /**
     * Get if a player is the owner of the single player server. This will <em>never</em> return {@code true} on a dedicated server.
     *
     * @param username The player's username
     *
     * @return {@code true} if the player is the owner
     */
    public static boolean isSinglePlayerOwner(final String username) {
        Objects.requireNonNull(username, "username");
        return username.equals(server.getServerOwner());
    }

    /**
     * Get if a player is an effective op on the server. An effective op means that they are allowed to use features categorized as {@literal "cheats"}.
     *
     * @param gameProfile The player's GameProfile
     *
     * @return {@code true} if the player is an effective op
     */
    public static boolean isEffectiveOp(final GameProfile gameProfile) {
        return server.getConfigurationManager().canSendCommands(gameProfile);
    }

    /**
     * Get if the current server is a single-player server
     *
     * @return {@code true} if single-player
     */
    public static boolean isSinglePlayer() {
        return server.isSinglePlayer();
    }

    /**
     * Get if the current server is a multi-player server
     *
     * @return {@code true} if multi-player
     */
    public static boolean isMultiPlayer() {
        return !server.isSinglePlayer();
    }

    public static double getWorldTickTime(final int dimId) {
        @Nullable long[] times = server.worldTickTimes.get(dimId);
        if (times == null) return -1;
        return MathHelper.average(times) * 1.0E-6D;
    }

    public static double getMeanTickTime() {
        return MathHelper.average(server.tickTimeArray) * 1.0E-6D;
    }

    public static double getWorldTPS(final int dimId) {
        final double worldTickTime = getWorldTickTime(dimId);
        if (worldTickTime == -1L) return -1;
        return Math.min(1000.0 / worldTickTime, 20);
    }

    public static double getMeanTPS() {
        return Math.min(1000.0 / getMeanTickTime(), 20);
    }

    private ServerUtils() {}
}
