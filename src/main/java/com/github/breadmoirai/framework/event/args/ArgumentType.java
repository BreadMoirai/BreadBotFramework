package com.github.breadmoirai.framework.event.args;

public enum ArgumentType {
    INTEGER, LONG, FLOAT, DOUBLE, RANGE, HEX, USER, MEMBER, ROLE, TEXTCHANNEL, EMOTE, EMOJI;

    ArgumentType() { }

//    public static ArgumentType getTypeFromClass(Class klass) {
//        if (klass == Integer.class || klass == Integer.TYPE) {
//            return INTEGER;
//        } else if (klass == Long.class || klass == Long.TYPE) {
//            return LONG;
//        } else if (klass == Float.class || klass == Float.TYPE) {
//            return FLOAT;
//        } else if (klass == Double.class || klass == Double.TYPE) {
//            return DOUBLE;
//        } else if () {
//            return RANGE;
//        } else if () {
//            return HEX;
//        } else if (User.class.isAssignableFrom(klass)) {
//            return USER;
//        } else if (Member.class.isAssignableFrom(klass)) {
//            return MEMBER;
//        } else if (Role.class.isAssignableFrom(klass)) {
//            return ROLE;
//        } else if (TextChannel.class.isAssignableFrom(klass)) {
//            return TEXTCHANNEL;
//        } else if (Emote.class.isAssignableFrom(klass)) {
//            return EMOTE;
//        } else if (Emoji.class.isAssignableFrom(klass)) {
//            return EMOJI;
//        }
//    }
}
