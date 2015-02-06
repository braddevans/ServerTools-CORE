/*
 * Copyright 2014 ServerTools
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

import info.servertools.core.chat.Motd;
import info.servertools.core.chat.NickHandler;
import info.servertools.core.chat.VoiceHandler;
import info.servertools.core.command.CommandManager;
import info.servertools.core.lib.Reference;
import info.servertools.core.task.TickHandler;
import info.servertools.core.util.FlatBedrockGenerator;

import net.minecraft.command.CommandHandler;
import net.minecraft.server.MinecraftServer;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.FMLInjectionData;
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

    public static final File minecraftDir = (File) FMLInjectionData.data()[6];
    public static final File serverToolsDir = new File(minecraftDir, "servertools");

    public static final Logger LOG = LogManager.getLogger(Reference.MOD_NAME);

    static {
        serverToolsDir.mkdirs();
    }

    @Mod.Instance(Reference.MOD_ID)
    public static ServerTools instance;

    public Motd motd;

    public VoiceHandler voiceHandler;

    public TickHandler tickHandler;

    public BlockLogger blockLogger;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        /* Initialize the Core Configuration */
        CoreConfig.init(new File(serverToolsDir, "core.cfg"));

        /* Initialize the save file for nicknames */
        NickHandler.instance.init(new File(serverToolsDir, "nicks.json"));

        /* Create a new TickHandler Instance */
        tickHandler = new TickHandler();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        /* Register the Flat Bedrock Generator */
        if (CoreConfig.GENERATE_FLAT_BEDROCK) {
            LOG.info("Registering Flat Bedrock Generator");
            GameRegistry.registerWorldGenerator(new FlatBedrockGenerator(), 1);
        }
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        /* Initialize the Message of the Day */
        if (motd == null) { motd = new Motd(new File(serverToolsDir, "motd.txt")); }

        /* Initialize the Voice Handler */
        if (voiceHandler == null) { voiceHandler = new VoiceHandler(); }

        /* Initialize the Block Logger */
        if (blockLogger == null && (CoreConfig.LOG_BLOCK_BREAKS || CoreConfig.LOG_BLOCK_PLACES)) {
            blockLogger = new BlockLogger(
                    new File(serverToolsDir, "blockBreaks"),
                    new File(serverToolsDir, "blockPlaces"),
                    CoreConfig.LOG_BLOCK_BREAKS,
                    CoreConfig.LOG_BLOCK_PLACES
            );
        }

        /* Initialize the Core Commands to be Registered */
        CommandManager.initCoreCommands();
    }

    @Mod.EventHandler
    public void serverStarted(FMLServerStartedEvent event) {
        /* Register All Commands In Queue */
        CommandHandler ch = (CommandHandler) MinecraftServer.getServer().getCommandManager();
        CommandManager.registerCommands(ch);
    }

    @Mod.EventHandler
    public void serverStopped(FMLServerStoppedEvent event) {
        CommandManager.onServerStopped();
    }
}
