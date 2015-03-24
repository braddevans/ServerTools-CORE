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

import static info.servertools.core.util.STConfig.CATEGORY_CHAT;
import static info.servertools.core.util.STConfig.CATEGORY_GENERAL;
import static info.servertools.core.util.STConfig.CATEGORY_TELEPORT;
import static info.servertools.core.util.STConfig.CATEGORY_WORLD;

import info.servertools.core.STVersion;
import info.servertools.core.lib.Environment;
import info.servertools.core.util.STConfig;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public final class CoreConfig {

    private static final Logger log = LogManager.getLogger();

    public static final STConfig stConfig;

    public static boolean ENABLE_HELP_OVERRIDE;
    public static boolean ENABLE_FLAT_BEDROCK;
    public static boolean ENABLE_BLOCK_BREAK_LOG;
    public static boolean ENABLE_BLOCK_PLACE_LOG;
    public static boolean ENABLE_MOTD_ON_LOGIN;
    public static boolean ENABLE_OP_PREFIX;
    public static String OP_PREFIX;
    public static String VOICE_PREFIX;
    public static List<String> SILENCE_BLACKLISTED_COMMANDS;

    public static boolean BROADCAST_NICK_CHANGES;
    public static final Set<Pattern> NICKNAME_BLACKLIST = new HashSet<>();

    // Teleport
    public static boolean ENABLE_INTERDIM_TELEPORT;

    public static boolean ENABLE_INTERDIM_TPREQUEST;

    static {
        stConfig = new STConfig(Environment.getServerToolsConfigDir().resolve("core.cfg").toFile(), STVersion.VERSION);

        final Configuration config = stConfig.getConfig();

        String category;
        Property prop;

        category = CATEGORY_GENERAL;
        {
            prop = config.get(category, "enable-help-override", true);
            prop.comment = "Overrides the /help command to make it work with broken mod commands";
            prop.setRequiresWorldRestart(true);
            ENABLE_HELP_OVERRIDE = prop.getBoolean();
        }

        category = CATEGORY_WORLD;
        {
            prop = config.get(category, "enable-flat-bedrock", true);
            prop.comment = "Makes newly generated chunks have a flat one layer thick level of bedrock";
            prop.setRequiresMcRestart(true);
            ENABLE_FLAT_BEDROCK = prop.getBoolean();
        }

        category = CATEGORY_WORLD + ".Logging";
        {
            prop = config.get(category, "enable-block-break-log", false);
            prop.comment = "Enable logging whenever a player breaks a block on the server";
            prop.setRequiresMcRestart(true);
            ENABLE_BLOCK_BREAK_LOG = prop.getBoolean();

            prop = config.get(category, "enable-block-place-log", false);
            prop.comment = "Enable logging whenever a player places a block on the server";
            prop.setRequiresMcRestart(true);
            ENABLE_BLOCK_PLACE_LOG = prop.getBoolean();
        }

        category = CATEGORY_CHAT;
        {
            prop = config.get(category, "enable-motd-on-login", true);
            prop.comment = "Enables sending the MOTD to players when the log onto the server";

            prop = config.get(category, "voice-prefix", "+");
            prop.comment = "The prefix to use for voiced players in chat";
            VOICE_PREFIX = prop.getString();

            prop = config.get(category, "silence-blacklisted-commands", new String[]{"tell", "tellraw", "me", "say"});
            prop.comment = "The commands that silenced players aren't allowed to use";
            SILENCE_BLACKLISTED_COMMANDS = Arrays.asList(prop.getStringList());

            prop = config.get(category, "enable-nick-change-broadcast", true);
            prop.comment = "If the entire server should be notified when a player changes their nickname";
            BROADCAST_NICK_CHANGES = prop.getBoolean();

            prop = config.get(category, "nickname-regex-blacklist", new String[0]);
            prop.comment = "A comma separated list of REGEX patterns to use for validating nicknames";
            NICKNAME_BLACKLIST.clear();
            for (final String entry : prop.getStringList()) {
                try {
                    final Pattern pattern = Pattern.compile(entry);
                    NICKNAME_BLACKLIST.add(pattern);
                } catch (PatternSyntaxException e) {
                    log.warn("Invalid nickname REGEX pattern {}", entry);
                }
            }
        }

        category = CATEGORY_CHAT + ".OPPrefix";
        {
            prop = config.get(category, "enable-op-prefix", true);
            prop.comment = "Enables a prefix on OP messages in chat";
            ENABLE_OP_PREFIX = prop.getBoolean();

            prop = config.get(category, "op-prefix", "OP");
            prop.comment = "The prefix to use for OPs in chat";
            OP_PREFIX = prop.getString();
        }

        category = CATEGORY_TELEPORT;
        {
            prop = config.get(category, "enable-interdimension-teleport", false);
            prop.comment = "If enabled, teleports will work across dimensions";
            ENABLE_INTERDIM_TELEPORT = prop.getBoolean();

            prop = config.get(category, "enable-interdimension-tprequest", false);
            prop.comment = "If enabled, the /tprequest command will work across dimensions";
            ENABLE_INTERDIM_TPREQUEST = prop.getBoolean();
        }

        stConfig.saveIfChanged();
    }

    private CoreConfig() {}
}
