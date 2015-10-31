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

public class CoreConfig {

    @Setting(value = "general")
    private GeneralCategory general = new GeneralCategory();

    @Setting(value = "logging")
    private LoggingCategory logging = new LoggingCategory();

    @Setting(value = "chat")
    private ChatCategory chat = new ChatCategory();

    public GeneralCategory getGeneral() {
        return general;
    }

    public LoggingCategory getLogging() {
        return logging;
    }

    public ChatCategory getChat() {
        return chat;
    }

    @ConfigSerializable
    public static class GeneralCategory extends Category {

    }

    @ConfigSerializable
    public static class ChatCategory extends Category {

        @Setting(value = "enable-motd", comment = "Enable a message of the day sent to users when they log in")
        private boolean motdEnabled = true;

        public boolean isMotdEnabled() {
            return motdEnabled;
        }
    }

    @ConfigSerializable
    public static class LoggingCategory extends Category {
        @Setting(value = "enable-block-break-logging", comment = "Enable logging to a file whenever a block is broken on the server")
        private boolean breakLogEnabled = false;

        @Setting(value = "enable-block-place-logging", comment = "Enable logging to a file whenever a block is placed on the server")
        private boolean placeLogEnabled = false;

        public boolean isBreakLogEnabled() {
            return breakLogEnabled;
        }

        public boolean isPlaceLogEnabled() {
            return placeLogEnabled;
        }
    }

    @ConfigSerializable
    public static class Category {}
}
