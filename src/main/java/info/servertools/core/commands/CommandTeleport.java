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
package info.servertools.core.commands;

import info.servertools.core.STCommand;
import info.servertools.core.ServerToolsCore;
import info.servertools.core.feature.TeleportHandler;
import info.servertools.core.util.Location;
import info.servertools.core.util.PlayerUtils;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.BlockPos;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class CommandTeleport extends STCommand {

    private final TeleportHandler teleportHandler;

    public CommandTeleport(final TeleportHandler teleportHandler, final String defaultName) {
        super(defaultName);
        this.teleportHandler = teleportHandler;
        setPermissionLevel(PERMISSION_EVERYONE);
    }

    @Override
    public String getCommandUsage(final ICommandSender sender) {
        return "/" + getCommandName() + " <name>";
    }

    @Nullable
    @Override
    public List<String> addTabCompletionOptions(final ICommandSender sender, final String[] args, final BlockPos pos) {
        return getListOfStringsMatchingLastWord(args, teleportHandler.getTeleportNames());
    }

    @Override
    public void processCommand(final ICommandSender sender, final String[] args) throws CommandException {
        if (args.length == 1) {
            final EntityPlayerMP player = requirePlayer(sender);
            final Optional<Location> optTeleport = teleportHandler.getTeleport(args[0]);
            if (optTeleport.isPresent()) {
                final Location teleport = optTeleport.get();
                if (!ServerToolsCore.getConfig().getTeleport().isCrossDimTeleportEnabled()) {
                    if (teleport.getDim() != player.worldObj.provider.getDimensionId()) {
                        throw new CommandException("Teleporting to a different dimension is disabled");
                    }
                }
                PlayerUtils.teleportPlayer(player, teleport);
            } else {
                throw new CommandException("That teleport doesn't exist");
            }
        } else {
            throw new WrongUsageException(getCommandUsage(sender));
        }
    }
}
