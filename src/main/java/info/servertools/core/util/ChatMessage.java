package info.servertools.core.util;

import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import java.util.Deque;
import java.util.LinkedList;

/**
 * Utility to create formatted chat components in a clean way
 */
public final class ChatMessage {

    /**
     * Get a new {@link Builder} instance
     *
     * @return The builder
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final Deque<IChatComponent> components = new LinkedList<>();
        private ChatStyle chatStyle = new ChatStyle();

        private Builder() {}

        /**
         * Add a message to the builder, using the pre-set chat style
         *
         * @param message The message
         *
         * @return {@code this}
         */
        public Builder add(String message) {
            final ChatComponentText componentText = new ChatComponentText(message);
            componentText.setChatStyle(chatStyle);
            chatStyle = new ChatStyle();
            components.add(componentText);
            return this;
        }

        /**
         * Set the color of the following messages. Doesn't affect existing messages
         *
         * @param color The {@link EnumChatFormatting color}
         *
         * @return {@code this}
         */
        public Builder color(EnumChatFormatting color) {
            chatStyle.setColor(color);
            return this;
        }

        /**
         * Set if the following messages are bold. Doesn't affect existing messages
         *
         * @param value {@code true} for bold, {@code false} for not
         *
         * @return {@code this}
         */
        public Builder bold(boolean value) {
            chatStyle.setBold(value);
            return this;
        }

        /**
         * Set if the following messages are italic. Doesn't affect existing messages
         *
         * @param value {@code true} for italic, {@code false} for not
         *
         * @return {@code this}
         */
        public Builder italic(boolean value) {
            chatStyle.setItalic(value);
            return this;
        }

        /**
         * Set if the following messages are underlined. Doesn't affect existing messages
         *
         * @param value {@code true} for underlined, {@code false} for not
         *
         * @return {@code this}
         */
        public Builder underline(boolean value) {
            chatStyle.setUnderlined(value);
            return this;
        }

        /**
         * Set if the following messages are striked thourh. Doesn't affect existing messages
         *
         * @param value {@code true} for striked through, {@code false} for not
         *
         * @return {@code this}
         */
        public Builder strikethrough(boolean value) {
            chatStyle.setStrikethrough(value);
            return this;
        }

        /**
         * Set if the following messages are obfuscated (unreadable). Doesn't affect existing messages
         *
         * @param value {@code true} for obfuscated, {@code false} for not
         *
         * @return {@code this}
         */
        public Builder obfuscated(boolean value) {
            chatStyle.setObfuscated(value);
            return this;
        }

        /**
         * Build into an {@link IChatComponent}
         *
         * @return The {@link IChatComponent}
         */
        public IChatComponent build() {
            final IChatComponent root = new ChatComponentText("");
            for (IChatComponent component : components) {
                root.appendSibling(component);
            }
            return root;
        }
    }

    private ChatMessage() {}
}
