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
import static net.minecraft.util.EnumChatFormatting.GOLD;
import static net.minecraft.util.EnumChatFormatting.GRAY;
import static net.minecraft.util.EnumChatFormatting.RESET;

import info.servertools.core.command.CommandLevel;
import info.servertools.core.command.ServerToolsCommand;
import info.servertools.core.util.ChatMessage;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import javax.annotation.Nullable;

public class CommandWhereIs extends ServerToolsCommand {

    public CommandWhereIs(String defaultName) {
        super(defaultName);
    }

    @Override
    public CommandLevel getCommandLevel() {
        return CommandLevel.ANYONE;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return index == 0;
    }

    @Nullable
    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return args.length >= 1 ? getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames()) : null;
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/" + name + " [username]";
    }

    @Override
    public void processCommand(ICommandSender icommandsender, String[] args) throws CommandException {

        if (args.length == 1) {
            EntityPlayerMP player = getPlayer(icommandsender, args[0]);
            NumberFormat f = new DecimalFormat("#");
            final String xPos = f.format(player.posX);
            final String yPos = f.format(player.posY);
            final String zPos = f.format(player.posZ);
            final String dim = f.format(player.worldObj.provider.getDimensionId());
            final String dimName = player.worldObj.provider.getDimensionName();

            icommandsender.addChatMessage(ChatMessage.builder()
                                                  .color(GOLD).add(player.getName() + " ").color(RESET).add("is at")
                                                  .add(" X:").color(AQUA).add(xPos).color(RESET)
                                                  .add(" Y:").color(AQUA).add(yPos).color(RESET)
                                                  .add(" Z:").color(AQUA).add(zPos).color(RESET)
                                                  .add(" In DIM:").color(AQUA).add(dim).color(RESET)
                                                  .add(" ").color(GRAY).add('(' + dimName + ')')
                                                  .build());
        } else {
            throw new WrongUsageException(getCommandUsage(icommandsender));
        }
    }
}
