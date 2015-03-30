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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;

import java.util.List;

import javax.annotation.Nullable;

public class CommandHeal extends ServerToolsCommand {

    public CommandHeal(String defaultName) {
        super(defaultName);
        setRequiredLevel(OP);
    }

    @Nullable
    @Override
    public List addTabCompletionOptions(ICommandSender par1ICommandSender, String[] par2ArrayOfStr, BlockPos pos) {
        return par2ArrayOfStr.length >= 1 ? getListOfStringsMatchingLastWord(par2ArrayOfStr, MinecraftServer.getServer().getAllUsernames()) : null;
    }

    @Override
    public boolean isUsernameIndex(String[] par1ArrayOfStr, int par2) {

        return par2 == 0;
    }

    @Override
    public String getCommandUsage(ICommandSender icommandsender) {

        return "/" + name + " {username}";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] astring) throws CommandException {

        EntityPlayer player;

        if (astring.length < 1) {
            player = getCommandSenderAsPlayer(sender);
            player.heal(Integer.MAX_VALUE);
            player.getFoodStats().addStats(20, 5F);
            player.clearActivePotions();
        } else {
            player = getPlayer(sender, astring[0]);
            player.heal(Integer.MAX_VALUE);
            player.getFoodStats().addStats(20, 5F);
            player.clearActivePotions();
        }
    }
}
