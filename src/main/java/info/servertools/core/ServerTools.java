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

import static info.servertools.core.lib.Environment.SERVERTOOLS_DIR;

import info.servertools.core.chat.Motd;
import info.servertools.core.chat.NickHandler;
import info.servertools.core.chat.VoiceSilenceHandler;
import info.servertools.core.command.CommandManager;
import info.servertools.core.config.STConfig;
import info.servertools.core.lib.Reference;
import info.servertools.core.task.TickHandler;
import info.servertools.core.util.FlatBedrockGenerator;

import net.minecraft.command.CommandHandler;
import net.minecraft.server.MinecraftServer;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

@Mod(
        modid = Reference.MOD_ID,
        name = Reference.MOD_NAME,
        dependencies = Reference.DEPENDENCIES,
        acceptableRemoteVersions = "*"
)
public class ServerTools {

    public static final Logger LOG = LogManager.getLogger(ServerTools.class);

    @Mod.Instance(Reference.MOD_ID)
    public static ServerTools instance;

    public Motd motd;
    public VoiceSilenceHandler voiceSilenceHandler;
    public NickHandler nickHandler;
    public TickHandler tickHandler;
    public BlockLogger blockLogger;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        STConfig.load();

        motd = new Motd(new File(SERVERTOOLS_DIR, "motd.txt"));
        voiceSilenceHandler = new VoiceSilenceHandler(
                new File(SERVERTOOLS_DIR, "voice.json"),
                new File(SERVERTOOLS_DIR, "silence.json")
        );
        nickHandler = new NickHandler(new File(SERVERTOOLS_DIR, "nicks.json"));
        tickHandler = new TickHandler();

        if (STConfig.settings().ENABLE_BLOCK_BREAK_LOG || STConfig.settings().ENABLE_BLOCK_PLACE_LOG) {
            blockLogger = new BlockLogger(
                    new File(SERVERTOOLS_DIR, "blockBreaks"), STConfig.settings().ENABLE_BLOCK_BREAK_LOG,
                    new File(SERVERTOOLS_DIR, "blockPlaces"), STConfig.settings().ENABLE_BLOCK_PLACE_LOG
            );
        }
    }

    @Mod.EventHandler
    public void init(final FMLInitializationEvent event) {
        if (STConfig.settings().ENABLE_FLAT_BEDROCK) {
            LOG.info("Registering Flat Bedrock Generator");
            GameRegistry.registerWorldGenerator(new FlatBedrockGenerator(), 1);
        }
    }

    @Mod.EventHandler
    public void serverStarted(final FMLServerStartedEvent event) {
        CommandHandler ch = (CommandHandler) MinecraftServer.getServer().getCommandManager();
        CommandManager.registerCommands(ch);
    }
}
