package com.aaomidi.ftpclient.engine.command.commands;

import com.aaomidi.ftpclient.engine.FTPClient;
import com.aaomidi.ftpclient.engine.command.FTPCommand;
import com.aaomidi.ftpclient.engine.lang.Type;
import com.aaomidi.ftpclient.util.Log;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public class HelpCommand extends FTPCommand {
    public HelpCommand(FTPClient client) {
        super(
                client,
                "help",
                "Displays information about the commands and what actions they preform.",
                "h", "?", "whatisdis", "wot", "?", "helpme", "plz", "tfw", "newnumberwhodis?"
        );
    }

    @Override
    public void execute(String command, List<String> args) {
        Set<FTPCommand> commands = new HashSet<>(client.getCommands().values());
        for (FTPCommand cmd : commands) {
            if (cmd.isHidden()) return;
            Log.log(Level.INFO, Type.LOCAL, "%s -> %s", cmd.getName(), cmd.getHelp());
        }
    }
}
