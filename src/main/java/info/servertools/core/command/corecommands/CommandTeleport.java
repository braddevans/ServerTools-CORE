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
package info.servertools.core.command.corecommands;

import static net.minecraft.util.EnumChatFormatting.AQUA;
import static net.minecraft.util.EnumChatFormatting.GRAY;

import info.servertools.core.ServerTools;
import info.servertools.core.command.CommandLevel;
import info.servertools.core.command.ServerToolsCommand;
import info.servertools.core.config.CoreConfig;
import info.servertools.core.util.ChatMessage;
import info.servertools.core.util.Location;
import info.servertools.core.util.ServerUtils;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.BlockPos;

import java.util.List;

import javax.annotation.Nullable;

public class CommandTeleport extends ServerToolsCommand {

    public CommandTeleport(String defaultName) {
        super(defaultName);
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/" + name;
    }

    @Override
    public CommandLevel getCommandLevel() {
        return CommandLevel.ANYONE;
    }

    @Nullable
    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, ServerTools.instance.teleportHandler.getTeleports().keySet()) : null;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 1) {
            throw new WrongUsageException(getCommandUsage(sender));
        }

        requirePlayer(sender);
        final EntityPlayerMP player = (EntityPlayerMP) sender;

        @Nullable Location teleport = ServerTools.instance.teleportHandler.getTeleport(args[0]);
        if (teleport != null) {
            if (teleport.dimID != player.dimension && !CoreConfig.ENABLE_INTERDIM_TELEPORT) {
                throw new CommandException("Interdimension teleporting isn't enabled on the server");
            }
            ServerUtils.teleportPlayer(player, teleport);
            sender.addChatMessage(ChatMessage.builder().color(GRAY).add("Teleported to ").color(AQUA).add(args[0]).build());
        } else {
            throw new CommandException("That teleport doesn't exist");
        }
    }
}
