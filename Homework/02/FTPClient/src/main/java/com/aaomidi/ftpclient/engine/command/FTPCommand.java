package com.aaomidi.ftpclient.engine.command;

import com.aaomidi.ftpclient.engine.FTPClient;
import lombok.Getter;

import java.util.List;

public abstract class FTPCommand {
    protected final FTPClient client;
    @Getter
    private final String name;
    @Getter
    private final String help;
    @Getter
    private final boolean hidden;
    @Getter
    private final String[] aliases;


    public FTPCommand(FTPClient client, String name, String help, String... aliases) {
        this.client = client;
        this.name = name;
        this.help = help;
        this.aliases = aliases;
        this.hidden = false;
    }

    public FTPCommand(FTPClient client, String name, String help, boolean hidden, String... aliases) {
        this.client = client;
        this.name = name;
        this.help = help;
        this.hidden = hidden;
        this.aliases = aliases;
    }

    /**
     * Execute the command.
     */
    public abstract void execute(String cmd, List<String> args);
}
