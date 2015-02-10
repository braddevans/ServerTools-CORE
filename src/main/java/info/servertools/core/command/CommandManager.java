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

import info.servertools.core.command.corecommands.CommandDisarm;
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
import info.servertools.core.command.corecommands.CommandVoice;
import info.servertools.core.command.corecommands.CommandWhereIs;
import info.servertools.core.config.STConfig;
import info.servertools.core.lib.Environment;

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

    private static final String ENABLE_COMMAND_CONFIG_CATEGORY = "enableCommand";
    private static final String COMMAND_NAME_CONFIG_CATEGORY = "commandName";

    private static final Configuration commandConfig = new Configuration(new File(Environment.SERVERTOOLS_DIR, "command.cfg"));

    private static final THashSet<ServerToolsCommand> commandsToLoad = new THashSet<>();

    static {
        commandConfig.load();

        commandConfig.addCustomCategoryComment(ENABLE_COMMAND_CONFIG_CATEGORY, "Allows you to disable any command registered with ServerTools");
        commandConfig.addCustomCategoryComment(COMMAND_NAME_CONFIG_CATEGORY, "Allows you to rename any command registered with ServerTools");

        registerSTCommand(new CommandMotd("motd"));
        registerSTCommand(new CommandVoice("voice"));
        registerSTCommand(new CommandSilence("silence"));
        registerSTCommand(new CommandDisarm("disarm"));
        registerSTCommand(new CommandEntityCount("entitycount"));
        registerSTCommand(new CommandHeal("heal"));
        registerSTCommand(new CommandInventory("inventory"));
        registerSTCommand(new CommandKillPlayer("killplayer"));
        registerSTCommand(new CommandKillAll("killall"));
        registerSTCommand(new CommandReloadMotd("reloadmotd"));
        registerSTCommand(new CommandSpawnMob("spawnmob"));
        registerSTCommand(new CommandWhereIs("whereis"));
        registerSTCommand(new CommandTPS("tps"));
        registerSTCommand(new CommandRemoveAll("removeall"));
        registerSTCommand(new CommandMemory("memory"));
        registerSTCommand(new CommandPing("ping"));
        registerSTCommand(new CommandNick("nick"));
        registerSTCommand(new CommandSetNick("setnick"));
        registerSTCommand(new CommandReloadConfig("reloadconfig"));

        if (commandConfig.hasChanged()) {
            commandConfig.save();
        }
    }

    /**
     * Registers a command with ServerTools
     *
     * @param command A command that extends ServerToolsCommand
     */
    public static void registerSTCommand(ServerToolsCommand command) {
        boolean enableCommand = commandConfig.get("enableCommand", command.getClass().getName(), true).getBoolean(true);
        command.name = commandConfig.get("commandName", command.getClass().getName(), command.defaultName).getString();

        if (enableCommand) {
            commandsToLoad.add(command);
        }

        if (commandConfig.hasChanged()) {
            commandConfig.save();
        }
    }

    public static void registerCommands(final CommandHandler commandHandler) {

        commandsToLoad.forEach(new TObjectProcedure<ServerToolsCommand>() {
            @Override
            public boolean execute(final ServerToolsCommand command) {
                log.trace("Registering Command: {} , with name: {}", command.getClass(), command.name);
                commandHandler.registerCommand(command);
                return true;
            }
        });
        commandsToLoad.clear();

        if (STConfig.settings().ENABLE_HELP_OVERRIDE) {
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
}
