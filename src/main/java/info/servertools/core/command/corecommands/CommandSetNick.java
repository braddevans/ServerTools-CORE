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

import info.servertools.core.ServerTools;
import info.servertools.core.command.CommandLevel;
import info.servertools.core.command.ServerToolsCommand;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;

import java.util.List;

import javax.annotation.Nullable;

public class CommandSetNick extends ServerToolsCommand {

    public CommandSetNick(String defaultName) {
        super(defaultName);
        setRequiredLevel(OP);
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/" + name + " [username] {nickname}";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {

        if (args.length == 1) {
            EntityPlayer player = getPlayer(sender, args[0]);
            ServerTools.instance.nickHandler.setNick(player, player.getGameProfile().getName());
            notifyOperators(sender, this, "Removed %s's nickname", player.getCommandSenderName());
        } else if (args.length == 2) {
            EntityPlayer player = getPlayer(sender, args[0]);
            ServerTools.instance.nickHandler.setNick(player, args[1]);
            notifyOperators(sender, this, "Set %s's nickname to %s", player.getCommandSenderName(), args[1]);
        } else {
            throw new WrongUsageException(getCommandUsage(sender));
        }
    }

    @Nullable
    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
        }
        return null;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return index == 0;
    }
}
