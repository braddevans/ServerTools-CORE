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
import static info.servertools.core.command.CommandLevel.ANYONE;
import static info.servertools.core.command.CommandLevel.OP;

import info.servertools.core.command.corecommands.CommandBack;
import info.servertools.core.command.corecommands.CommandDisarm;
import info.servertools.core.command.corecommands.CommandEditTeleport;
import info.servertools.core.command.corecommands.CommandEntityCount;
import info.servertools.core.command.corecommands.CommandHeal;
import info.servertools.core.command.corecommands.CommandHome;
import info.servertools.core.command.corecommands.CommandInventory;
import info.servertools.core.command.corecommands.CommandKillAll;
import info.servertools.core.command.corecommands.CommandKillPlayer;
import info.servertools.core.command.corecommands.CommandMemory;
import info.servertools.core.command.corecommands.CommandMotd;
import info.servertools.core.command.corecommands.CommandNick;
import info.servertools.core.command.corecommands.CommandPing;
import info.servertools.core.command.corecommands.CommandReloadMotd;
import info.servertools.core.command.corecommands.CommandRemoveAll;
import info.servertools.core.command.corecommands.CommandSetNick;
import info.servertools.core.command.corecommands.CommandSilence;
import info.servertools.core.command.corecommands.CommandSpawnMob;
import info.servertools.core.command.corecommands.CommandTPS;
import info.servertools.core.command.corecommands.CommandTeleport;
import info.servertools.core.command.corecommands.CommandTop;
import info.servertools.core.command.corecommands.CommandVanish;
import info.servertools.core.command.corecommands.CommandVoice;
import info.servertools.core.command.corecommands.CommandWhereIs;
import info.servertools.core.command.corecommands.CommandWhois;
import info.servertools.core.config.CommandConfigHandler;
import info.servertools.core.config.CoreConfig;
import info.servertools.core.lib.Environment;

import net.minecraft.command.CommandHandler;
import net.minecraft.command.CommandHelp;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

public class CommandManager {

    private static final Logger log = LogManager.getLogger(CommandManager.class);

    private final CommandConfigHandler commandConfigs = new CommandConfigHandler(Environment.getServerToolsConfigDir().resolve("commands.json"));

    private final Queue<ServerToolsCommand> commandsToLoad = new ArrayDeque<>();

    public CommandManager(final Path configFile) {
        checkNotNull(configFile, "configFile");
        checkArgument(!Files.exists(configFile) || Files.isRegularFile(configFile), "A directory exists with the same name as the command config file: " + configFile);
        commandConfigs.load();
    }

    /**
     * Register a command with ServerTools
     *
     * @param command A command that extends ServerToolsCommand
     */
    public void registerCommand(final ServerToolsCommand command) {
        final CommandConfigHandler.ConfigEntry configEntry = commandConfigs.getEntry(command);

        command.name = configEntry.getCommandName();
        command.setRequiredLevel(configEntry.isRequireOP() ? OP : ANYONE);
        if (configEntry.isEnableCommand()) {
            commandsToLoad.add(command);
        }
    }

    public void registerCommands() {
        final CommandHandler commandHandler = (CommandHandler) MinecraftServer.getServer().getCommandManager();

        while (!commandsToLoad.isEmpty()) {
            commandHandler.registerCommand(commandsToLoad.poll());
        }

        if (CoreConfig.ENABLE_HELP_OVERRIDE) {
            commandHandler.registerCommand(new CommandHelp() {
                @SuppressWarnings("unchecked")
                @Override
                protected List getSortedPossibleCommands(ICommandSender sender) {
                    List<ICommand> list = MinecraftServer.getServer().getCommandManager().getPossibleCommands(sender);
                    final Iterator<ICommand> iterator = list.iterator();
                    while (iterator.hasNext()) {
                        final ICommand next = iterator.next();
                        if (next.getCommandName() == null) {
                            log.warn("Detected command with null name: {}, excluding from /help", next.getClass().getName());
                            iterator.remove();
                        } else if (next.getCommandUsage(MinecraftServer.getServer()) == null) {
                            log.warn("Detected command with null usage: {}, excluding from /help", next.getClass().getName());
                            iterator.remove();
                        }
                    }
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

        commandConfigs.save();
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
        registerCommand(new CommandEditTeleport("editteleport"));
        registerCommand(new CommandTeleport("teleport"));
        registerCommand(new CommandWhois("whois"));
        registerCommand(new CommandHome("home"));
        registerCommand(new CommandVanish("vanish"));
        registerCommand(new CommandBack("back"));
        registerCommand(new CommandTop("top"));
    }
}
