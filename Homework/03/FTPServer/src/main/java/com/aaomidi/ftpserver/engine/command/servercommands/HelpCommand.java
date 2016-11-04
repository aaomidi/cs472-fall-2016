package com.aaomidi.ftpserver.engine.command.servercommands;

import com.aaomidi.ftpserver.Main;
import com.aaomidi.ftpserver.engine.command.ServerCommand;
import com.aaomidi.ftpserver.util.Log;
import com.aaomidi.ftpserver.util.Type;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public class HelpCommand extends ServerCommand {
    public HelpCommand(Main instance) {
        super(
                instance,
                "help",
                "Displays information about the commands and what actions they preform.",
                "h", "?", "whatisdis", "wot", "?", "helpme", "plz", "tfw", "newnumberwhodis?"
        );
    }

    @Override
    public void execute(String command, List<String> args) {
        Set<ServerCommand> commands = new HashSet<>(getInstance().getCommands().values());
        for (ServerCommand cmd : commands) {
            Log.log(Level.INFO, Type.LOCAL, "%s -> %s", cmd.getName(), cmd.getDescription());
        }
    }
}
