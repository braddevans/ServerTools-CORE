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

import static info.servertools.core.command.CommandLevel.OP;
import static net.minecraft.util.EnumChatFormatting.AQUA;
import static net.minecraft.util.EnumChatFormatting.RED;

import info.servertools.core.ServerTools;
import info.servertools.core.command.ServerToolsCommand;
import info.servertools.core.util.ChatMessage;
import info.servertools.core.util.Location;

import com.google.common.collect.Lists;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.BlockPos;

import java.util.List;

import javax.annotation.Nullable;

public class CommandEditTeleport extends ServerToolsCommand {

    public CommandEditTeleport(String defaultName) {
        super(defaultName);
        setRequiredLevel(OP);
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/" + name + "[set|delete] [name]";
    }

    @Nullable
    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, Lists.newArrayList("set", "delete")) :
               args.length == 2 ? getListOfStringsMatchingLastWord(args, ServerTools.instance.teleportHandler.getTeleports().keySet()) : null;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 2) {
            throw new WrongUsageException(getCommandUsage(sender));
        }

        requirePlayer(sender);
        final EntityPlayerMP player = (EntityPlayerMP) sender;

        final String teleportName = args[1];
        switch (args[0]) {
            case "set":
                if (ServerTools.instance.teleportHandler.setTeleport(teleportName, new Location(player))) {
                    sender.addChatMessage(ChatMessage.builder().add("Replaced existing teleport ").color(AQUA).add(teleportName).build());
                } else {
                    sender.addChatMessage(ChatMessage.builder().add("Set teleport ").color(AQUA).add(teleportName).build());
                }
                break;
            case "delete":
                if (ServerTools.instance.teleportHandler.removeTeleport(teleportName)) {
                    sender.addChatMessage(ChatMessage.builder().add("Removed teleport ").color(AQUA).add(teleportName).build());
                } else {
                    sender.addChatMessage(ChatMessage.builder().color(RED).add("That teleport doesn't exist").build());
                }
                break;
            default:
                throw new WrongUsageException(getCommandUsage(sender));
        }
    }
}
