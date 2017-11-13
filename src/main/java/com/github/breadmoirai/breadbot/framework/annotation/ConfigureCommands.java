package com.github.breadmoirai.breadbot.framework.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
public @interface ConfigureCommands {

    ConfigureCommand[] value();

}
