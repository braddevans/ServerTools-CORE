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

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.S07PacketRespawn;
import net.minecraft.network.play.server.S1DPacketEntityEffect;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.MathHelper;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;

import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.Objects;

import javax.annotation.Nullable;

/**
 * A variety of utility functions for interacting with player entities
 */
public final class PlayerUtils {

    /**
     * Teleport a player to a target location
     *
     * @param player   The player to teleport
     * @param location The target location
     */
    public static void teleportPlayer(final EntityPlayerMP player, final Location location) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(location, "location");

        if (player.dimension != location.getDim()) {
            transferToDimension(player, location.getDim());
        }

        player.setPositionAndUpdate(location.getX(), location.getY(), location.getZ());
    }

    /**
     * Transfer a player to another dimension
     *
     * @param player The player to transfer
     * @param dimId  The target dimension Id
     */
    public static void transferToDimension(final EntityPlayerMP player, final int dimId) {
        Objects.requireNonNull(player, "player");

        final ServerConfigurationManager configurationManager = player.mcServer.getConfigurationManager();
        final WorldServer oldWorld = player.getServerForPlayer();
        final int oldDimId = oldWorld.provider.getDimensionId();
        @Nullable WorldServer newWorld = DimensionManager.getWorld(dimId);
        if (newWorld == null) {
            DimensionManager.initDimension(dimId);
            newWorld = DimensionManager.getWorld(dimId);
            if (newWorld == null) {
                throw new IllegalArgumentException("Dimension " + dimId + " could not be found or loaded");
            }
        }

        player.dimension = dimId;
        player.playerNetServerHandler.sendPacket(new S07PacketRespawn(player.dimension, newWorld.getDifficulty(), newWorld.getWorldInfo().getTerrainType(), player.theItemInWorldManager.getGameType()));
        oldWorld.removePlayerEntityDangerously(player);
        player.isDead = false;
        WorldProvider oldProvider = oldWorld.provider;
        WorldProvider newProvider = newWorld.provider;
        double moveFactor = oldProvider.getMovementFactor() / newProvider.getMovementFactor();
        double x = player.posX * moveFactor;
        double z = player.posZ * moveFactor;

        oldWorld.theProfiler.startSection("placing");

        x = (double) MathHelper.clamp_int((int) x, -29999872, 29999872);
        z = (double) MathHelper.clamp_int((int) z, -29999872, 29999872);

        if (player.isEntityAlive()) {
            player.setLocationAndAngles(x, player.posY, z, player.rotationYaw, player.rotationPitch);
            newWorld.spawnEntityInWorld(player);
            newWorld.updateEntityWithOptionalForce(player, false);
        }

        oldWorld.theProfiler.endSection();
        player.setWorld(newWorld);
        configurationManager.preparePlayer(player, oldWorld);
        player.playerNetServerHandler.setPlayerLocation(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);
        player.theItemInWorldManager.setWorld(newWorld);
        configurationManager.updateTimeAndWeatherForPlayer(player, newWorld);
        configurationManager.syncPlayerInventory(player);

        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.playerNetServerHandler.sendPacket(new S1DPacketEntityEffect(player.getEntityId(), effect));
        }

        FMLCommonHandler.instance().firePlayerChangedDimensionEvent(player, oldDimId, dimId);
    }

    private PlayerUtils() {}
}
