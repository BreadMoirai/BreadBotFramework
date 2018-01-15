package com.github.breadmoirai.breadbot.plugins.hocon;

import com.typesafe.config.Config;
import net.dv8tion.jda.core.entities.Guild;

import java.util.Map;

public interface HOCONConfigurable {

    void buildConfig(Guild guild, Map<String, Object> conf);

    boolean loadConfig(Guild guild, Config conf);

}
