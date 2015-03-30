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

import info.servertools.core.command.ServerToolsCommand;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;

import java.util.List;

import javax.annotation.Nullable;

public class CommandDisarm extends ServerToolsCommand {

    public CommandDisarm(String defaultName) {
        super(defaultName);
        setRequiredLevel(OP);
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/" + name + " {username}";
    }

    @Nullable
    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return args.length >= 1 ? getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames()) : null;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return index == 0;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {

        EntityPlayerMP player;
        if (args.length < 1) {
            player = getCommandSenderAsPlayer(sender);
        } else {
            player = getPlayer(sender, args[0]);
        }

        player.inventory.dropAllItems();
        notifyOperators(sender, this, "Disarming %s", player.getCommandSenderName());
    }
}
