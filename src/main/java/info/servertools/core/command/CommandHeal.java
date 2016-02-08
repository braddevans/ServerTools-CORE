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
package info.servertools.core.command;

import info.servertools.core.util.ServerUtils;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.BlockPos;

import javax.annotation.Nullable;
import java.util.List;

@Command(
        name = "heal",
        opRequired = true
)
public class CommandHeal extends STCommand {

    @Override
    public String getCommandUsage(final ICommandSender sender) {
        return "/" + getCommandName() + " <player>";
    }

    @Override
    public boolean isUsernameIndex(final String[] args, final int index) {
        return index == 0;
    }

    @Override
    @Nullable
    public List<String> addTabCompletionOptions(final ICommandSender sender, final String[] args, final BlockPos pos) {
        return args.length <= 1 ? getListOfStringsMatchingLastWord(args, ServerUtils.getAllUsernames()) : null;
    }

    @Override
    public void processCommand(final ICommandSender sender, final String[] args) throws CommandException {
        EntityPlayerMP player = args.length < 1 ? getCommandSenderAsPlayer(sender) : getPlayer(sender, args[0]);
        player.heal(Float.MAX_VALUE);
        notifyOperators(sender, this, "Healed %s", player.getName());
    }
}
