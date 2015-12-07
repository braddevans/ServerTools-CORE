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
package info.servertools.core.feature;

import info.servertools.core.Constants;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Motd {

    private final Path file;
    private List<String> lines = new ArrayList<>();

    public Motd(final Path file) throws IOException {
        this.file = file;
        if (!Files.exists(file.getParent())) {
            Files.createDirectories(file.getParent());
        }
        load();
        save();
    }

    private void load() throws IOException {
        if (!Files.exists(file)) {
            genDefaultMotd();
        } else {
            lines = Files.readAllLines(file, Constants.CHARSET);
        }
    }

    private void save() throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(file, Constants.CHARSET)) {
            for (String line : lines) {
                writer.write(line + "\r\n");
            }
        }
    }

    private void genDefaultMotd() {
        final List<String> list = new ArrayList<>();
        list.add("This is the default ServerTools MOTD.");
        list.add("To change it, edit the motd.txt in the ");
        list.add("ServerTools-CORE configuration directory");
        this.lines = list;
    }

    @SubscribeEvent
    public void onPlayerLogin(final PlayerEvent.PlayerLoggedInEvent event) {
        serveMotd(event.player);
    }

    public void serveMotd(final ICommandSender sender) {
        for (String line : lines) {
            sender.addChatMessage(new ChatComponentText(line));
        }
    }
}
