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
package info.servertools.core.command;

import info.servertools.core.feature.TeleportHandler;
import info.servertools.core.util.Location;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.BlockPos;

import java.util.List;

import javax.annotation.Nullable;

public class CommandEditTeleport extends STCommand {

    private final TeleportHandler teleportHandler;

    public CommandEditTeleport(final TeleportHandler teleportHandler) {
        super("editteleport");
        this.teleportHandler = teleportHandler;
        setPermissionLevel(PERMISSION_OPERATOR);
    }

    @Override
    public String getCommandUsage(final ICommandSender sender) {
        return "/" + getCommandName() + " <set|delete> <name> [[dim] [x] [y] [z]]";
    }

    @Nullable
    @Override
    public List<String> addTabCompletionOptions(final ICommandSender sender, final String[] args, final BlockPos pos) {
        switch (args.length) {
            case 0:
            case 1:
                return getListOfStringsMatchingLastWord(args, "set", "delete");
            case 2:
                return "delete".equals(args[0]) ? getListOfStringsMatchingLastWord(args, teleportHandler.getTeleportNames()) : null;
            default:
                return null;
        }
    }

    @Override
    public void processCommand(final ICommandSender sender, final String[] args) throws CommandException {
        switch (args.length) {
            case 2:
                if ("delete".equals(args[0])) {
                    final TeleportHandler.EditTeleportResult result = teleportHandler.deleteTeleport(args[1]);
                    sender.addChatMessage(result.toChat());
                } else if ("set".equals(args[0])) {
                    final EntityPlayerMP player = requirePlayer(sender, "Error: provide coordinates or execute as a player");
                    final Location teleport = new Location(player);
                    final TeleportHandler.EditTeleportResult result = teleportHandler.setTeleport(args[1], teleport);
                    player.addChatMessage(result.toChat());
                } else {
                    throw new WrongUsageException(getCommandUsage(sender));
                }
                break;
            case 5:
                if ("set".equals(args[0])) {
                    final Location teleport = new Location(
                            parseInt(args[2]), // Dim
                            parseDouble(args[3]), // X
                            parseDouble(args[4]), // Y
                            parseDouble(args[5])); // Z
                    final TeleportHandler.EditTeleportResult result = teleportHandler.setTeleport(args[1], teleport);
                    sender.addChatMessage(result.toChat());
                } else {
                    throw new WrongUsageException(getCommandUsage(sender));
                }
                break;
            default:
                throw new WrongUsageException(getCommandUsage(sender));
        }
    }
}
