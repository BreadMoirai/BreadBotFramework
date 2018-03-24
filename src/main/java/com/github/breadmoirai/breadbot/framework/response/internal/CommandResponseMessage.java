/*
 *        Copyright 2017-2018 Ton Ly (BreadMoirai)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.github.breadmoirai.breadbot.framework.response.internal;

import com.github.breadmoirai.breadbot.framework.response.InternalCommandResponse;
import com.github.breadmoirai.breadbot.framework.response.RestActionExtension;
import net.dv8tion.jda.core.entities.IMentionable;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.utils.Checks;

import java.awt.*;
import java.io.File;
import java.io.InputStream;
import java.time.Instant;
import java.time.temporal.TemporalAccessor;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.LongConsumer;

public class CommandResponseMessage implements InternalCommandResponse {

    private TextChannel channel;
    private Message message;
    private RMessageBuilder builder;
    private FileSender file;
    private long delay;
    private TimeUnit unit;
    private Consumer<Message> success = m -> {
    };
    private Consumer<Throwable> failure;

    public CommandResponseMessage(TextChannel channel) {
        this.channel = channel;
    }

    public CommandResponseMessage(TextChannel channel, Message message) {
        this.channel = channel;
        this.message = message;
    }

    @Override
    public void dispatch(LongConsumer linkReceiver) {
        if (!builder.mustSplit()) {
            if (message == null) {
                message = builder.buildMessage();
            }
            final RestAction<Message> restAction;
            if (file != null) {
                restAction = file.sendFile(channel, message);
            } else {
                restAction = channel.sendMessage(message);
            }
            if (delay > 0)
                restAction.queueAfter(delay, unit, success.andThen(m -> linkReceiver.accept(m.getIdLong())), failure);
            else {
                restAction.queue(success.andThen(m -> linkReceiver.accept(m.getIdLong())), failure);
            }
        } else {
            final Queue<Message> messages = builder.buildMessages();
            while (!messages.isEmpty()) {
                final Message poll = messages.poll();
                if (!messages.isEmpty()) {
                    if (delay > 0)
                        channel.sendMessage(poll).queueAfter(delay, unit, m -> linkReceiver.accept(m.getIdLong()), failure);
                    else
                        channel.sendMessage(poll).queue(m -> linkReceiver.accept(m.getIdLong()), failure);
                } else {
                    final RestAction<Message> restAction;
                    if (file != null) {
                        restAction = file.sendFile(channel, poll);
                    } else {
                        restAction = channel.sendMessage(poll);
                    }
                    if (delay > 0)
                        restAction.queueAfter(delay, unit, success.andThen(m -> linkReceiver.accept(m.getIdLong())), failure);
                    else {
                        restAction.queue(success.andThen(m -> linkReceiver.accept(m.getIdLong())), failure);
                    }
                }
            }
        }
    }

    public RMessageBuilder builder() {
        if (builder == null) {
            builder = new RMessageBuilder();
        }
        return builder;
    }

    public abstract class ResponseMessageBuilder implements RestActionExtension<Message> {

        /**
         * Attaches a file to this message.
         *
         * @param file the file to upload
         * @return this instance
         * @throws IllegalArgumentException        <ul>
         *                                         <li>Provided {@code file} is null.</li>
         *                                         <li>Provided {@code file} does not exist.</li>
         *                                         <li>Provided {@code file} is unreadable.</li>
         *                                         <li>Provided {@code file} is greater than 8 MiB on a normal or 50 MiB on a nitro account.</li>
         *                                         <li>Provided {@link net.dv8tion.jda.core.entities.Message Message} is not {@code null} <b>and</b>
         *                                         contains a {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbed} which
         *                                         is not {@link net.dv8tion.jda.core.entities.MessageEmbed#isSendable(net.dv8tion.jda.core.AccountType) sendable}</li>
         *                                         </ul>
         * @throws InsufficientPermissionException If the logged in account does not have
         *                                         <ul>
         *                                         <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}</li>
         *                                         <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_WRITE Permission.MESSAGE_WRITE}</li>
         *                                         <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_ATTACH_FILES Permission.MESSAGE_ATTACH_FILES}</li>
         *                                         </ul>
         * @see MessageChannel#sendFile(File, Message)
         */
        public ResponseMessageBuilder upload(File file) {
            Checks.notNull(file, "file");
            return upload(file, file.getName());
        }

        /**
         * Attaches a fle to this message with the specified filename.
         * The filename must specify the type of file in the extension.
         * The filename may also be provided as the file type such as "png" or "jpeg" and discord will generate a name.
         *
         * @param file     the file to upload
         * @param fileName the name of the file for discord
         * @return this instance
         * @throws IllegalArgumentException        <ul>
         *                                         <li>Provided {@code file} is null.</li>
         *                                         <li>Provided {@code file} does not exist.</li>
         *                                         <li>Provided {@code file} is unreadable.</li>
         *                                         <li>Provided {@code file} is greater than 8 MiB on a normal or 50 MiB on a nitro account.</li>
         *                                         <li>Provided {@link net.dv8tion.jda.core.entities.Message Message} is not {@code null} <b>and</b>
         *                                         contains a {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbed} which
         *                                         is not {@link net.dv8tion.jda.core.entities.MessageEmbed#isSendable(net.dv8tion.jda.core.AccountType) sendable}</li>
         *                                         </ul>
         * @throws InsufficientPermissionException If the logged in account does not have
         *                                         <ul>
         *                                         <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}</li>
         *                                         <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_WRITE Permission.MESSAGE_WRITE}</li>
         *                                         <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_ATTACH_FILES Permission.MESSAGE_ATTACH_FILES}</li>
         *                                         </ul>
         * @see MessageChannel#sendFile(File, String, Message)
         */
        public ResponseMessageBuilder upload(File file, String fileName) {
            Checks.notNull(file, "file");
            Checks.check(file.exists() && file.canRead(),
                    "Provided file is either null, doesn't exist or is not readable!");
            Checks.check(file.length() <= channel.getJDA().getSelfUser().getAllowedFileSize(),
                    "File is to big! Max file-size is 8 MiB for normal and 50 MiB for nitro users");
            Checks.notNull(fileName, "fileName");

            CommandResponseMessage.this.file = new FileFileSender(file, fileName);
            return this;
        }

        /**
         * Attaches a file to this message.
         *
         * @param data     the data to represent the file
         * @param fileName the name of the file for discord
         * @return this instance
         * @see MessageChannel#sendFile(byte[], String, Message)
         * @see #upload(File, String)
         */
        public ResponseMessageBuilder upload(byte[] data, String fileName) {
            Checks.notNull(data, "data");
            CommandResponseMessage.this.file = new DataFileSender(data, fileName);
            return this;
        }

        /**
         * Attaches a file to this message.
         *
         * @param inputStream the inputStream containing the file contents
         * @param fileName    the name of the file for discord
         * @return this instance
         * @see MessageChannel#sendFile(InputStream, String, Message)
         * @see #upload(File, String)
         */
        public ResponseMessageBuilder upload(InputStream inputStream, String fileName) {
            Checks.notNull(inputStream, "inputStream");
            CommandResponseMessage.this.file = new StreamFileSender(inputStream, fileName);
            return this;
        }


        @Override
        public ResponseMessageBuilder after(long delay, TimeUnit unit) {
            Checks.notNull(unit, "TimeUnit");
            Checks.positive(delay, "delay");
            CommandResponseMessage.this.delay = delay;
            CommandResponseMessage.this.unit = unit;
            return this;
        }

        @Override
        public ResponseMessageBuilder onSuccess(Consumer<Message> success) {
            CommandResponseMessage.this.success = success;
            return this;
        }

        @Override
        public ResponseMessageBuilder onFailure(Consumer<Throwable> failure) {
            CommandResponseMessage.this.failure = failure;
            return this;
        }

        @Override
        public ResponseMessageBuilder appendSuccess(Consumer<Message> success) {
            if (CommandResponseMessage.this.success == null) {
                return onSuccess(success);
            } else {
                return onSuccess(CommandResponseMessage.this.success.andThen(success));
            }
        }

        @Override
        public ResponseMessageBuilder appendFailure(Consumer<Throwable> failure) {
            if (CommandResponseMessage.this.failure == null) {
                return onFailure(RestAction.DEFAULT_FAILURE.andThen(failure));
            } else {
                return onFailure(CommandResponseMessage.this.failure.andThen(failure));
            }
        }

    }

    /**
     * Wraps around a MessageBuilder. Is not required to build.
     * Will automatically send once the command finishes execution.
     */
    public class RMessageBuilder extends ResponseMessageBuilder implements Appendable {

        private net.dv8tion.jda.core.MessageBuilder builder;
        private net.dv8tion.jda.core.MessageBuilder.SplitPolicy[] splitPolicy;
        private REmbedBuilder embed;

        private RMessageBuilder() {
            builder = new net.dv8tion.jda.core.MessageBuilder();
        }

        public RMessageBuilder setTargetChannel(TextChannel channel) {
            CommandResponseMessage.this.channel = channel;
            return this;
        }

        /**
         * Creates a new Embed Builder for this message, overriding any existing embeds.
         *
         * @return an wrapper around an EmbedBuilder
         */
        public REmbedBuilder embed() {
            this.embed = CommandResponseMessage.this.new REmbedBuilder();
            return embed;
        }

        @Override
        public RMessageBuilder upload(File file) {
            super.upload(file);
            return this;
        }

        @Override
        public RMessageBuilder upload(File file, String fileName) {
            super.upload(file, fileName);
            return this;
        }

        @Override
        public RMessageBuilder upload(byte[] data, String fileName) {
            super.upload(data, fileName);
            return this;
        }

        @Override
        public RMessageBuilder upload(InputStream inputStream, String fileName) {
            super.upload(inputStream, fileName);
            return this;
        }

        @Override
        public RMessageBuilder after(long delay, TimeUnit unit) {
            super.after(delay, unit);
            return this;
        }

        @Override
        public RMessageBuilder onSuccess(Consumer<Message> success) {
            super.onSuccess(success);
            return this;
        }

        @Override
        public RMessageBuilder onFailure(Consumer<Throwable> failure) {
            super.onFailure(failure);
            return this;
        }

        @Override
        public RMessageBuilder appendSuccess(Consumer<Message> success) {
            super.appendSuccess(success);
            return this;
        }

        @Override
        public RMessageBuilder appendFailure(Consumer<Throwable> failure) {
            super.appendFailure(failure);
            return this;
        }

        /**
         * Makes the created Message a TTS message.
         * <br>TTS stands for Text-To-Speech. When a TTS method is received by the Discord client,
         * it is vocalized so long as the user has not disabled TTS.
         *
         * @param tts whether the created Message should be a tts message
         * @return This instance.
         */
        public RMessageBuilder setTTS(boolean tts) {
            builder.setTTS(tts);
            return this;
        }

        /**
         * Adds a {@link MessageEmbed} to the Message. Embeds can be built using
         * the {@link net.dv8tion.jda.core.EmbedBuilder} and offer specialized formatting.
         *
         * @param embed the embed to add, or null to remove
         * @return This instance.
         */
        public RMessageBuilder setEmbed(MessageEmbed embed) {
            builder.setEmbed(embed);
            return this;
        }

        /**
         * Appends a String to the Message.
         *
         * @param text the text to append
         * @return This instance.
         */
        public RMessageBuilder append(CharSequence text) {
            builder.append(text);
            return this;
        }

        @Override
        public RMessageBuilder append(CharSequence text, int start, int end) {
            builder.append(text, start, end);
            return this;
        }

        @Override
        public RMessageBuilder append(char c) {
            builder.append(c);
            return this;
        }

        /**
         * Appends the string representation of an object to the Message.
         * <br>This is the same as {@link #append(CharSequence) append(String.valueOf(object))}
         *
         * @param object the object to append
         * @return This instance.
         */
        public RMessageBuilder append(Object object) {
            builder.append(object);
            return this;
        }

        /**
         * Appends a mention to the Message.
         * <br>Typical usage would be providing an {@link IMentionable IMentionable} like
         * {@link User User} or {@link TextChannel TextChannel}.
         *
         * @param mention the mention to append
         * @return The {@link net.dv8tion.jda.core.MessageBuilder MessageBuilder} instance. Useful for chaining.
         */
        public RMessageBuilder append(IMentionable mention) {
            builder.append(mention);
            return this;
        }

        /**
         * Appends a String using the specified chat {@link net.dv8tion.jda.core.MessageBuilder.Formatting Formatting(s)}.
         *
         * @param text   the text to append.
         * @param format the format(s) to apply to the text.
         * @return This instance.
         */
        public RMessageBuilder append(CharSequence text, net.dv8tion.jda.core.MessageBuilder.Formatting... format) {
            builder.append(text, format);
            return this;
        }

        /**
         * This method is an extended form of {@link String#format(String, Object...)}. It allows for all of
         * the token replacement functionality that String.format(String, Object...) supports.
         * <br>A lot of JDA entities implement {@link java.util.Formattable Formattable} and will provide
         * specific format outputs for their specific type.
         * <ul>
         * <li>{@link net.dv8tion.jda.core.entities.IMentionable IMentionable}
         * <br>These will output their {@link net.dv8tion.jda.core.entities.IMentionable#getAsMention() getAsMention} by default,
         * some implementations have alternatives such as {@link net.dv8tion.jda.core.entities.User User} and {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}.</li>
         * <li>{@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel}
         * <br>All message channels format to {@code "#" + getName()} by default, TextChannel has special handling
         * and uses the getAsMention output by default and the MessageChannel output as alternative ({@code #} flag).</li>
         * <li>{@link net.dv8tion.jda.core.entities.Message Message}
         * <br>Messages by default output their {@link net.dv8tion.jda.core.entities.Message#getContentDisplay() getContentDisplay()} value and
         * as alternative use the {@link net.dv8tion.jda.core.entities.Message#getContentRaw() getContentRaw()} value</li>
         * </ul>
         *
         * <p>Example:
         * <br>If you placed the following code in an method handling a
         * {@link net.dv8tion.jda.core.events.message.MessageReceivedEvent MessageReceivedEvent}
         * <br><pre>{@code
         * User user = event.getAuthor();
         * MessageBuilder builder = new MessageBuilder();
         * builder.appendFormat("%#s is really cool!", user);
         * builder.build();
         * }</pre>
         * <p>
         * It would build a message that mentions the author and says that he is really cool!. If the user's
         * name was "Minn" and his discriminator "6688", it would say:
         * <br><pre>  "Minn#6688 is really cool!"</pre>
         * <br>Note that this uses the {@code #} flag to utilize the alternative format for {@link net.dv8tion.jda.core.entities.User User}.
         * <br>By default it would fallback to {@link net.dv8tion.jda.core.entities.IMentionable#getAsMention()}
         *
         * @param format a format string.
         * @param args   an array objects that will be used to replace the tokens, they must be
         *               provided in the order that the tokens appear in the provided format string.
         * @return The MessageBuilder instance. Useful for chaining.
         * @throws IllegalArgumentException         If the provided format string is {@code null} or empty
         * @throws java.util.IllegalFormatException If a format string contains an illegal syntax,
         *                                          a format specifier that is incompatible with the given arguments,
         *                                          insufficient arguments given the format string, or other illegal conditions.
         *                                          For specification of all possible formatting errors,
         *                                          see the <a href="../util/Formatter.html#detail">Details</a>
         *                                          section of the formatter class specification.
         */
        public RMessageBuilder appendFormat(String format, Object... args) {
            builder.appendFormat(format, args);
            return this;
        }

        /**
         * Appends a code-block to the Message.
         * <br>Discord uses <a href="https://highlightjs.org/">Highlight.js</a> for its language highlighting support. You can find out what
         * specific languages are supported <a href="https://github.com/isagalaev/highlight.js/tree/master/src/languages">here</a>.
         *
         * @param text     the code to append
         * @param language the language of the code. If unknown use an empty string
         * @return This instance.
         */
        public RMessageBuilder appendCodeBlock(CharSequence text, CharSequence language) {
            builder.appendCodeBlock(text, language);
            return this;
        }

        /**
         * This sets the SplitPolicies to be used in case this message exceeds 2000 characters.
         *
         * @param policies What splitpolicies to use in the order of their priority.
         * @return this instance
         * @see net.dv8tion.jda.core.MessageBuilder#buildAll(net.dv8tion.jda.core.MessageBuilder.SplitPolicy...)
         */
        public RMessageBuilder splitPolicy(net.dv8tion.jda.core.MessageBuilder.SplitPolicy... policies) {
            splitPolicy = policies;
            return this;
        }

        /**
         * Returns the current length of the content.
         * <br>If this value is {@code 0} (and there is no embed) an exception
         * will be raised as you cannot send an empty message to Discord.
         * <br>If this value is greater than 2000, multiple message will be sent as according to the set {@link net.dv8tion.jda.core.MessageBuilder.SplitPolicy SplitPolicies}.
         *
         * @return the current length of the content that will be built into a Message.
         */
        public int length() {
            return builder.length();
        }

        /**
         * Retrieves the underlying MessageBuilder.
         *
         * @return the underlying MessageBuilder
         */
        public net.dv8tion.jda.core.MessageBuilder getMessageBuilder() {
            return builder;
        }

        private boolean mustSplit() {
            return builder.length() > 2000;
        }

        private Message buildMessage() {
            if (embed != null)
                builder.setEmbed(embed.getEmbedBuilder().build()).build();
            return builder.build();
        }

        private Queue<Message> buildMessages() {
            if (embed != null)
                builder.setEmbed(embed.getEmbedBuilder().build());
            return builder.buildAll(splitPolicy);
        }


    }

    /**
     * A wrapper around an EmbedBuilder
     */
    public class REmbedBuilder extends ResponseMessageBuilder {
        private final net.dv8tion.jda.core.EmbedBuilder embed;

        private REmbedBuilder() {
            this.embed = new net.dv8tion.jda.core.EmbedBuilder();
        }

        public net.dv8tion.jda.core.EmbedBuilder getEmbedBuilder() {
            return embed;
        }

        @Override
        public REmbedBuilder upload(File file) {
            super.upload(file);
            return this;
        }

        @Override
        public REmbedBuilder upload(File file, String fileName) {
            super.upload(file, fileName);
            return this;
        }

        @Override
        public REmbedBuilder upload(byte[] data, String fileName) {
            super.upload(data, fileName);
            return this;
        }

        @Override
        public REmbedBuilder upload(InputStream inputStream, String fileName) {
            super.upload(inputStream, fileName);
            return this;
        }

        @Override
        public REmbedBuilder after(long delay, TimeUnit unit) {
            super.after(delay, unit);
            return this;
        }

        @Override
        public REmbedBuilder onSuccess(Consumer<Message> success) {
            super.onSuccess(success);
            return this;
        }

        @Override
        public REmbedBuilder onFailure(Consumer<Throwable> failure) {
            super.onFailure(failure);
            return this;
        }

        @Override
        public REmbedBuilder appendSuccess(Consumer<Message> success) {
            super.appendSuccess(success);
            return this;
        }

        @Override
        public REmbedBuilder appendFailure(Consumer<Throwable> failure) {
            super.appendFailure(failure);
            return this;
        }

        /**
         * Sets the Title of the embed.
         * <br>Overload for {@link #title(String, String)} without URL parameter.
         *
         * <p><b><a href="http://i.imgur.com/JgZtxIM.png">Example</a></b>
         *
         * @param title the title of the embed
         * @return this instance
         * @throws IllegalArgumentException <ul>
         *                                  <li>If the provided {@code title} is an empty String.</li>
         *                                  <li>If the length of {@code title} is greater than {@link MessageEmbed#TITLE_MAX_LENGTH}.</li>
         *                                  </ul>
         */
        public REmbedBuilder title(String title) {
            embed.setTitle(title);
            return this;
        }

        /**
         * Sets the Title of the embed.
         * <br>You can provide {@code null} as url if no url should be used.
         *
         * <p><b><a href="http://i.imgur.com/JgZtxIM.png">Example</a></b>
         *
         * @param title the title of the embed
         * @param url   Makes the title into a hyperlink pointed at this url.
         * @return the builder after the title has been set
         * @throws IllegalArgumentException <ul>
         *                                  <li>If the provided {@code title} is an empty String.</li>
         *                                  <li>If the length of {@code title} is greater than {@link MessageEmbed#TITLE_MAX_LENGTH}.</li>
         *                                  <li>If the length of {@code url} is longer than {@link MessageEmbed#URL_MAX_LENGTH}.</li>
         *                                  <li>If the provided {@code url} is not a properly formatted http or https url.</li>
         *                                  </ul>
         */
        public REmbedBuilder title(String title, String url) {
            embed.setTitle(title);
            return this;
        }

        /**
         * Sets the Description of the embed. This is where the main chunk of text for an embed is typically placed.
         *
         * <p><b><a href="http://i.imgur.com/lbchtwk.png">Example</a></b>
         *
         * @param description the description of the embed, {@code null} to reset
         * @return the builder after the description has been set
         * @throws IllegalArgumentException If the length of {@code description} is greater than {@link MessageEmbed#TEXT_MAX_LENGTH}
         */
        public REmbedBuilder description(CharSequence description) {
            embed.setDescription(description);
            return this;
        }

        /**
         * Appends to the description of the embed. This is where the main chunk of text for an embed is typically placed.
         *
         * <p><b><a href="http://i.imgur.com/lbchtwk.png">Example</a></b>
         *
         * @param description the string to append to the description of the embed
         * @return the builder after the description has been set
         * @throws IllegalArgumentException <ul>
         *                                  <li>If the provided {@code description} String is null</li>
         *                                  <li>If the length of {@code description} is greater than {@link MessageEmbed#TEXT_MAX_LENGTH}.</li>
         *                                  </ul>
         */
        public REmbedBuilder appendDescription(CharSequence description) {
            embed.appendDescription(description);
            return this;
        }

        /**
         * Sets the Timestamp of the embed.
         *
         * <p><b><a href="http://i.imgur.com/YP4NiER.png">Example</a></b>
         *
         * <p><b>Hint:</b> You can get the current time using {@link Instant#now() Instant.now()} or convert time from a
         * millisecond representation by using {@link Instant#ofEpochMilli(long) Instant.ofEpochMilli(long)};
         *
         * @param temporal the temporal accessor of the timestamp
         * @return the builder after the timestamp has been set
         */
        public REmbedBuilder timestamp(TemporalAccessor temporal) {
            embed.setTimestamp(temporal);
            return this;
        }

        /**
         * Sets the Color of the embed.
         * <p>
         * <a href="http://i.imgur.com/2YnxnRM.png" target="_blank">Example</a>
         *
         * @param color The {@link Color Color} of the embed
         *              or {@code null} to use no color
         * @return the builder after the color has been set
         */
        public REmbedBuilder color(Color color) {
            embed.setColor(color);
            return this;
        }

        /**
         * Sets the Thumbnail of the embed.
         *
         * <p><b><a href="http://i.imgur.com/Zc3qwqB.png">Example</a></b>
         *
         * <p><b>Uploading images with Embeds</b>
         * <br>When uploading an <u>image</u>
         * (using {@link MessageChannel#sendFile(File, Message) MessageChannel.sendFile(...)})
         * you can reference said image using the specified filename as URI {@code attachment://filename.ext}.
         *
         * <p><u>Example</u>
         * <pre><code>
         * MessageChannel channel; // = reference of a MessageChannel
         * MessageBuilder message = new MessageBuilder();
         * EmbedBuilder embed = new EmbedBuilder();
         * InputStream file = new URL("https://http.cat/500").openStream();
         * embed.setThumbnail("attachment://cat.png") // we specify this in sendFile as "cat.png"
         *      .setDescription("This is a cute cat :3");
         * message.setEmbed(embed.build());
         * channel.sendFile(file, "cat.png", message.build()).queue();
         * </code></pre>
         *
         * @param url the url of the thumbnail of the embed
         * @return the builder after the thumbnail has been set
         * @throws IllegalArgumentException <ul>
         *                                  <li>If the length of {@code url} is longer than {@link MessageEmbed#URL_MAX_LENGTH}.</li>
         *                                  <li>If the provided {@code url} is not a properly formatted http or https url.</li>
         *                                  </ul>
         */
        public REmbedBuilder thumbnail(String url) {
            embed.setThumbnail(url);
            return this;
        }

        /**
         * Sets the Image of the embed.
         *
         * <p><b><a href="http://i.imgur.com/2hzuHFJ.png">Example</a></b>
         *
         * <p><b>Uploading images with Embeds</b>
         * <br>When uploading an <u>image</u>
         * (using {@link MessageChannel#sendFile(File, Message) MessageChannel.sendFile(...)})
         * you can reference said image using the specified filename as URI {@code attachment://filename.ext}.
         *
         * <p><u>Example</u>
         * <pre><code>
         * MessageChannel channel; // = reference of a MessageChannel
         * MessageBuilder message = new MessageBuilder();
         * EmbedBuilder embed = new EmbedBuilder();
         * InputStream file = new URL("https://http.cat/500").openStream();
         * embed.setImage("attachment://cat.png") // we specify this in sendFile as "cat.png"
         *      .setDescription("This is a cute cat :3");
         * message.setEmbed(embed.build());
         * channel.sendFile(file, "cat.png", message.build()).queue();
         * </code></pre>
         *
         * @param url the url of the image of the embed
         * @return the builder after the image has been set
         * @throws IllegalArgumentException <ul>
         *                                  <li>If the length of {@code url} is longer than {@link MessageEmbed#URL_MAX_LENGTH}.</li>
         *                                  <li>If the provided {@code url} is not a properly formatted http or https url.</li>
         *                                  </ul>
         * @see MessageChannel#sendFile(File, String, Message) MessageChannel.sendFile(...)
         */
        public REmbedBuilder image(String url) {
            embed.setImage(url);
            return this;
        }

        /**
         * Sets the Author of the embed. The author appears in the top left of the embed and can have a small
         * image beside it along with the author's name being made clickable by way of providing a url.
         * This convenience method just sets the name.
         *
         * <p><b><a href="http://i.imgur.com/JgZtxIM.png">Example</a></b>
         *
         * @param name the name of the author of the embed. If this is not set, the author will not appear in the embed
         * @return the builder after the author has been set
         */
        public REmbedBuilder author(String name) {
            embed.setAuthor(name);
            return this;
        }

        /**
         * Sets the Author of the embed. The author appears in the top left of the embed and can have a small
         * image beside it along with the author's name being made clickable by way of providing a url.
         * This convenience method just sets the name and the url.
         *
         * <p><b><a href="http://i.imgur.com/JgZtxIM.png">Example</a></b>
         *
         * @param name the name of the author of the embed. If this is not set, the author will not appear in the embed
         * @param url  the url of the author of the embed
         * @return the builder after the author has been set
         * @throws IllegalArgumentException <ul>
         *                                  <li>If the length of {@code url} is longer than {@link MessageEmbed#URL_MAX_LENGTH}.</li>
         *                                  <li>If the provided {@code url} is not a properly formatted http or https url.</li>
         *                                  </ul>
         */
        public REmbedBuilder author(String name, String url) {
            embed.setAuthor(name, url);
            return this;
        }

        /**
         * Sets the Author of the embed. The author appears in the top left of the embed and can have a small
         * image beside it along with the author's name being made clickable by way of providing a url.
         *
         * <p><b><a href="http://i.imgur.com/JgZtxIM.png">Example</a></b>
         *
         * <p><b>Uploading images with Embeds</b>
         * <br>When uploading an <u>image</u>
         * (using {@link MessageChannel#sendFile(File, Message) MessageChannel.sendFile(...)})
         * you can reference said image using the specified filename as URI {@code attachment://filename.ext}.
         *
         * <p><u>Example</u>
         * <pre><code>
         * MessageChannel channel; // = reference of a MessageChannel
         * MessageBuilder message = new MessageBuilder();
         * EmbedBuilder embed = new EmbedBuilder();
         * InputStream file = new URL("https://http.cat/500").openStream();
         * embed.setAuthor("Minn", null, "attachment://cat.png") // we specify this in sendFile as "cat.png"
         *      .setDescription("This is a cute cat :3");
         * message.setEmbed(embed.build());
         * channel.sendFile(file, "cat.png", message.build()).queue();
         * </code></pre>
         *
         * @param name    the name of the author of the embed. If this is not set, the author will not appear in the embed
         * @param url     the url of the author of the embed
         * @param iconUrl the url of the icon for the author
         * @return the builder after the author has been set
         * @throws IllegalArgumentException <ul>
         *                                  <li>If the length of {@code url} is longer than {@link MessageEmbed#URL_MAX_LENGTH}.</li>
         *                                  <li>If the provided {@code url} is not a properly formatted http or https url.</li>
         *                                  <li>If the length of {@code iconUrl} is longer than {@link MessageEmbed#URL_MAX_LENGTH}.</li>
         *                                  <li>If the provided {@code iconUrl} is not a properly formatted http or https url.</li>
         *                                  </ul>
         */
        public REmbedBuilder author(String name, String url, String iconUrl) {
            embed.setAuthor(name, url, iconUrl);
            return this;
        }

        /**
         * Sets the Footer of the embed.
         *
         * <p><b><a href="http://i.imgur.com/jdf4sbi.png">Example</a></b>
         *
         * <p><b>Uploading images with Embeds</b>
         * <br>When uploading an <u>image</u>
         * (using {@link MessageChannel#sendFile(File, Message) MessageChannel.sendFile(...)})
         * you can reference said image using the specified filename as URI {@code attachment://filename.ext}.
         *
         * <p><u>Example</u>
         * <pre><code>
         * MessageChannel channel; // = reference of a MessageChannel
         * MessageBuilder message = new MessageBuilder();
         * EmbedBuilder embed = new EmbedBuilder();
         * InputStream file = new URL("https://http.cat/500").openStream();
         * embed.setFooter("Cool footer!", "attachment://cat.png") // we specify this in sendFile as "cat.png"
         *      .setDescription("This is a cute cat :3");
         * message.setEmbed(embed.build());
         * channel.sendFile(file, "cat.png", message.build()).queue();
         * </code></pre>
         *
         * @param text    the text of the footer of the embed. If this is not set, the footer will not appear in the embed.
         * @param iconUrl the url of the icon for the footer
         * @return the builder after the footer has been set
         * @throws IllegalArgumentException <ul>
         *                                  <li>If the length of {@code text} is longer than {@link MessageEmbed#TEXT_MAX_LENGTH}.</li>
         *                                  <li>If the length of {@code iconUrl} is longer than {@link MessageEmbed#URL_MAX_LENGTH}.</li>
         *                                  <li>If the provided {@code iconUrl} is not a properly formatted http or https url.</li>
         *                                  </ul>
         */
        public REmbedBuilder footer(String text, String iconUrl) {
            embed.setFooter(text, iconUrl);
            return this;
        }

        /**
         * Adds a Field to the embed.
         *
         * <p>Note: If a blank string is provided to either {@code name} or {@code value}, the blank string is replaced
         * with {@link net.dv8tion.jda.core.EmbedBuilder#ZERO_WIDTH_SPACE}.
         *
         * <p><b><a href="http://i.imgur.com/gnjzCoo.png">Example of Inline</a></b>
         * <p><b><a href="http://i.imgur.com/Ky0KlsT.png">Example if Non-inline</a></b>
         *
         * @param name   the name of the Field, displayed in bold above the {@code value}.
         * @param value  the contents of the field.
         * @param inline whether or not this field should display inline.
         * @return the builder after the field has been added
         * @throws IllegalArgumentException <ul>
         *                                  <li>If only {@code name} or {@code value} is set. Both must be set.</li>
         *                                  <li>If the length of {@code name} is greater than {@link MessageEmbed#TITLE_MAX_LENGTH}.</li>
         *                                  <li>If the length of {@code value} is greater than {@link MessageEmbed#VALUE_MAX_LENGTH}.</li>
         *                                  </ul>
         */
        public REmbedBuilder field(String name, String value, boolean inline) {
            embed.addField(name, value, inline);
            return this;
        }

        /**
         * Adds a blank (empty) Field to the embed.
         *
         * <p><b><a href="http://i.imgur.com/tB6tYWy.png">Example of Inline</a></b>
         * <p><b><a href="http://i.imgur.com/lQqgH3H.png">Example of Non-inline</a></b>
         *
         * @param inline whether or not this field should display inline
         * @return the builder after the field has been added
         */
        public REmbedBuilder blankField(boolean inline) {
            embed.addBlankField(inline);
            return this;
        }

        /**
         * Sets the Author of the embed. The author appears in the top left of the embed.
         * The image is set to the author's effective avater and the name is set to the username of the user.
         *
         * @param user the User to be set as the author.
         * @return this instance.
         */
        public REmbedBuilder author(User user) {
            return author(user.getName(), null, user.getEffectiveAvatarUrl());
        }

        /**
         * Sets the Author of the embed. The author appears in the top left of the embed.
         * The image is set to the author's effective avatar and the name is set to the username of the user.
         * The url allows the author to be clicked.
         *
         * @param user the User to be set as the author.
         * @param url  the URL of the author for the embed
         * @return this instance
         * @throws IllegalArgumentException <ul>
         *                                  <li>If the length of {@code url} is longer than {@link MessageEmbed#URL_MAX_LENGTH}.</li>
         *                                  <li>If the provided {@code url} is not a properly formatted http or https url.</li>
         *                                  </ul>
         */
        public REmbedBuilder author(User user, String url) {
            return author(user.getName(), url, user.getEffectiveAvatarUrl());
        }

        /**
         * Sets the Author of the embed. The author appears in the top left of the embed.
         * The image is set to the author's effective avatar and the name is set to the username of the member or their nickname if they have one.
         * The embed's color is also set to the member's visible color.
         *
         * @param member the Member to be set as the author.
         * @return this instance
         */
        public REmbedBuilder author(Member member) {
            author(member.getEffectiveName(), null, member.getUser().getEffectiveAvatarUrl());
            color(member.getColor());
            return this;
        }

        /**
         * Sets the Author of the embed. The author appears in the top left of the embed.
         * The image is set to the author's effective avatar and the name is set to the username of the member or their nickname if they have one.
         * The embed's color is also set to the member's visible color.
         *
         * @param member the Member to be set as the author.
         * @param url    the URL of the author for the embed
         * @return this instance
         * @throws IllegalArgumentException <ul>
         *                                  <li>If the length of {@code url} is longer than {@link MessageEmbed#URL_MAX_LENGTH}.</li>
         *                                  <li>If the provided {@code url} is not a properly formatted http or https url.</li>
         *                                  </ul>
         */
        public REmbedBuilder author(Member member, String url) {
            author(member.getEffectiveName(), url, member.getUser().getEffectiveAvatarUrl());
            color(member.getColor());
            return this;
        }

        /**
         * Returns the Builder representing the MessageBuilder
         *
         * @return the Builder.
         */
        public RMessageBuilder message() {
            if (embed.isEmpty())
                throw new IllegalStateException("An empty embed cannot be added to a message.");
            return CommandResponseMessage.this.builder;
        }

    }


    private abstract class FileSender {
        protected abstract RestAction<Message> sendFile(MessageChannel channel, Message message);
    }

    private class FileFileSender extends FileSender {
        final private String fileName;
        final private File file;

        public FileFileSender(File file, String fileName) {
            this.fileName = fileName;
            this.file = file;
        }

        @Override
        protected RestAction<Message> sendFile(MessageChannel channel, Message message) {
            return channel.sendFile(file, fileName, message);
        }
    }

    private class DataFileSender extends FileSender {
        final private String fileName;
        final private byte[] file;

        public DataFileSender(byte[] file, String fileName) {
            this.fileName = fileName;
            this.file = file;
        }

        @Override
        protected RestAction<Message> sendFile(MessageChannel channel, Message message) {
            return channel.sendFile(file, fileName, message);
        }
    }

    private class StreamFileSender extends FileSender {
        final private String fileName;
        final private InputStream file;

        public StreamFileSender(InputStream file, String fileName) {
            this.fileName = fileName;
            this.file = file;
        }

        @Override
        protected RestAction<Message> sendFile(MessageChannel channel, Message message) {
            return channel.sendFile(file, fileName, message);
        }
    }
}
