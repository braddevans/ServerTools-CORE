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
package info.servertools.core.config;

import static com.google.common.base.Preconditions.checkNotNull;
import static info.servertools.core.command.CommandLevel.OP;

import info.servertools.core.ServerTools;
import info.servertools.core.command.ServerToolsCommand;
import info.servertools.core.lib.Reference;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class CommandConfigHandler {

    private static final Logger log = LogManager.getLogger();

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Type type = new ParameterizedType() {
        @Override
        public Type[] getActualTypeArguments() { return new Type[]{String.class, ConfigEntry.class}; }

        @Override
        public Type getRawType() { return HashMap.class; }

        @Nullable
        @Override
        public Type getOwnerType() { return null; }
    };

    private final Path saveFile;

    /**
     * Construct a new {@code CommandConfigHandler}
     *
     * @param saveFile The file to save configuration values to
     */
    public CommandConfigHandler(final Path saveFile) {
        this.saveFile = checkNotNull(saveFile);
    }

    private Map<String, ConfigEntry> commandConfigs = new HashMap<>();

    /**
     * Get the {@link ConfigEntry} for the given {@link ServerToolsCommand}
     *
     * @param command The command
     *
     * @return The config entry
     */
    public ConfigEntry getEntry(final ServerToolsCommand command) {
        checkNotNull(command, "command");
        final String clazzName = command.getClass().getName();
        synchronized (saveFile) {
            if (!commandConfigs.containsKey(clazzName)) {
                ConfigEntry entry = new ConfigEntry();
                entry.commandName = command.getDefaultName();
                entry.enableCommand = true;
                entry.requireOP = command.getRequiredLevel() == OP;
                commandConfigs.put(clazzName, entry);
                return entry;
            } else {
                return commandConfigs.get(clazzName);
            }
        }
    }

    /**
     * Load the configuration from disk, done one a separate thread
     */
    public void load() {
        if (!Files.exists(saveFile)) {
            return;
        }
        ServerTools.executorService.execute(new Runnable() {
            @Override
            public void run() {
                try (final BufferedReader reader = Files.newBufferedReader(saveFile, Reference.CHARSET)) {
                    synchronized (saveFile) {
                        commandConfigs = gson.fromJson(reader, type);
                    }
                } catch (JsonSyntaxException e) {
                    log.error("Failed to parse command configuration file as valid JSON", e);
                } catch (IOException e) {
                    log.error("Failed to load command configuration file from disk", e);
                }
            }
        });
    }

    /**
     * Save the configuration to disk, done on a separate thread
     */
    public void save() {
        ServerTools.executorService.execute(new Runnable() {
            @Override
            public void run() {
                try (final BufferedWriter writer = Files.newBufferedWriter(saveFile, Reference.CHARSET)) {
                    synchronized (saveFile) {
                        gson.toJson(commandConfigs, type, writer);
                    }
                } catch (IOException e) {
                    log.error("Failed to save command configuration file to disk", e);
                }
            }
        });
    }

    /**
     * A container for the configuration values for each {@link ServerToolsCommand}
     */
    public static class ConfigEntry {

        String commandName;
        boolean enableCommand;
        boolean requireOP;

        public String getCommandName() {
            return commandName;
        }

        public boolean isEnableCommand() {
            return enableCommand;
        }

        public boolean isRequireOP() {
            return requireOP;
        }
    }
}
