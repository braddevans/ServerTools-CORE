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
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.world.Teleporter;

import java.util.Objects;

public final class PlayerUtils {

    public static void teleportPlayer(final EntityPlayerMP player, final Location location) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(location, "location");

        final ServerConfigurationManager configurationManager = player.mcServer.getConfigurationManager();
        if (player.dimension != location.getDim()) {
            configurationManager.transferPlayerToDimension(player, location.getDim(), new Teleporter(player.getServerForPlayer()) {
                @Override
                public void placeInPortal(final Entity entity, final float rotationYaw) {}

                @Override
                public boolean placeInExistingPortal(final Entity entity, final float rotationYaw) {
                    return false;
                }

                @Override
                public boolean makePortal(final Entity entity) {
                    return false;
                }

                @Override
                public void removeStalePortalLocations(final long worldTime) {}
            });
        }

        player.setPositionAndUpdate(location.getX(), location.getY(), location.getZ());
    }

    private PlayerUtils() {}
}
