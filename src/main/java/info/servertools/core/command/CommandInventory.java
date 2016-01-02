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

import static java.util.Objects.requireNonNull;

import info.servertools.core.util.ServerUtils;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

import java.util.List;

import javax.annotation.Nullable;

@Command(
        name = "inventory",
        opRequired = true
)
public class CommandInventory extends STCommand {

    @Override
    public String getCommandUsage(final ICommandSender sender) {
        return "/" + getCommandName() + " <player>";
    }

    @Nullable
    @Override
    public List<String> addTabCompletionOptions(final ICommandSender sender, final String[] args, final BlockPos pos) {
        return args.length <= 1 ? getListOfStringsMatchingLastWord(args, ServerUtils.getAllUsernames()) : null;
    }

    @Override
    public boolean isUsernameIndex(final String[] args, final int index) {
        return index == 0;
    }

    @Override
    public void processCommand(final ICommandSender sender, final String[] args) throws CommandException {
        if (args.length == 1) {
            final EntityPlayerMP targetPlayer = getPlayer(sender, args[0]);
            final EntityPlayerMP sourcePlayer = requirePlayer(sender);

            sourcePlayer.displayGUIChest(new PlayerInventoryWrapper(targetPlayer));

        } else {
            throw new WrongUsageException(getCommandUsage(sender));
        }
    }

    public static class PlayerInventoryWrapper implements IInventory {

        private final EntityPlayerMP target;

        public PlayerInventoryWrapper(final EntityPlayerMP target) {
            this.target = requireNonNull(target, "null target");
        }

        @Override
        public int getSizeInventory() {
            return target.inventory.mainInventory.length;
        }

        @Override
        @Nullable
        public ItemStack getStackInSlot(final int index) {
            return target.inventory.mainInventory[index];
        }

        @Override
        @Nullable
        public ItemStack decrStackSize(final int index, final int count) {
            ItemStack[] mainInventory = target.inventory.mainInventory;

            if (mainInventory[index] != null) {
                ItemStack itemstack;

                if (mainInventory[index].stackSize <= count) {
                    itemstack = mainInventory[index];
                    mainInventory[index] = null;
                    return itemstack;
                } else {
                    itemstack = mainInventory[index].splitStack(count);

                    if (mainInventory[index].stackSize == 0) {
                        mainInventory[index] = null;
                    }

                    return itemstack;
                }
            } else {
                return null;
            }
        }

        @Override
        @Nullable
        public ItemStack removeStackFromSlot(final int index) {
            ItemStack[] mainInventory = target.inventory.mainInventory;
            if (mainInventory[index] != null) {
                ItemStack itemstack = mainInventory[index];
                mainInventory[index] = null;
                return itemstack;
            } else {
                return null;
            }
        }

        @Override
        public void setInventorySlotContents(final int index, final ItemStack stack) {
            ItemStack[] mainInventory = target.inventory.mainInventory;
            mainInventory[index] = stack;
        }

        @Override
        public int getInventoryStackLimit() {
            return 64;
        }

        @Override
        public void markDirty() {}

        @Override
        public boolean isUseableByPlayer(final EntityPlayer player) {
            return !player.isDead;
        }

        @Override
        public void openInventory(final EntityPlayer player) {}

        @Override
        public void closeInventory(final EntityPlayer player) {}

        @Override
        public boolean isItemValidForSlot(final int index, final ItemStack stack) {
            return true;
        }

        @Override
        public int getField(final int id) {
            return 0;
        }

        @Override
        public void setField(final int id, final int value) {}

        @Override
        public int getFieldCount() {
            return 0;
        }

        @Override
        public void clear() {
            for (int id = 0; id < target.inventory.mainInventory.length; id++) {
                target.inventory.mainInventory[id] = null;
            }
            for (int id = 0; id < target.inventory.armorInventory.length; id++) {
                target.inventory.armorInventory[id] = null;
            }
        }

        @Override
        public String getName() {
            return target.getName() + "'s Inventory";
        }

        @Override
        public boolean hasCustomName() {
            return false;
        }

        @Override
        public IChatComponent getDisplayName() {
            return new ChatComponentText(getName());
        }
    }
}
