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
package info.servertools.core.teleport;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import info.servertools.core.ServerTools;
import info.servertools.core.lib.Reference;
import info.servertools.core.util.Location;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

public class TeleportHandler {

    private static final Logger log = LogManager.getLogger(TeleportHandler.class);

    private final Path saveFile;

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static final Type type = new ParameterizedType() {
        @Override
        public Type[] getActualTypeArguments() { return new Type[]{String.class, Location.class}; }

        @Override
        public Type getRawType() { return ConcurrentHashMap.class; }

        @Nullable
        @Override
        public Type getOwnerType() { return null; }
    };

    private Map<String, Location> teleportMap = new ConcurrentHashMap<>();

    /**
     * Construct a new TeleportHandler
     *
     * @param saveFile The {@link Path file} that we should save to
     */
    public TeleportHandler(final Path saveFile) {
        this.saveFile = checkNotNull(saveFile, "saveFile");
        checkArgument(!Files.exists(saveFile) || Files.isRegularFile(saveFile), "A directory exists with the name: " + saveFile);
        load();
    }

    /**
     * Get a server teleport by its name
     *
     * @param name The name of the teleport
     *
     * @return The {@link Location} of the teleport
     */
    @Nullable
    public Location getTeleport(final String name) {
        checkNotNull(name, "name");
        return teleportMap.get(name);
    }

    /**
     * Set a server teleport
     *
     * @param name     The name of the teleport
     * @param location The location of the teleport
     *
     * @return {@code true} if an existing teleport was replaced, {@code false} if a new one was created
     */
    public boolean setTeleport(final String name, final Location location) {
        checkNotNull(name, "name");
        checkNotNull(location, "location");
        if (teleportMap.put(name, location) != null) {
            save();
            return true;
        } else {
            save();
            return false;
        }
    }

    /**
     * Remove a server teleport
     *
     * @param name The name of the teleport
     *
     * @return {@code true} if the teleport was removed, {@code false} if it didn't exist
     */
    public boolean removeTeleport(final String name) {
        if (teleportMap.remove(name) != null) {
            save();
            return true;
        }
        return false;
    }

    /**
     * Get an {@link ImmutableMap immutable} copy of the server teleports
     *
     * @return A copy of the server teleports
     */
    public ImmutableMap<String, Location> getTeleports() {
        return ImmutableMap.copyOf(teleportMap);
    }

    /**
     * Save the teleports to disk
     */
    public void save() {
        ServerTools.executorService.execute(new Runnable() {
            @Override
            public void run() {
                try (final BufferedWriter writer = Files.newBufferedWriter(saveFile, Reference.CHARSET)) {
                    gson.toJson(teleportMap, type, writer);
                } catch (IOException e) {
                    log.error("Failed to save teleport file to disk: " + saveFile, e);
                }
            }
        });
    }

    /**
     * Read the teleports from disk
     */
    private void load() {
        ServerTools.executorService.execute(new Runnable() {
            @Override
            public void run() {
                if (!Files.exists(saveFile)) {
                    return;
                }

                try (final BufferedReader reader = Files.newBufferedReader(saveFile, Reference.CHARSET)) {
                    teleportMap = gson.fromJson(reader, type);
                } catch (JsonSyntaxException e) {
                    log.error("Failed to parse teleport file as valid JSON: " + saveFile, e);
                } catch (IOException e) {
                    log.error("Failed to load teleport file from disk: " + saveFile, e);
                }
            }
        });
    }
}
