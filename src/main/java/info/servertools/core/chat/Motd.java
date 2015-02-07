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
package info.servertools.core.chat;

import info.servertools.core.config.STConfig;
import info.servertools.core.lib.Reference;
import info.servertools.core.lib.Strings;
import info.servertools.core.util.ChatUtils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumChatFormatting;

import com.google.common.io.Files;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class Motd {

    private static final Logger log = LogManager.getLogger(Motd.class);

    private Collection<String> motd = new ArrayList<>();
    private final File motdFile;

    public Motd(File motdFile) {
        this.motdFile = motdFile;
        loadMotd();
        FMLCommonHandler.instance().bus().register(this);
    }

    public void loadMotd() {
        if (!motdFile.exists()) {
            try {
                for (String line : Strings.MOTD_DEFAULT) {
                    Files.append(line + Reference.LINE_SEPARATOR, motdFile, Reference.CHARSET);
                }
            } catch (IOException e) {
                log.error("Failed to save default MOTD to file: " + motdFile, e);
            }
            Collections.addAll(motd, Strings.MOTD_DEFAULT);
        } else {
            try {
                motd = Files.readLines(motdFile, Reference.CHARSET);
            } catch (IOException e) {
                log.error("Failed to read MOTD from file: " + motdFile, e);
            }
        }
    }

    public void serveMotd(EntityPlayer player) {
        for (String line : motd) {
            line = line.replace("$PLAYER$", player.getDisplayNameString());
            for (final EnumChatFormatting formatting : EnumChatFormatting.values()) {
                line = line.replace('$' + formatting.getFriendlyName().toUpperCase() + '$', formatting.toString());
            }
            player.addChatComponentMessage(ChatUtils.getChatComponent(line));
        }
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (STConfig.settings().ENABLE_MOTD_LOGIN) {
            serveMotd(event.player);
        }
    }
}
