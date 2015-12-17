/*
 * This file is a part of ServerTools <http://servertools.info>
 *
 * Copyright (c) 2015 ServerTools
 * Copyright (c) 2015 contributors
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

import info.servertools.core.feature.Features;
import info.servertools.core.ServerToolsCore;

import net.minecraft.command.CommandHandler;
import net.minecraft.command.CommandHelp;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderException;
import net.minecraftforge.fml.common.ModClassLoader;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

/**
 * ServerTools Command Manager
 */
public final class CommandManager {

    private static final Logger log = LogManager.getLogger();

    private static final String HEADER =
            "This configuration file can be used to: disable, rename, and change required permission level for ServerTools commands. " +
                    "Command names must match the REGEX [a-zA-Z0-9_\\-]";

    private static final Pattern validCommandPattern = Pattern.compile("[a-zA-Z0-9_\\-]+");

    private final Collection<STCommand> commands = new ArrayList<>();

    private Path configFile;
    private HoconConfigurationLoader loader;
    private CommentedConfigurationNode node = SimpleCommentedConfigurationNode.root();

    public CommandManager(final Path configFile) {
        this.configFile = configFile;
        try {
            if (!Files.exists(configFile.getParent())) {
                Files.createDirectories(configFile.getParent());
            }
            if (!Files.exists(configFile)) {
                Files.createFile(configFile);
            }

            loader = HoconConfigurationLoader.builder().setFile(configFile.toFile()).build();
            node = loader.load(ConfigurationOptions.defaults().setHeader(HEADER));

        } catch (IOException e) {
            log.fatal("Failed to initialize command manager");
            throw new LoaderException(e);
        }
    }

    private void registerCommand(final STCommand command, final Command commandAnnotation) {
        log.trace("registerCommand {}", command);

        CommentedConfigurationNode commandNode = node.getNode(command.getClass().getName());
        CommentedConfigurationNode enableNode = commandNode.getNode("enable-command");
        CommentedConfigurationNode nameNode = commandNode.getNode("command-name");
        CommentedConfigurationNode permNode = commandNode.getNode("permission-level");

        if (enableNode.isVirtual() || enableNode.getValue() == null) { enableNode.setValue(true); }
        if (nameNode.isVirtual() || nameNode.getValue() == null) { nameNode.setValue(commandAnnotation.name()); }
        if (permNode.isVirtual() || permNode.getValue() == null) { permNode.setValue(commandAnnotation.requiredPermissionLevel()); }

        enableNode.setComment("Set to false to disable this command");
        nameNode.setComment("Default name: " + commandAnnotation.name());
        permNode.setComment("The required permission level for this command. 0 is everyone. 1+ requires some level of OP");

        final String name = nameNode.getString();

        if (!validCommandPattern.matcher(name).matches()) {
            throw new RuntimeException(String.format("Command %s was configured with an invald name: %s", command, name));
        }

        ReflectionHelper.setPrivateValue(STCommand.class, command, name, "name");

        if (!commandAnnotation.name().equals(name)) {
            log.info("Command {} was renamed from {} to {}", command, commandAnnotation.name(), command.getCommandName());
        }

        final int permLevel = permNode.getInt();
        if (permLevel != commandAnnotation.requiredPermissionLevel()) {
            log.info("Changing permission level of {} from {} to {}", command, commandAnnotation.requiredPermissionLevel(), permLevel);
        }
        ObfuscationReflectionHelper.setPrivateValue(STCommand.class, command, permLevel, "permissionLevel");

        if (enableNode.getBoolean(true)) {
            commands.add(command);
        } else {
            log.info("Command {} was disabled via configuration");
        }
        saveConfig();
    }

    public void gatherCommands(final ASMDataTable dataTable) {
        final ModClassLoader modClassLoader = Loader.instance().getModClassLoader();

        mainLoop:
        for (final ASMDataTable.ASMData data : dataTable.getAll(Command.class.getName())) {
            try {
                Class<?> clazz = Class.forName(data.getClassName(), true, modClassLoader);
                if (!STCommand.class.isAssignableFrom(clazz)) {
                    throw new RuntimeException("Class: " + clazz.getName() + " is annotated with @Command, but doesn't extend STCommand!");
                }

                final Class<? extends STCommand> commandClass = clazz.asSubclass(STCommand.class);
                final Command commandAnnotation = commandClass.getAnnotation(Command.class);

                for (final Class<?> featureClass : commandAnnotation.requiredFeatures()) {
                    if (Features.isRegistered(featureClass)) {
                        log.info("Service present");
                    } else {
                        log.info("Not registering command {} becuase one of its required features was not enabled. Service: {}", data.getClassName(), featureClass.getName());
                        continue mainLoop;
                    }
                }

                final STCommand command = commandClass.newInstance();

                registerCommand(command, commandAnnotation);

            } catch (ClassNotFoundException e) {
                log.fatal("Class {} could not be found", data.getClassName());
                throw new Error(e);
            } catch (InstantiationException e) {
                log.error("Command class {} doesn't have a no-arg constructor", data.getClassName());
                throw new RuntimeException("No no-arg constructor", e);
            } catch (IllegalAccessException e) {
                log.error("Command class {} doesn't have a public no-arg constructor", data.getClassName());
                throw new RuntimeException("No public constructor", e);
            }
        }
    }

    private void saveConfig() {
        try {
            loader.save(node);
        } catch (IOException e) {
            log.error("Failed to save command configuration file {}", configFile, e);
        }
    }

    private void overrideHelp(final CommandHandler commandHandler) {
        if (Loader.isModLoaded("HelpFixer")) {
            log.trace("HelpFixer detected. Not overriding /help");
        } else {
            log.trace("Overriding /help");
            commandHandler.registerCommand(new CommandHelp() {

                @Override
                protected List<ICommand> getSortedPossibleCommands(final ICommandSender sender) {
                    final List<ICommand> list = MinecraftServer.getServer().getCommandManager().getPossibleCommands(sender);
                    final Iterator<ICommand> iterator = list.iterator();
                    while (iterator.hasNext()) {
                        ICommand command = iterator.next();
                        if (command.getCommandName() == null) {
                            log.warn("Identified command with a null name: {}", command.getClass());
                            iterator.remove();
                        } else if (command.getCommandUsage(sender) == null) {
                            log.warn("Identified command with null usage: {}", command.getClass());
                            iterator.remove();
                        }
                    }

                    Collections.sort(list, (o1, o2) -> o1.getCommandName().compareTo(o2.getCommandName()));
                    return list;
                }
            });
        }
    }

    /**
     * <em>Internal Use Only!</em>
     *
     * @param server The server
     */
    public void doRegister(final MinecraftServer server) {
        log.trace("Registering commands with Minecraft...");
        CommandHandler commandHandler = (CommandHandler) server.getCommandManager();
        commands.forEach(commandHandler::registerCommand);

        if (ServerToolsCore.instance().getConfig().getGeneral().isHelpOverrideEnabled()) {
            overrideHelp(commandHandler);
        }
    }
}
