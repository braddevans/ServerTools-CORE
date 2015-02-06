/*
 * Copyright 2014 ServerTools
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

import info.servertools.core.ServerTools;
import info.servertools.core.chat.VoiceHandler;
import info.servertools.core.command.CommandLevel;
import info.servertools.core.command.ServerToolsCommand;
import info.servertools.core.util.ServerUtils;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

public class CommandSilence extends ServerToolsCommand {

    public CommandSilence(String defaultName) {
        super(defaultName);
    }

    @Override
    public CommandLevel getCommandLevel() {
        return CommandLevel.OP;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int par2) {
        return par2 == 1;
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return String.format("/%s [add|remove] [username]" + " OR " + "/%s reload", name, name);
    }

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {

        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "add", "remove", "reload");
        } else if (args.length == 2 && !"reload".equals(args[0])) {
            return getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
        }
        return null;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        final VoiceHandler voiceHandler = ServerTools.instance.voiceHandler;

        if (args.length >= 1) {
            switch (args[0]) {
                case "add":
                    if (args.length == 2) {
                        @Nullable final UUID uuid = ServerUtils.getUUIDForUsername(args[1]);
                        if (uuid == null) {
                            throw new PlayerNotFoundException();
                        } else {
                            if (voiceHandler.addSilence(uuid)) {
                                notifyOperators(sender, this, "Gave silence to %s", args[1]);
                            } else {
                                throw new CommandException("That player was already silenced");
                            }
                        }
                    } else {
                        throw new WrongUsageException(getCommandUsage(sender));
                    }
                    break;
                case "remove":
                    if (args.length == 2) {
                        @Nullable final UUID uuid = ServerUtils.getUUIDForUsername(args[1]);
                        if (uuid == null) {
                            throw new PlayerNotFoundException();
                        } else {
                            if (voiceHandler.removeSilence(uuid)) {
                                notifyOperators(sender, this, "Removed silence from %s", args[1]);
                            } else {
                                throw new CommandException("That player wasn't silenced");
                            }
                        }
                    } else {
                        throw new WrongUsageException(getCommandUsage(sender));
                    }
                    break;
                case "reload":
                    voiceHandler.loadSilenceList();
                    notifyOperators(sender, this, "Reloaded silenced players");
                    break;
                default:
                    throw new WrongUsageException(getCommandUsage(sender));
            }
        } else {
            throw new WrongUsageException(getCommandUsage(sender));
        }
    }
}
