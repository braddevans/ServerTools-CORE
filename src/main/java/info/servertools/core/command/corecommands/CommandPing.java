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
import static net.minecraft.util.EnumChatFormatting.RESET;

import info.servertools.core.command.CommandLevel;
import info.servertools.core.command.ServerToolsCommand;
import info.servertools.core.util.ChatMessage;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;

public class CommandPing extends ServerToolsCommand {

    public CommandPing(String defaultName) {
        super(defaultName);
    }

    @Override
    public String getCommandUsage(ICommandSender var1) {

        return "/" + name;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {

        if (!(sender instanceof EntityPlayerMP)) { throw new WrongUsageException("Must be used by a player"); }

        final EntityPlayerMP player = (EntityPlayerMP) sender;
        sender.addChatMessage(ChatMessage.builder().add("Your ping to the server is ").color(AQUA).add(String.format("%d", player.ping)).color(RESET).add(" ms").build());
    }
}
