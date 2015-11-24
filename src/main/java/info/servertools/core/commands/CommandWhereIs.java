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
package info.servertools.core.commands;

import info.servertools.core.STCommand;
import info.servertools.core.util.ServerUtils;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;

import javax.annotation.Nullable;
import java.util.List;

public class CommandWhereIs extends STCommand {

    public CommandWhereIs(final String defaultName) {
        super(defaultName);
        setPermissionLevel(PERMISSION_EVERYONE);
    }

    @Override
    public String getCommandUsage(final ICommandSender sender) {
        return "/" + getCommandName() + " <player>";
    }

    @Nullable
    @Override
    public List<String> addTabCompletionOptions(final ICommandSender sender, final String[] args, final BlockPos pos) {
        if (args.length <= 1) {
            return getListOfStringsMatchingLastWord(args, ServerUtils.getAllUsernames());
        } else {
            return null;
        }
    }

    @Override
    public void processCommand(final ICommandSender sender, final String[] args) throws CommandException {
        if (args.length == 1) {
            final EntityPlayerMP player = getPlayer(sender, args[0]);
            sender.addChatMessage(new ChatComponentText(
                    player.getGameProfile().getName() +
                            " is at X: " + player.posX +
                            " Y: " + player.posY +
                            " Z: " + player.posZ +
                            " in Dim: " + player.worldObj.provider.getDimensionId() + " (" + player.worldObj.provider.getDimensionName() + ')'
            ));

        } else {
            throw new WrongUsageException(getCommandUsage(sender));
        }
    }
}
