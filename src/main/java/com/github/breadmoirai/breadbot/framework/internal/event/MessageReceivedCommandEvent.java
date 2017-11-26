/*
 *       Copyright 2017 Ton Ly (BreadMoirai)
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
package com.github.breadmoirai.breadbot.framework.internal.event;

import com.github.breadmoirai.breadbot.framework.BreadBotClient;
import com.github.breadmoirai.breadbot.framework.CommandEvent;
import com.github.breadmoirai.breadbot.framework.response.simple.EmbedResponse;
import com.github.breadmoirai.breadbot.framework.response.simple.MessageResponse;
import com.github.breadmoirai.breadbot.framework.response.simple.ReactionResponse;
import com.github.breadmoirai.breadbot.framework.response.simple.StringResponse;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.guild.GenericGuildMessageEvent;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MessageReceivedCommandEvent extends CommandEvent {

    private GenericGuildMessageEvent event;
    private Message message;
    private String prefix;
    private String key;
    private String content;

    MessageReceivedCommandEvent(BreadBotClient client, GenericGuildMessageEvent event, Message message, String prefix, String key, String content, boolean isHelpEvent) {
        super(event.getJDA(), event.getResponseNumber(), client, isHelpEvent);
        this.event = event;
        this.message = message;
        this.prefix = prefix;
        this.key = key;
        this.content = content;
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public Message getMessage() {
        return message;
    }

    @Override
    public User getAuthor() {
        return getMessage().getAuthor();
    }

    @Override
    public long getAuthorId() {
        return getAuthor().getIdLong();
    }

    @Override
    public Member getMember() {
        return getGuild().getMember(getAuthor());
    }

    @Override
    public SelfUser getSelfUser() {
        return getJDA().getSelfUser();
    }

    @Override
    public Member getSelfMember() {
        return getGuild().getMember(getSelfUser());
    }

    @Override
    public long getMessageId() {
        return getMessage().getIdLong();
    }

    @Override
    public Guild getGuild() {
        return event.getGuild();
    }

    @Override
    public long getGuildId() {
        return getGuild().getIdLong();
    }

    @Override
    public TextChannel getChannel() {
        return event.getChannel();
    }

    @Override
    public long getChannelId() {
        return getChannel().getIdLong();
    }

    @Override
    public OffsetDateTime getTime() {
        return getMessage().isEdited() ? getMessage().getEditedTime() : getMessage().getCreationTime();
    }

    @Override
    public Instant getInstant() {
        return getTime().toInstant();
    }

    @Override
    public JDA getJDA() {
        return event.getJDA();
    }

    @Override
    public List<User> getMentionedUsers() {
        return message.getMentionedUsers();
    }

    @Override
    public List<Role> getMentionedRoles() {
        return message.getMentionedRoles();
    }

    @Override
    public List<TextChannel> getMentionedChannels() {
        return message.getMentionedChannels();
    }

    @Override
    public List<Member> getMentionedMembers() {
        return message.getMentionedUsers().stream().map(getGuild()::getMember).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public void reply(String message) {
        replyWith(new StringResponse(message));
    }

    @Override
    public void reply(MessageEmbed message) {
        replyWith(new EmbedResponse(message));
    }

    @Override
    public void reply(Message message) {
        replyWith(new MessageResponse(message));
    }

    @Override
    public void replyReaction(Emote emote) {
        replyWith(new ReactionResponse(getMessageId(), emote));
    }

    @Override
    public void replyReaction(String emoji) {
        replyWith(new ReactionResponse(getMessageId(), emoji));
    }

//    @Override
//    public CommandEvent serialize() {
//        return new SerializableCommandEvent(this);
//    }


}
