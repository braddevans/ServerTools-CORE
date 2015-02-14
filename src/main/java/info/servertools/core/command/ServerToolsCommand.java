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
package info.servertools.core.command;

import static com.google.common.base.Preconditions.checkNotNull;
import static info.servertools.core.util.ServerUtils.isOP;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

/**
 * The ServerTools implementation of the Minecraft {@link net.minecraft.command.ICommand command}
 *
 * <p>
 * Commands extending this class can have their name changed via a user-configurable config file. The command can also be disabled from the file.
 * </p>
 */
public abstract class ServerToolsCommand extends CommandBase {

    /**
     * The default name of this command
     */
    protected final String defaultName;

    /**
     * The registered name of this command. Will be {@link #defaultName} unless configuration is changed
     */
    protected String name;

    /**
     * Construct a new instance with the provided {@link #defaultName}
     *
     * @param defaultName The default name
     */
    public ServerToolsCommand(final String defaultName) {
        this.defaultName = checkNotNull(defaultName, "defaultName");
    }

    /**
     * Get the registered name of this command
     *
     * @return The registered name
     */
    @Override
    public final String getCommandName() {
        return name;
    }

    /**
     * Get the required access level to use this command
     *
     * @return the Access Level
     */
    public abstract CommandLevel getCommandLevel();

    @Override
    public boolean canCommandSenderUseCommand(final ICommandSender sender) {
        if (sender instanceof EntityPlayerMP) {
            final EntityPlayerMP player = (EntityPlayerMP) sender;
            final CommandLevel requiredLevel = getCommandLevel();
            return CommandLevel.ANYONE.equals(requiredLevel)
                   || CommandLevel.OP.equals(requiredLevel) && isOP(player.getGameProfile())
                   || MinecraftServer.getServer().getConfigurationManager().canSendCommands(player.getGameProfile());
        } else {
            return true;
        }
    }

    @Override
    public int getRequiredPermissionLevel() {
        switch (getCommandLevel()) {
            case ANYONE:
                return 0;
            case OP:
            default:
                return 4;
        }
    }

    /**
     * Set the name of this command. <b>This will only take effect before the command is registered with Minecraft</b>
     *
     * @param name The new registered name
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * The registered name of this command. Will be {@link #defaultName} unless configuration is changed
     *
     * @return The default name
     */
    public String getDefaultName() {
        return defaultName;
    }
}
