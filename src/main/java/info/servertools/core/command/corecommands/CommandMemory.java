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
import net.minecraft.command.ICommandSender;

public class CommandMemory extends ServerToolsCommand {

    public CommandMemory(String defaultName) {
        super(defaultName);
    }

    @Override
    public CommandLevel getCommandLevel() {

        return CommandLevel.OP;
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {

        return "/" + name;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {

        long totalMemory = Runtime.getRuntime().totalMemory() / 1048576L;
        long freeMemory = Runtime.getRuntime().freeMemory() / 1048576L;
        long usedMemory = totalMemory - freeMemory;

        sender.addChatMessage(ChatMessage.builder()
                                      .color(AQUA).add(String.format("%d", usedMemory)).color(RESET).add(" out of ").color(AQUA).add(String.format("%d", totalMemory)).color(RESET).add(" MB used")
                                      .build());
    }
}
