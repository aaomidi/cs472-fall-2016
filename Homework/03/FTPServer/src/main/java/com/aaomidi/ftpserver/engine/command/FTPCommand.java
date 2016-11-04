package com.aaomidi.ftpserver.engine.command;

import com.aaomidi.ftpserver.Main;
import com.aaomidi.ftpserver.engine.FTPConnection;
import com.aaomidi.ftpserver.engine.FTPState;
import lombok.Getter;

import java.util.List;

public abstract class FTPCommand {
    @Getter
    private final Main instance;
    @Getter
    private final String name;
    @Getter
    private final String description;
    @Getter
    private final FTPState requiredState;
    @Getter
    private final String[] aliases;

    public FTPCommand(Main instance, String name, String description, FTPState requiredState, String... aliases) {
        this.instance = instance;
        this.name = name;
        this.description = description;
        this.requiredState = requiredState;
        this.aliases = aliases;
    }

    public abstract String execute(FTPConnection connection, String command, List<String> args);
}
