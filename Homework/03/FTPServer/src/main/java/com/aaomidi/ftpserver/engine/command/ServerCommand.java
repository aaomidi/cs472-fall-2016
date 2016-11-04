package com.aaomidi.ftpserver.engine.command;

import com.aaomidi.ftpserver.Main;
import lombok.Getter;

import java.util.List;

public abstract class ServerCommand {
    @Getter
    private final Main instance;
    @Getter
    private final String name;
    @Getter
    private final String description;
    @Getter
    private final String[] aliases;

    public ServerCommand(Main instance, String name, String description, String... aliases) {
        this.instance = instance;
        this.name = name;
        this.description = description;
        this.aliases = aliases;
    }

    public abstract void execute(String command, List<String> args);

}
