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

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.ArrayList;
import java.util.List;

public class CoreConfig {

    @Setting(value = "general")
    private GeneralCategory general = new GeneralCategory();

    @Setting(value = "chat")
    private ChatCategory chat = new ChatCategory();

    @Setting(value = "teleport")
    private TeleportCategory teleport = new TeleportCategory();

    public GeneralCategory getGeneral() {
        return general;
    }

    public ChatCategory getChat() {
        return chat;
    }

    public TeleportCategory getTeleport() {
        return teleport;
    }

    @ConfigSerializable
    public static class GeneralCategory extends Category {

        @Setting(value = "enable-help-override", comment = "Enable an override for the /help command that makes it work with broken mod commands")
        private boolean helpOverrideEnabled = true;

        public boolean isHelpOverrideEnabled() {
            return helpOverrideEnabled;
        }
    }

    @ConfigSerializable
    public static class ChatCategory extends Category {

        @Setting(value = "enable-motd", comment = "Enable a message of the day sent to users when they log in")
        private boolean motdEnabled = true;

        @Setting(value = "additional-silence-banned-commands",
                comment = "Additional class names to ban when users are silenced. The built in vanilla commands are already banned")
        private List<String> additionalSilenceCommands = new ArrayList<>();

        public boolean isMotdEnabled() {
            return motdEnabled;
        }

        public List<String> getAdditionalSilenceCommands() {
            return additionalSilenceCommands;
        }
    }

    @ConfigSerializable
    public static class TeleportCategory extends Category {

        @Setting(value = "enable-teleports", comment = "Enable server teleports")
        private boolean teleportsEnabled = true;

        @Setting(value = "enable-homes", comment = "Allow players to set a home and teleport back to it from anywhere")
        private boolean homesEnabled = true;

        @Setting(value = "enable-cross-dimension-teleports", comment = "Enable teleporting to a different dimension")
        private boolean crossDimTeleportEnabled = false;

        public boolean isTeleportsEnabled() {
            return teleportsEnabled;
        }

        public boolean isHomesEnabled() {
            return homesEnabled;
        }

        public boolean isCrossDimTeleportEnabled() {
            return crossDimTeleportEnabled;
        }
    }

    @ConfigSerializable
    public static class Category {}
}
