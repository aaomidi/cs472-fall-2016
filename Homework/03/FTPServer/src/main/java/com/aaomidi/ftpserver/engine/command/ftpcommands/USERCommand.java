package com.aaomidi.ftpserver.engine.command.ftpcommands;

import com.aaomidi.ftpserver.Main;
import com.aaomidi.ftpserver.engine.FTPConnection;
import com.aaomidi.ftpserver.engine.FTPState;
import com.aaomidi.ftpserver.engine.command.FTPCommand;

import java.util.List;

public class USERCommand extends FTPCommand {

    public USERCommand(Main instance) {
        super(
                instance,
                "user",
                "Authenticates a username with the service",
                FTPState.NOT_AUTHENTICATED);
    }

    @Override
    public String execute(FTPConnection connection, String command, List<String> args) {
        /*
         * USER username
         */
        if (args.size() != 1) {
            connection.changeState(FTPState.NOT_AUTHENTICATED);
            return "500 Syntax error.";
        }

        String username = args.get(0);
        connection.changeInformation(username);
        connection.changeState(FTPState.LOGGING_IN);

        return "331 Username okay, need password.";
    }
}
