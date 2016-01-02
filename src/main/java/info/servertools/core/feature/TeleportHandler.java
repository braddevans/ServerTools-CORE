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
package info.servertools.core.feature;

import static info.servertools.core.feature.TeleportHandler.EditTeleportResult.ALREADY_EXISTS;
import static info.servertools.core.feature.TeleportHandler.EditTeleportResult.CREATED;
import static info.servertools.core.feature.TeleportHandler.EditTeleportResult.DELETED;
import static info.servertools.core.feature.TeleportHandler.EditTeleportResult.NO_EXIST;

import info.servertools.core.Constants;
import info.servertools.core.util.FileIO;
import info.servertools.core.util.Location;

import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

import javax.annotation.Nullable;

public class TeleportHandler {

    private static final Logger log = LogManager.getLogger();

    private Map<String, Location> locationMap = new HashMap<>();
    private final Path saveFile;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Type type = new ParameterizedType() {

        @Override
        public Type[] getActualTypeArguments() { return new Type[]{ String.class, Location.class }; }

        @Override
        public Type getRawType() { return HashMap.class; }

        @Nullable
        @Override
        public Type getOwnerType() { return null; }
    };

    public TeleportHandler(final Path saveFile) throws IOException {
        this.saveFile = saveFile;
        load();
    }

    public Optional<Location> getTeleport(String name) {
        Objects.requireNonNull(name, "name");
        name = name.toLowerCase();
        return Optional.ofNullable(locationMap.get(name));
    }

    public Set<String> getTeleportNames() {
        return ImmutableSet.copyOf(locationMap.keySet());
    }

    public EditTeleportResult setTeleport(String name, final Location location) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(location, "location");
        name = name.toLowerCase();
        if (locationMap.containsKey(name)) {
            return ALREADY_EXISTS;
        } else {
            locationMap.put(name, location);
            save();
            return CREATED;
        }
    }

    public EditTeleportResult deleteTeleport(String name) {
        Objects.requireNonNull(name, "name");
        name = name.toLowerCase();
        if (locationMap.remove(name) != null) {
            save();
            return DELETED;
        } else {
            return NO_EXIST;
        }
    }

    private void load() throws IOException {
        synchronized (saveFile) {
            if (!Files.exists(saveFile)) return;
            try (BufferedReader reader = Files.newBufferedReader(saveFile, Constants.CHARSET)) {
                @Nullable Map<String, Location> map = gson.fromJson(reader, type);
                if (map == null) map = new HashMap<>();
                this.locationMap = map;
            } catch (IOException e) {
                log.error("Failed to load teleports", e);
                throw e;
            }
        }
    }

    private void save() {
        final HashMap<String, Location> tempMap = new HashMap<>(locationMap);
        FileIO.submitTask(() -> {
            synchronized (saveFile) {
                try {
                    if (Files.exists(saveFile)) Files.delete(saveFile);
                    try (final BufferedWriter writer = Files.newBufferedWriter(saveFile, Constants.CHARSET, StandardOpenOption.CREATE_NEW)) {
                        gson.toJson(tempMap, type, writer);
                    }
                } catch (IOException e) {
                    log.error("Failed to save teleports", e);
                }
            }
        });
    }


    public enum EditTeleportResult {
        CREATED("Teleport created"),
        DELETED("Teleport deleted"),
        ALREADY_EXISTS("That teleport already exists", true),
        NO_EXIST("That teleport doesn't exist", true);

        final String message;
        final boolean error;

        EditTeleportResult(final String message) {
            this(message, false);
        }

        EditTeleportResult(final String message, final boolean error) {
            this.message = message;
            this.error = error;
        }

        public ChatComponentText toChat() {
            final ChatComponentText component = new ChatComponentText(this.message);
            if (error) {
                component.getChatStyle().setColor(EnumChatFormatting.RED);
            }
            return component;
        }
    }
}
