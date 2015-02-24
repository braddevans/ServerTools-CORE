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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import info.servertools.core.command.corecommands.CommandDisarm;
import info.servertools.core.command.corecommands.CommandEditTeleport;
import info.servertools.core.command.corecommands.CommandEntityCount;
import info.servertools.core.command.corecommands.CommandHeal;
import info.servertools.core.command.corecommands.CommandInventory;
import info.servertools.core.command.corecommands.CommandKillAll;
import info.servertools.core.command.corecommands.CommandKillPlayer;
import info.servertools.core.command.corecommands.CommandMemory;
import info.servertools.core.command.corecommands.CommandMotd;
import info.servertools.core.command.corecommands.CommandNick;
import info.servertools.core.command.corecommands.CommandPing;
import info.servertools.core.command.corecommands.CommandReloadConfig;
import info.servertools.core.command.corecommands.CommandReloadMotd;
import info.servertools.core.command.corecommands.CommandRemoveAll;
import info.servertools.core.command.corecommands.CommandSetNick;
import info.servertools.core.command.corecommands.CommandSilence;
import info.servertools.core.command.corecommands.CommandSpawnMob;
import info.servertools.core.command.corecommands.CommandTPS;
import info.servertools.core.command.corecommands.CommandTeleport;
import info.servertools.core.command.corecommands.CommandVoice;
import info.servertools.core.command.corecommands.CommandWhereIs;
import info.servertools.core.config.CoreConfig;

import net.minecraft.command.CommandHandler;
import net.minecraft.command.CommandHelp;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

import gnu.trove.procedure.TObjectProcedure;
import gnu.trove.set.hash.THashSet;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CommandManager {

    private static final Logger log = LogManager.getLogger(CommandManager.class);

    private final Configuration commandConfig;
    private final THashSet<ServerToolsCommand> commandsToLoad = new THashSet<>();

    public CommandManager(final File configFile) {
        checkNotNull(configFile, "configFile");
        checkArgument(!configFile.exists() || configFile.isFile(), "A directory exists with the same name as the command config file: " + configFile);

        commandConfig = new Configuration(configFile, true);
    }

    /**
     * Register a command with ServerTools
     *
     * @param command A command that extends ServerToolsCommand
     */
    public void registerCommand(final ServerToolsCommand command) {
        final boolean enableCommand = commandConfig.get(command.getClass().getName(), "enable", true).getBoolean(true);
        final String name = commandConfig.get(command.getClass().getName(), "name", command.defaultName).getString();

        log.debug("RegisterCommand Default Name: {}, Configured Name: {}, Enable?: {}", command.defaultName, name, enableCommand);

        command.name = name;
        if (enableCommand) {
            commandsToLoad.add(command);
        }

        if (commandConfig.hasChanged()) {
            commandConfig.save();
        }
    }

    public void registerCommands() {
        final CommandHandler commandHandler = (CommandHandler) MinecraftServer.getServer().getCommandManager();
        commandsToLoad.forEach(new TObjectProcedure<ServerToolsCommand>() {
            @Override
            public boolean execute(final ServerToolsCommand command) {
                commandHandler.registerCommand(command);
                return true;
            }
        });
        commandsToLoad.clear();

        if (CoreConfig.ENABLE_HELP_OVERRIDE) {
            commandHandler.registerCommand(new CommandHelp() {
                @SuppressWarnings("unchecked")
                @Override
                protected List getSortedPossibleCommands(ICommandSender sender) {
                    List<ICommand> list = MinecraftServer.getServer().getCommandManager().getPossibleCommands(sender);
                    Collections.sort(list, new Comparator<ICommand>() {
                        @Override
                        public int compare(ICommand o1, ICommand o2) {
                            return o1.getCommandName().compareTo(o2.getCommandName());
                        }
                    });
                    return list;
                }
            });
        }
    }

    public void registerCoreCommands() {
        registerCommand(new CommandMotd("motd"));
        registerCommand(new CommandVoice("voice"));
        registerCommand(new CommandSilence("silence"));
        registerCommand(new CommandDisarm("disarm"));
        registerCommand(new CommandEntityCount("entitycount"));
        registerCommand(new CommandHeal("heal"));
        registerCommand(new CommandInventory("inventory"));
        registerCommand(new CommandKillPlayer("killplayer"));
        registerCommand(new CommandKillAll("killall"));
        registerCommand(new CommandReloadMotd("reloadmotd"));
        registerCommand(new CommandSpawnMob("spawnmob"));
        registerCommand(new CommandWhereIs("whereis"));
        registerCommand(new CommandTPS("tps"));
        registerCommand(new CommandRemoveAll("removeall"));
        registerCommand(new CommandMemory("memory"));
        registerCommand(new CommandPing("ping"));
        registerCommand(new CommandNick("nick"));
        registerCommand(new CommandSetNick("setnick"));
        registerCommand(new CommandReloadConfig("reloadconfig"));
        registerCommand(new CommandEditTeleport("editteleport"));
        registerCommand(new CommandTeleport("teleport"));
    }
}
