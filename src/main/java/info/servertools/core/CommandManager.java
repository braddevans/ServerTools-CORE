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
package info.servertools.core;

import net.minecraft.command.CommandHandler;
import net.minecraft.server.MinecraftServer;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

public final class CommandManager {

    private static final Logger log = LogManager.getLogger();

    private static final Pattern validCommandPattern = Pattern.compile("[a-zA-Z0-9_\\-]+");

    private static final Deque<STCommand> commands = new ArrayDeque<>();

    private static final String HEADER =
            "This configuration file can be used to both disable and rename ServerTools commands. Command names must match the REGEX [a-zA-Z0-9_\\-]";
    private static Path configFile;
    private static HoconConfigurationLoader loader;
    private static CommentedConfigurationNode node = SimpleCommentedConfigurationNode.root();

    public static void registerCommand(final STCommand command) {
        requireNonNull(command, "command");
        log.trace("registerCommand {}", command);

        CommentedConfigurationNode commandNode = node.getNode(command.getClass().getName());
        CommentedConfigurationNode enableNode = commandNode.getNode("enable-command");
        CommentedConfigurationNode nameNode = commandNode.getNode("command-name");

        if (enableNode.isVirtual() || enableNode.getValue() == null) {
            enableNode.setValue(true);
        }
        if (nameNode.isVirtual() || nameNode.getValue() == null) {
            nameNode.setValue(command.getDefaultName());
        }

        enableNode.setComment("Set to false to disable this command");
        nameNode.setComment("Default name: " + command.getDefaultName());

        final String name = nameNode.getString();
        command.setName(name);

        if (!validCommandPattern.matcher(name).matches()) {
            throw new RuntimeException(String.format("Command %s was configured with an invald name: %s", command, name));
        }
        if (!command.getDefaultName().equals(name)) {
            log.info("Command {} was renamed from {} to {}", command, command.getDefaultName(), command.getCommandName());
        }

        if (enableNode.getBoolean(true)) {
            commands.add(command);
        } else {
            log.info("Command {} was disabled via configuration");
        }
        saveConfig();
    }

    static void init(final Path configFile) throws IOException {
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

    private static void saveConfig() {
        try {
            loader.save(node);
        } catch (IOException e) {
            log.error("Failed to save command configuration file {}", configFile, e);
        }
    }

    static void register(final MinecraftServer server) {
        log.trace("Registering commands with Minecraft...");
        CommandHandler commandHandler = (CommandHandler) server.getCommandManager();
        while (!commands.isEmpty()) {
            final STCommand command = commands.pop();
            log.trace("Registering {} with Minecraft", command);
            commandHandler.registerCommand(command);
        }
    }
}
