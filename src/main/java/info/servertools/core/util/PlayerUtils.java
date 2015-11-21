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
