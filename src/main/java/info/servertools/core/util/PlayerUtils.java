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

import net.minecraft.entity.Entity;
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

public final class PlayerUtils {

    public static void transferEntityToWorld(final Entity entity, final WorldServer oldWorld, final WorldServer newWorld) {
        WorldProvider oldProvider = oldWorld.provider;
        WorldProvider newProvider = newWorld.provider;
        double moveFactor = oldProvider.getMovementFactor() / newProvider.getMovementFactor();
        double x = entity.posX * moveFactor;
        double z = entity.posZ * moveFactor;

        oldWorld.theProfiler.startSection("placing");

        x = (double) MathHelper.clamp_int((int) x, -29999872, 29999872);
        z = (double) MathHelper.clamp_int((int) z, -29999872, 29999872);

        if (entity.isEntityAlive()) {
            entity.setLocationAndAngles(x, entity.posY, z, entity.rotationYaw, entity.rotationPitch);
            newWorld.spawnEntityInWorld(entity);
            newWorld.updateEntityWithOptionalForce(entity, false);
        }

        oldWorld.theProfiler.endSection();
        entity.setWorld(newWorld);
    }

    public static void teleportPlayer(final EntityPlayerMP player, final Location location) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(location, "location");

        final ServerConfigurationManager configurationManager = player.mcServer.getConfigurationManager();
        final WorldServer oldWorld = DimensionManager.getWorld(player.dimension);
        final WorldServer newWorld = DimensionManager.getWorld(location.getDim());
        final int oldDimId = oldWorld.provider.getDimensionId();
        final int newDimId = newWorld.provider.getDimensionId();

        if (player.dimension != location.getDim()) {
            player.dimension = newDimId;
            player.playerNetServerHandler.sendPacket(new S07PacketRespawn(player.dimension, newWorld.getDifficulty(), newWorld.getWorldInfo().getTerrainType(), player.theItemInWorldManager.getGameType()));
            oldWorld.removePlayerEntityDangerously(player);
            player.isDead = false;
            transferEntityToWorld(player, oldWorld, newWorld);
            configurationManager.preparePlayer(player, oldWorld);
            player.playerNetServerHandler.setPlayerLocation(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);
            player.theItemInWorldManager.setWorld(newWorld);
            configurationManager.updateTimeAndWeatherForPlayer(player, newWorld);
            configurationManager.syncPlayerInventory(player);

            for (PotionEffect effect : player.getActivePotionEffects()) {
                player.playerNetServerHandler.sendPacket(new S1DPacketEntityEffect(player.getEntityId(), effect));
            }
            FMLCommonHandler.instance().firePlayerChangedDimensionEvent(player, oldDimId, newDimId);
        }

        player.setPositionAndUpdate(location.getX(), location.getY(), location.getZ());
    }

    private PlayerUtils() {}
}
