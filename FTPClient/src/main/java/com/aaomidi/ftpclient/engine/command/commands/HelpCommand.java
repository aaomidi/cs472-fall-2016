package com.aaomidi.ftpclient.engine.command.commands;

import com.aaomidi.ftpclient.engine.FTPClient;
import com.aaomidi.ftpclient.engine.command.FTPCommand;
import com.aaomidi.ftpclient.util.Log;

import java.util.logging.Level;

public class HelpCommand extends FTPCommand {
    public HelpCommand(FTPClient client) {
        super(
                client,
                "Help",
                "Displays information about the commands and what actions they preform.",
                "h", "?", "whatisdis", "wot", "?", "helpme", "plz", "tfw"
        );
    }

    @Override
    public void execute() {
        for (FTPCommand cmd : client.getCommands().values()) {
            Log.log(Level.INFO, "%s -> %s", cmd.getName(), cmd.getHelp());
        }
    }
}
