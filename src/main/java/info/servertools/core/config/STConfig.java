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

import info.servertools.core.STVersion;
import info.servertools.core.lib.Environment;

import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class STConfig {

    private static final Logger log = LogManager.getLogger(STConfig.class);

    public static final String CATEGORY_GENERAL = "general";
    public static final String CATEGORY_WORLD = "world";
    public static final String CATEGORY_CHAT = "chat";

    private static final File configFile = new File(Environment.SERVERTOOLS_DIR, "servertools.cfg");

    private static Settings settings;
    
    private static Configuration config = new Configuration(configFile, STVersion.VERSION);

    public static Settings settings() {
        return settings;
    }


    public static void load() {
        config.load();
        log.info("Loading ServerTools Config version: {}", config.getLoadedConfigVersion());
        config.setCategoryComment(CATEGORY_GENERAL, "General Settings for ServerTools");
        config.setCategoryComment(CATEGORY_WORLD, "Settings that affect the world and how ServerTools interacts with it");
        config.setCategoryComment(CATEGORY_CHAT, "Settings that affect chat");

        settings = new Settings();

        if (config.hasChanged()) {
            config.save();
        }
    }

    public static class Settings {

        // General

        public final boolean ENABLE_HELP_OVERRIDE = config.get(
                CATEGORY_GENERAL, "enable-help-override", true, "Overrides the /help command to make it work with broken mod commands"
        ).getBoolean();

        // World

        public final boolean ENABLE_FLAT_BEDROCK = config.get(
                CATEGORY_WORLD, "enable-flat-bedrock", true, "Makes newly generated chunks have a flat one layer thick level of bedrock"
        ).getBoolean();

        public final boolean ENABLE_BLOCK_BREAK_LOG = config.get(
                CATEGORY_WORLD, "enable-block-break-log", false, "Enables logging whenever a block is broken on the server"
        ).getBoolean();

        public final boolean ENABLE_BLOCK_PLACE_LOG = config.get(
                CATEGORY_WORLD, "enable-block-place-log", false, "Enables logging whenever a block is placed on the server"
        ).getBoolean();

        // Chat

        public final boolean ENABLE_MOTD_LOGIN = config.get(
                CATEGORY_CHAT, "enable-motd-on-login", true, "Enables sending the MOTD to players when they log onto the server"
        ).getBoolean();

        public final boolean ENABLE_OP_PREFIX = config.get(
                CATEGORY_CHAT, "enable-op-prefix", true, "Enables a prefix on OP messages in chat"
        ).getBoolean();

        public final String OP_PREFIX = config.get(
                CATEGORY_CHAT, "op-prefix", "OP", "The prefix used for ops in chat"
        ).getString();

        public final String VOICE_PREFIX = config.get(
                CATEGORY_CHAT, "voice-prefix", "+", "The prefix used for voiced players in chat"
        ).getString();

        public final List<String> SILENCE_BLACKLISTED_COMMANDS = Arrays.asList(config.getStringList(
                "silence-blacklisted-commands", CATEGORY_CHAT, new String[]{"tell", "me", "tellraw", "say"}, "The commands that are not allowed for silenced users"
        ));

    }
}
