package com.aaomidi.ftpclient.engine.command;

import com.aaomidi.ftpclient.engine.FTPClient;
import lombok.Getter;

public abstract class FTPCommand {
    protected final FTPClient client;
    @Getter
    private final String name;
    @Getter
    private final String help;
    @Getter
    private final String[] aliases;

    public FTPCommand(FTPClient client, String name, String help, String... aliases) {
        this.client = client;
        this.name = name;
        this.help = help;
        this.aliases = aliases;
    }

    /**
     * Execute the command.
     */
    public abstract void execute();
}
