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

import static net.minecraft.util.EnumChatFormatting.GREEN;
import static net.minecraft.util.EnumChatFormatting.RED;

import info.servertools.core.ServerTools;
import info.servertools.core.command.ServerToolsCommand;
import info.servertools.core.util.ChatMessage;
import info.servertools.core.util.Location;
import info.servertools.core.util.ServerUtils;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;

import javax.annotation.Nullable;

public class CommandHome extends ServerToolsCommand {

    public CommandHome(String defaultName) {
        super(defaultName);
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/" + name + " OR /" + name + " <set|clear>";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        final EntityPlayerMP player = requirePlayer(sender);
        switch (args.length) {
            case 0:
                @Nullable final Location home = ServerTools.instance.homeHandler.getHome(player.getPersistentID(), player.dimension);
                if (home != null) {
                    ServerUtils.teleportPlayer(player, home);
                    sender.addChatMessage(ChatMessage.builder().color(GREEN).add("Teleported home").build());
                } else {
                    sender.addChatMessage(ChatMessage.builder().color(RED).add("You don't have a home in this world").build());
                }
                break;
            case 1:
                switch (args[0].toLowerCase()) {
                    case "set":
                        if (ServerTools.instance.homeHandler.setHome(player.getPersistentID(), player.dimension, new Location(player))) {
                            sender.addChatMessage(ChatMessage.builder().add("Replaced existing home").build());
                        } else {
                            sender.addChatMessage(ChatMessage.builder().add("Set new home").build());
                        }
                        break;
                    case "clear":
                        if (ServerTools.instance.homeHandler.setHome(player.getPersistentID(), player.dimension, null)) {
                            sender.addChatMessage(ChatMessage.builder().add("Removed home").build());
                        } else {
                            sender.addChatMessage(ChatMessage.builder().color(RED).add("You didn't have a home in this world").build());
                        }
                        break;
                    default:
                        throw new WrongUsageException(getCommandUsage(sender));
                }
                break;
            default:
                throw new WrongUsageException(getCommandUsage(sender));
        }
    }
}
