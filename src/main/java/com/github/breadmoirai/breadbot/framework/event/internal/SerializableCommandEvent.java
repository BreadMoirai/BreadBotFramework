///*
// *       Copyright 2017 Ton Ly (BreadMoirai)
// *
// *   Licensed under the Apache License, Version 2.0 (the "License");
// *   you may not use this file except in compliance with the License.
// *   You may obtain a copy of the License at
// *
// *       http://www.apache.org/licenses/LICENSE-2.0
// *
// *   Unless required by applicable law or agreed to in writing, software
// *   distributed under the License is distributed on an "AS IS" BASIS,
// *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// *   See the License for the specific language governing permissions and
// *   limitations under the License.
// */

//package com.github.breadmoirai.breadbot.framework.event.impl;
//
//import com.github.breadmoirai.breadbot.framework.event.CommandEvent;
//import net.dv8tion.jda.core.JDA;
//import net.dv8tion.jda.core.entities.*;
//
//import java.io.Serializable;
//import java.time.Instant;
//import java.time.OffsetDateTime;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Objects;
//import java.util.stream.Collectors;
//
//public class SerializableCommandEvent extends CommandEvent implements Serializable {
//
//    private static final long serialVersionUID = 1;
//
//    private transient JDA jda;
//
//    private String prefix;
//    private String key;
//    private String content;
//    private long authorId;
//    private long guildId;
//    private long channelId;
//    private long messageId;
//    private long[] mentionedUsers;
//    private long[] mentionedRoles;
//    private long[] mentionedChannels;
//    private String attachment;
//    private OffsetDateTime time;
//
//
//    /**
//     * public no-args constructor for serialization purposes only
//     */
//    public SerializableCommandEvent() {
//        super(null, 0, null);
//    }
//
//    SerializableCommandEvent(MessageReceivedCommandEvent commandEvent) {
//        super(commandEvent.getJDA(), 0, commandEvent.getClient());
//        prefix = commandEvent.getPrefix();
//        key = commandEvent.getKey();
//        authorId = commandEvent.getAuthorId();
//        content = commandEvent.getContent();
//        guildId = commandEvent.getTargetGuildId();
//        channelId = commandEvent.getTargetChannelId();
//        messageId = commandEvent.getMessageId();
//        mentionedUsers = commandEvent.getMentionedUsers().stream().mapToLong(ISnowflake::getIdLong).toArray();
//        mentionedRoles = commandEvent.getMentionedRoles().stream().mapToLong(ISnowflake::getIdLong).toArray();
//        mentionedChannels = commandEvent.getMentionedChannels().stream().mapToLong(ISnowflake::getIdLong).toArray();
//        time = commandEvent.getTime();
//    }
//
//    public void load(JDA jda) {
//        this.jda = jda;
//    }
//
//    @Override
//    public String getPrefix() {
//        return prefix;
//    }
//
//    @Override
//    public String getKey() {
//        return key;
//    }
//
//    @Override
//    public String getContent() {
//        return content;
//    }
//
//    @Override
//    public Message getMessage() {
//        return null;
//    }
//
//    @Override
//    public User getAuthor() {
//        return getJDA().getUserById(getAuthorId());
//    }
//
//    @Override
//    public long getAuthorId() {
//        return authorId;
//    }
//
//    @Override
//    public Member getMember() {
//        return getGuild().getMemberById(getAuthorId());
//    }
//
//    @Override
//    public SelfUser getSelfUser() {
//        return getJDA().getSelfUser();
//    }
//
//    @Override
//    public Member getSelfMember() {
//        return getGuild().getMember(getSelfUser());
//    }
//
//    @Override
//    public long getMessageId() {
//        return messageId;
//    }
//
//    @Override
//    public Guild getGuild() {
//        return getJDA().getGuildById(guildId);
//    }
//
//    @Override
//    public long getTargetGuildId() {
//        return guildId;
//    }
//
//    @Override
//    public TextChannel getChannel() {
//        return getJDA().getTextChannelById(channelId);
//    }
//
//    @Override
//    public long getTargetChannelId() {
//        return channelId;
//    }
//
//    @Override
//    public OffsetDateTime getTime() {
//        return time;
//    }
//
//    @Override
//    public Instant getInstant() {
//        return getTime().toInstant();
//    }
//
//    @Override
//    public JDA getJDA() {
//        return jda;
//    }
//
//    @Override
//    public List<User> getMentionedUsers() {
//        return Arrays.stream(mentionedUsers).mapToObj(id -> getJDA().getUserById(id)).filter(Objects::nonNull).collect(Collectors.toList());
//    }
//
//    @Override
//    public List<Role> getMentionedRoles() {
//        return null;
//    }
//
//    @Override
//    public List<TextChannel> getMentionedChannels() {
//        return null;
//    }
//
//    @Override
//    public List<Member> getMentionedMembers() {
//        return null;
//    }
//
//    @Override
//    public void reply(String message) {
//
//    }
//
//    @Override
//    public void reply(MessageEmbed message) {
//
//    }
//
//    @Override
//    public void reply(Message message) {
//
//    }
//
//    @Override
//    public void replyPrivate(String message) {
//
//    }
//
//    @Override
//    public void replyPrivate(MessageEmbed message) {
//
//    }
//
//    @Override
//    public void replyPrivate(Message message) {
//
//    }
//
//    @Override
//    public void replyReaction(Emote emote) {
//
//    }
//
//    @Override
//    public void replyReaction(String emoji) {
//
//    }
//
////    @Override
////    public CommandEvent serialize() {
////        return this;
////    }
//
//}