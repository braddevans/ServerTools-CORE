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

import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

import com.mojang.authlib.GameProfile;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

/**
 * Helper class with methods used for server functions and checks
 */
public final class ServerUtils {

    private ServerUtils() {
    }

    /**
     * Get all players logged into the server
     *
     * @return a list of players
     */
    @SuppressWarnings("unchecked")
    public static List<EntityPlayerMP> getAllPlayers() {
        return MinecraftServer.getServer().getConfigurationManager().playerEntityList;
    }

    /**
     * Get the player for a given {@link java.util.UUID UUID}
     *
     * @param uuid the UUID
     *
     * @return the EntityPlayer, or <code>null</code> if the player does not exist
     */
    @Nullable
    public static EntityPlayerMP getPlayerForUUID(UUID uuid) {

        checkNotNull(uuid, "uurd");

        for (EntityPlayerMP player : getAllPlayers()) {
            if (uuid.equals(player.getGameProfile().getId())) {
                return player;
            }
        }

        return null;
    }

    /**
     * Get the player for a given username
     *
     * @param username the username
     *
     * @return the EntityPlayer, or <code>null</code> if the player does not exist
     */
    @Nullable
    public static EntityPlayerMP getPlayerForUsername(String username) {

        checkNotNull(username, "username");

        for (EntityPlayerMP player : getAllPlayers()) {
            if (username.equals(player.getGameProfile().getName())) {
                return player;
            }
        }

        return null;
    }

    /**
     * Get a player's {@link UUID} given their username.
     * <p>
     * Local players will be checked first and their offline UUID will be used if necessary. <br>
     * If the player isn't logged into the server, Mojang's servers will be queried for their UUID.
     * </p>
     *
     * @param username The username
     *
     * @return The UUID, or {@code null} if a match could not be found
     */
    @Nullable
    public static UUID getUUIDForUsername(String username) {
        @Nullable final EntityPlayerMP player = MinecraftServer.getServer().getConfigurationManager().getPlayerByUsername(username);
        if (player != null) {
            return player.getPersistentID();
        } else {
            @Nullable final GameProfile profile = MinecraftServer.getServer().getPlayerProfileCache().getGameProfileForUsername(username);
            if (profile != null) {
                return profile.getId();
            } else {
                return null;
            }
        }
    }

    /**
     * Get if the current server is single player
     *
     * @return {@code true} if the server is single player, {@code false} if multi player
     */
    public static boolean isSinglePlayer() {
        return MinecraftServer.getServer().isSinglePlayer();
    }

    /**
     * Get if the current server is multi player
     *
     * @return {@code true} if the server is multi player, {@code false} if single player
     */
    public static boolean isMultiplayer() {
        return !isSinglePlayer();
    }

    /**
     * Teleport a player to a given location
     *
     * @param entityPlayer the player
     * @param location     the location
     */
    public static void teleportPlayer(EntityPlayerMP entityPlayer, Location location) {

        checkNotNull(entityPlayer, "entityPlayer");
        checkNotNull(location, "location");

        if (entityPlayer.worldObj.provider.getDimensionId() != location.dimID) { entityPlayer.travelToDimension(location.dimID); }

        entityPlayer.setPositionAndUpdate(location.x, location.y, location.z);
    }

    /**
     * Checks to see if a given player has server OP status
     *
     * @param profile the player's {@link com.mojang.authlib.GameProfile GameProfile}
     *
     * @return if the player is OP
     */
    public static boolean isOP(GameProfile profile) {
        return MinecraftServer.getServer().getConfigurationManager().getOppedPlayers().hasEntry(profile);
    }

    /**
     * Refresh a player's {@link EntityPlayer#displayname display name} given their {@link UUID}.
     *
     * @param uuid The UUID
     */
    public static void refreshPlayerDisplayName(UUID uuid) {
        @Nullable EntityPlayer player = MinecraftServer.getServer().getConfigurationManager().getPlayerByUUID(uuid);
        if (player != null) { player.refreshDisplayName(); }
    }
}
