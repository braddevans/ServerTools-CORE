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

import com.mojang.authlib.GameProfile;
import info.servertools.core.STCommand;
import info.servertools.core.feature.SilenceHandler;
import info.servertools.core.util.ServerUtils;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.BlockPos;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CommandSilence extends STCommand {

    private final SilenceHandler silenceHandler;

    public CommandSilence(final SilenceHandler silenceHandler, final String defaultName) {
        super(defaultName);
        this.silenceHandler = silenceHandler;
    }

    @Override
    public String getCommandUsage(final ICommandSender sender) {
        return "/" + getCommandName() + " <add|remove> <player>";
    }

    @Nullable
    @Override
    public List<String> addTabCompletionOptions(final ICommandSender sender, final String[] args, final BlockPos pos) {
        if (args.length <= 1) {
            return getListOfStringsMatchingLastWord(args, "add", "remove");
        } else if (args.length == 2) {
            return getListOfStringsMatchingLastWord(args, ServerUtils.getAllUsernames());
        } else {
            return null;
        }
    }

    @Override
    public void processCommand(final ICommandSender sender, final String[] args) throws CommandException {
        if (args.length == 2) {
            Optional<GameProfile> gameProfile = ServerUtils.getGameProfile(args[1]);
            if (!gameProfile.isPresent()) {
                throw new PlayerNotFoundException();
            }

            final UUID uuid = gameProfile.get().getId();

            if ("add".equals(args[0])) {
                if (silenceHandler.addSilence(uuid)) {
                    notifyOperators(sender, this, "Silenced %s", args[1]);
                } else {
                    throw new CommandException("That player is already silenced");
                }
            } else if ("remove".equals(args[0])) {
                if (silenceHandler.removeSilence(uuid)) {
                    notifyOperators(sender, this, "Removed silence on %s", args[1]);
                } else {
                    throw new CommandException("That player was not silenced");
                }
            } else {
                throw new WrongUsageException(getCommandUsage(sender));
            }
        } else {
            throw new WrongUsageException(getCommandUsage(sender));
        }
    }
}
