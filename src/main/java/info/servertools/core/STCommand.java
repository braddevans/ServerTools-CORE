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

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public abstract class STCommand extends CommandBase {

    private final String defaultName;
    private String name;

    public STCommand(final String defaultName) {
        this.defaultName = requireNonNull(defaultName, "defaultName");
    }

    public String getDefaultName() {
        return this.defaultName;
    }

    /**
     * <em>Internal Use Only!</em>
     *
     * @param name Name
     */
    void setName(final String name) {
        this.name = name;
    }

    @Override
    public final String getCommandName() {
        return this.name;
    }

    @Override
    public String toString() {
        return "STCommand{" +
                "defaultName='" + defaultName + '\'' +
                ", name='" + name + '\'' +
                ", class='" + getClass().getName() + '\'' +
                '}';
    }

    /* ---------- Utilities ---------- */

    public static EntityPlayerMP requirePlayer(final ICommandSender sender) throws CommandException {
        return requirePlayer(sender, "That command can only be used by a player");
    }

    public static EntityPlayerMP requirePlayer(final ICommandSender sender, final String message) throws CommandException {
        Objects.requireNonNull(sender, "sender");
        Objects.requireNonNull(message, "message");
        if (sender instanceof EntityPlayerMP) {
            return (EntityPlayerMP) sender;
        } else {
            throw new CommandException(message);
        }
    }
}
