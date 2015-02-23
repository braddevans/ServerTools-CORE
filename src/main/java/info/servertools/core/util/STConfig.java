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
package info.servertools.core.util;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

import javax.annotation.Nullable;

/**
 * Wrapper around the Forge {@link Configuration}
 */
public class STConfig {

    public static final String CATEGORY_GENERAL = "General";
    public static final String CATEGORY_WORLD = "World";
    public static final String CATEGORY_CHAT = "Chat";

    private final File saveFile;
    private final Configuration configuration;

    public STConfig(final File saveFile) {
        this(saveFile, null);
    }

    public STConfig(final File saveFile, @Nullable final String version) {
        this.saveFile = checkNotNull(saveFile, "saveFile");
        checkArgument(!saveFile.exists() || saveFile.isFile(), "A directory exists with the name: " + saveFile);
        this.configuration = new Configuration(saveFile, version, true);
    }

    /**
     * Load the configuration from disk
     *
     * @see Configuration#load()
     */
    public void load() {
        configuration.load();
    }

    /**
     * Save the configuration to disk
     *
     * @see Configuration#save()
     */
    public void save() {
        configuration.save();
    }

    /**
     * Save the configuration if it has changed
     *
     * @see Configuration#hasChanged()
     * @see Configuration#save()
     */
    public void saveIfChanged() {
        if (configuration.hasChanged()) {
            configuration.save();
        }
    }

    /**
     * Get the Forge {@link Configuration}
     *
     * @return The configuration
     */
    public Configuration getConfig() {
        return configuration;
    }

    /**
     * Get the file that this configuration saves to
     *
     * @return The save file
     */
    public File getSaveFile() {
        return saveFile;
    }
}
