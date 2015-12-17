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
package info.servertools.core.command;

import info.servertools.core.feature.Features;
import info.servertools.core.ServerToolsCore;
import info.servertools.core.feature.TeleportHandler;
import info.servertools.core.util.Location;
import info.servertools.core.util.PlayerUtils;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.BlockPos;

import java.util.List;

import javax.annotation.Nullable;

@Command(
        name = "teleport",
        requiredPermissionLevel = STCommand.PERMISSION_EVERYONE,
        requiredFeatures = { TeleportHandler.class }
)
public class CommandTeleport extends STCommand {

    private final TeleportHandler teleportHandler;

    public CommandTeleport() {
        this.teleportHandler = Features.getService(TeleportHandler.class).get();
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
        if (args.length != 1) throw new WrongUsageException(getCommandUsage(sender));
        final EntityPlayerMP player = requirePlayer(sender);
        final Location teleport = teleportHandler.getTeleport(args[0]).orElseThrow(() -> new CommandException("That teleport doesn't exist"));
        if (!ServerToolsCore.instance().getConfig().getTeleport().isCrossDimTeleportEnabled()
                && teleport.getDim() != player.worldObj.provider.getDimensionId()) {
            throw new CommandException("Teleporting to a different dimension is disabled");
        }

        PlayerUtils.teleportPlayer(player, teleport);
    }
}
