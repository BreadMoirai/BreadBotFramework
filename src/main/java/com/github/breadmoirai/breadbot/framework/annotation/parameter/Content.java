package com.github.breadmoirai.breadbot.framework.annotation.parameter;

import com.github.breadmoirai.breadbot.framework.event.CommandEvent;
import net.dv8tion.jda.core.entities.Message;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.github.breadmoirai.breadbot.framework.annotation.parameter.Content.Type.RAW_TRIMMED;

/**
 * Sets this string parameter to represent the message contents.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Content {

    /**
     * Type of content.
     * <ul>
     * <li>{@link Content.Type#RAW}</li>
     * <li>{@link Content.Type#STRIPPED}</li>
     * <li>{@link Content.Type#DISPLAY}</li>
     * <li>{@link Content.Type#RAW_TRIMMED}</li>
     * </ul>
     *
     * @return the content type.
     */
    Content.Type value() default RAW_TRIMMED;

    /**
     * The enum Type.
     */
    enum Type {

        /**
         * Raw type.
         *
         * @see Message#getContentRaw()
         */
        RAW,

        /**
         * Stripped type.
         *
         * @see Message#getContentStripped()
         */
        STRIPPED,

        /**
         * Display type.
         *
         * @see Message#getContentStripped()
         */
        DISPLAY,

        /**
         * Raw trimmed type.
         * Similar to {@link Type#RAW} but does not include the prefix and keys.
         *
         * @see CommandEvent#getContent()
         */
        RAW_TRIMMED
    }
}
