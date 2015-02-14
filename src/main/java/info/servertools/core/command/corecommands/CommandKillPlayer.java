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

import info.servertools.core.command.CommandLevel;
import info.servertools.core.command.ServerToolsCommand;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

public class CommandKillPlayer extends ServerToolsCommand {

    public CommandKillPlayer(String defaultName) {
        super(defaultName);
    }

    @Override
    public CommandLevel getCommandLevel() {
        return CommandLevel.OP;
    }

    @Override
    public List getAliases() {
        return Collections.singletonList("kp");
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
    public String getCommandUsage(ICommandSender icommandsender) {
        return "/" + name + " [username]";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {

        if (args.length > 0) {
            getPlayer(sender, args[0]).attackEntityFrom(DamageSource.magic, Integer.MAX_VALUE);
            notifyOperators(sender, this, "Killing: " + args[0]);
        } else {
            throw new WrongUsageException(getCommandUsage(sender));
        }
    }
}
