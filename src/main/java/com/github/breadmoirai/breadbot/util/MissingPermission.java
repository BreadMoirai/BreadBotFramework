package com.github.breadmoirai.breadbot.util;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;

public class MissingPermission {

    private static ResponseBuilder responseBuilder = new ResponseBuilderImpl();

    private MissingPermission() {

    }

    public static void setResponseBuilder(ResponseBuilder responseBuilder) {
        MissingPermission.responseBuilder = responseBuilder;
    }

    public static Message buildResponse(Member selfMember, GuildChannel channel, GuildChannel messageChannel, Permission... permissions) {
        return responseBuilder.buildResponse(selfMember, channel, messageChannel, permissions);
    }

    public static abstract class ResponseBuilder {
        public abstract Message buildResponse(Member selfMember, GuildChannel channel, GuildChannel messageChannel, Permission... permissions);
    }

    private static class ResponseBuilderImpl extends ResponseBuilder {

        @Override
        public Message buildResponse(Member selfMember, GuildChannel channel, GuildChannel messageChannel, Permission... permissions) {
            final MessageBuilder messageBuilder = new MessageBuilder();
            final EnumSet<Permission> permissionsFound = selfMember.getPermissions(channel);
            messageBuilder.append("This command requires additional permissions");
            if (channel.getIdLong() != messageChannel.getIdLong()) {
                messageBuilder.append(" in channel ");
                if (channel.getType() == ChannelType.TEXT)
                    messageBuilder.append(((TextChannel) channel).getAsMention());
                else messageBuilder.append("**").append(channel.getName()).append("**");
            }
            messageBuilder.append(" to execute\n```diff");
            Arrays.stream(permissions).map(permission -> (permissionsFound.contains(permission) ? "\n+ " : "\n- ") + permission.getName()).sorted(Comparator.comparingInt(s -> s.codePointAt(0))).forEachOrdered(messageBuilder::append);
            messageBuilder.append("\n```");
            return messageBuilder.build();
        }
    }
}
