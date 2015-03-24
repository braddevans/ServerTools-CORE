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
package info.servertools.core.lib;

import net.minecraftforge.classloading.FMLForgePlugin;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.FMLInjectionData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Data that may change based on the installed environment
 */
public final class Environment {

    private static final Logger log = LogManager.getLogger();

    /**
     * Get the location where Minecraft is installed
     *
     * @return The location where Minecraft is installed
     */
    public static Path getMinecraftDir() {
        return minecraftDir;
    }

    /**
     * Get the location where mod configs are stored
     *
     * @return The location where mod configs are stored
     */
    public static Path getConfigDir() {
        return configDir;
    }

    /**
     * Get the location where ServerTools config files are stored
     * <p>
     * This differs from {@link #getServerToolsDataDir()} as files in this directory are general configurations that could apply to any server instance.
     * </p>
     *
     * @return The location where ServerTools config files are stored
     */
    public static Path getServerToolsConfigDir() {
        return serverToolsConfigDir;
    }

    /**
     * Get the location where ServerTools data files are stored
     * <p>
     * This differs from the {@link #getServerToolsConfigDir()} as files in this directory are generally specific to the current instance of the server.
     * </p>
     *
     * @return The location where ServerTools data files are stored
     */
    public static Path getServerToolsDataDir() {
        return serverToolsDataDir;
    }

    /**
     * Get if FML runtime deobfuscation is enabled. This will be {@code true} in normal obfuscated environments, and {@code false} in development environments
     *
     * @return If runtime deobfuscation is enabled
     */
    public static boolean runtimeDeobfEnabled() {
        return FMLForgePlugin.RUNTIME_DEOBF;
    }


    private static final Path minecraftDir;
    private static final Path configDir;
    private static final Path serverToolsConfigDir;
    private static final Path serverToolsDataDir;

    static {
        minecraftDir = ((File) FMLInjectionData.data()[6]).toPath();
        configDir = Loader.instance().getConfigDir().toPath();
        serverToolsConfigDir = getConfigDir().resolve("servertools");
        serverToolsDataDir = minecraftDir.resolve("servertools");

        try {
            Files.createDirectories(serverToolsConfigDir);
            Files.createDirectories(serverToolsDataDir);
        } catch (IOException e) {
            log.fatal("Failed to create servertools directories, double check that no files exist that conflict", e);
            throw new RuntimeException(e);
        }
    }

    private Environment() {
    }
}
