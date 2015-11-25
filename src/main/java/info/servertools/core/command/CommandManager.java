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

import static java.util.Objects.requireNonNull;

import info.servertools.core.ServerToolsCore;

import net.minecraft.command.CommandHandler;
import net.minecraft.command.CommandHelp;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

import net.minecraftforge.fml.common.Loader;

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
 * ServerTools Command Manager. Register commands using {@link #registerCommand(STCommand)}
 */
public final class CommandManager {

    private static final Logger log = LogManager.getLogger();

    private static final Pattern validCommandPattern = Pattern.compile("[a-zA-Z0-9_\\-]+");

    private static final Collection<STCommand> commands = new ArrayList<>();

    private static final String HEADER =
            "This configuration file can be used to: disable, rename, and change required permission level for ServerTools commands. " +
                    "Command names must match the REGEX [a-zA-Z0-9_\\-]";
    private static Path configFile;
    private static HoconConfigurationLoader loader;
    private static CommentedConfigurationNode node = SimpleCommentedConfigurationNode.root();

    /**
     * Register a {@linkplain STCommand} with ServerTools. This will handle registration with Minecraft internally.
     *
     * @param command The command to register
     */
    public static void registerCommand(final STCommand command) {
        requireNonNull(command, "command");
        log.trace("registerCommand {}", command);

        CommentedConfigurationNode commandNode = node.getNode(command.getClass().getName());
        CommentedConfigurationNode enableNode = commandNode.getNode("enable-command");
        CommentedConfigurationNode nameNode = commandNode.getNode("command-name");
        CommentedConfigurationNode permNode = commandNode.getNode("permission-level");

        if (enableNode.isVirtual() || enableNode.getValue() == null) {
            enableNode.setValue(true);
        }
        if (nameNode.isVirtual() || nameNode.getValue() == null) {
            nameNode.setValue(command.getDefaultName());
        }
        if (permNode.isVirtual() || permNode.getValue() == null) {
            permNode.setValue(command.getRequiredPermissionLevel());
        }

        enableNode.setComment("Set to false to disable this command");
        nameNode.setComment("Default name: " + command.getDefaultName());
        permNode.setComment("The required permission level for this command. 0 is everyone. 1+ requires some level of OP");

        final String name = nameNode.getString();
        command.setName(name);

        if (!validCommandPattern.matcher(name).matches()) {
            throw new RuntimeException(String.format("Command %s was configured with an invald name: %s", command, name));
        }
        if (!command.getDefaultName().equals(name)) {
            log.info("Command {} was renamed from {} to {}", command, command.getDefaultName(), command.getCommandName());
        }

        final int permLevel = permNode.getInt();
        if (permLevel != command.getRequiredPermissionLevel()) {
            log.info("Changing permission level of {} from {} to {}", command, command.getRequiredPermissionLevel(), permLevel);
            command.setPermissionLevel(permLevel);
        }

        if (enableNode.getBoolean(true)) {
            commands.add(command);
        } else {
            log.info("Command {} was disabled via configuration");
        }
        saveConfig();
    }

    private static void saveConfig() {
        try {
            loader.save(node);
        } catch (IOException e) {
            log.error("Failed to save command configuration file {}", configFile, e);
        }
    }

    private static void overrideHelp(final CommandHandler commandHandler) {
        if (Loader.isModLoaded("HelpFixer")) {
            log.info("HelpFixer detected. Not overriding /help");
        } else {
            log.trace("Overriding /help");
            commandHandler.registerCommand(new CommandHelp() {
                protected List<ICommand> getSortedPossibleCommands(ICommandSender sender) {
                    List<ICommand> list = MinecraftServer.getServer().getCommandManager().getPossibleCommands(sender);

                    Iterator<ICommand> iterator = list.iterator();
                    while (iterator.hasNext()) {
                        ICommand command = iterator.next();
                        if (command.getCommandName() == null || command.getCommandUsage(sender) == null) {
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
     * @param configFile The configFile
     *
     * @throws IOException IOException
     */
    public static void init(final Path configFile) throws IOException {
        CommandManager.configFile = configFile;
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
            throw e;
        }
    }

    /**
     * <em>Internal Use Only!</em>
     *
     * @param server The server
     */
    public static void doRegister(final MinecraftServer server) {
        log.trace("Registering commands with Minecraft...");
        CommandHandler commandHandler = (CommandHandler) server.getCommandManager();
        commands.forEach(commandHandler::registerCommand);

        if (ServerToolsCore.getConfig().getGeneral().isHelpOverrideEnabled()) {
            overrideHelp(commandHandler);
        }
    }
}
