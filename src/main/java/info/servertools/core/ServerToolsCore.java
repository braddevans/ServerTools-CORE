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

import info.servertools.core.feature.Features;
import info.servertools.core.command.CommandManager;
import info.servertools.core.feature.*;
import info.servertools.core.util.STConfig;

import net.minecraft.server.MinecraftServer;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

@Mod(
        modid = Constants.MOD_ID,
        name = Constants.MOD_NAME,
        dependencies = Constants.DEPENDENCIES,
        acceptableRemoteVersions = "*", // Don't require mod on client
        acceptedMinecraftVersions = '[' + Constants.MC_VERSION + ']'
)
public final class ServerToolsCore {

    @Mod.Instance(Constants.MOD_ID)
    private static ServerToolsCore instance;

    private Path configDir;
    private STConfig<CoreConfig> coreConfig;
    private CommandManager commandManager;

    @Mod.EventHandler
    public void onPreInit(final FMLPreInitializationEvent event) throws IOException, ObjectMappingException {
        final File modConfigDir = event.getModConfigurationDirectory();
        this.configDir = modConfigDir.toPath().resolve("ServerTools-CORE");
        this.coreConfig = new STConfig<>(configDir.resolve("ServerTools-CORE.conf"), CoreConfig.class);

        this.commandManager = new CommandManager(configDir.resolve("commands.conf"));

        final CoreConfig config = coreConfig.getConfig();

        if (config.getChat().isMotdEnabled()) {
            final Motd motd = new Motd(configDir.resolve("motd.txt"));
            Features.register(Motd.class, motd);
            MinecraftForge.EVENT_BUS.register(motd);
        }

        if (config.getTeleport().isTeleportsEnabled()) {
            Features.register(TeleportHandler.class, new TeleportHandler(configDir.resolve("teleports.json")));
        }

        if (config.getTeleport().isHomesEnabled()) {
            Features.register(HomeHandler.class, new HomeHandler(configDir.resolve("homes.json")));
        }

        if (config.getChat().isSilenceEnabled()) {
            final SilenceHandler silenceHandler = new SilenceHandler(configDir.resolve("silenced.json"));
            Features.register(SilenceHandler.class, silenceHandler);
            MinecraftForge.EVENT_BUS.register(silenceHandler);
        }

        this.commandManager.gatherCommands(event.getAsmData());
    }

    @Mod.EventHandler
    public void onInit(final FMLInitializationEvent event) {
        if (coreConfig.getConfig().getGeneral().isFlatBedrockEnabled()) {
            GameRegistry.registerWorldGenerator(new FlatBedrockGenerator(), 1);
        }
    }

    @Mod.EventHandler
    public void onServerStarted(final FMLServerStartedEvent event) {
        this.commandManager.doRegister(MinecraftServer.getServer());
    }

    public CoreConfig getConfig() {
        return coreConfig.getConfig();
    }

    public static ServerToolsCore instance() {
        return instance;
    }
}
