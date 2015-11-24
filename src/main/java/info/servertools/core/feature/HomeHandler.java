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

import static info.servertools.core.feature.HomeHandler.EditHomeResult.DELETED;
import static info.servertools.core.feature.HomeHandler.EditHomeResult.NO_HOME;
import static info.servertools.core.feature.HomeHandler.EditHomeResult.SET;

import info.servertools.core.Constants;
import info.servertools.core.util.FileIO;
import info.servertools.core.util.Location;

import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

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

public class HomeHandler {
    private static final Logger log = LogManager.getLogger();

    private Map<UUID, Location> homeMap = new HashMap<>();
    private final Path saveFile;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Type type = new ParameterizedType() {

        @Override
        public Type[] getActualTypeArguments() { return new Type[]{ UUID.class, Location.class }; }

        @Override
        public Type getRawType() { return HashMap.class; }

        @Nullable
        @Override
        public Type getOwnerType() { return null; }
    };

    public HomeHandler(final Path saveFile) {
        this.saveFile = saveFile;
        load();
    }

    public Optional<Location> getHome(final UUID uuid) {
        Objects.requireNonNull(uuid, "uuid");
        return Optional.ofNullable(homeMap.get(uuid));
    }

    public EditHomeResult setHome(final UUID uuid, final Location location) {
        Objects.requireNonNull(uuid, "uuid");
        Objects.requireNonNull(location, "location");
        homeMap.put(uuid, location);
        save();
        return SET;
    }

    public EditHomeResult deleteHome(UUID uuid) {
        Objects.requireNonNull(uuid, "uuid");
        if (homeMap.remove(uuid) != null) {
            save();
            return DELETED;
        } else {
            return NO_HOME;
        }
    }

    private void load() {
        synchronized (saveFile) {
            if (!Files.exists(saveFile)) return;
            try (BufferedReader reader = Files.newBufferedReader(saveFile, Constants.CHARSET)) {
                @Nullable Map<UUID, Location> map = gson.fromJson(reader, type);
                if (map == null) map = new HashMap<>();
                this.homeMap = map;
            } catch (IOException e) {
                log.error("Failed to load homes", e);
            }
        }
    }

    private void save() {
        final HashMap<UUID, Location> tempMap = new HashMap<>(homeMap);
        FileIO.submitTask(() -> {
            synchronized (saveFile) {
                try {
                    if (Files.exists(saveFile)) Files.delete(saveFile);
                    try (final BufferedWriter writer = Files.newBufferedWriter(saveFile, Constants.CHARSET, StandardOpenOption.CREATE_NEW)) {
                        gson.toJson(tempMap, type, writer);
                    }
                } catch (IOException e) {
                    log.error("Failed to save homes", e);
                }
            }
        });
    }


    public enum EditHomeResult {
        SET("Home set"),
        DELETED("Home deleted"),
        NO_HOME("You didn't have a home set", true);

        final String message;
        final boolean error;

        EditHomeResult(final String message) {
            this(message, false);
        }

        EditHomeResult(final String message, final boolean error) {
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
