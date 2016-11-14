package com.aaomidi.ftpserver.engine.command.ftpcommands;

import com.aaomidi.ftpserver.Main;
import com.aaomidi.ftpserver.engine.FTPConnection;
import com.aaomidi.ftpserver.engine.command.FTPCommand;

import java.util.List;

public class NOOPCommand extends FTPCommand {
    public NOOPCommand(Main instance) {
        super(
                instance,
                "noop",
                "No operation.",
                null
        );
    }

    @Override
    public String execute(FTPConnection connection, String command, List<String> args) {
        return "200 OK";
    }
}
