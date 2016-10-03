package com.aaomidi.ftpclient.engine.command.commands;

import com.aaomidi.ftpclient.engine.FTPClient;
import com.aaomidi.ftpclient.engine.command.FTPCommand;
import com.aaomidi.ftpclient.util.Log;

import java.util.logging.Level;

public class QuitCommand extends FTPCommand {
    public QuitCommand(FTPClient client) {
        super(
                client,
                "Quit",
                "Destroys the FTP connection and stops all communications between you and the server",
                "q", "leave", "kill", "die", "suicide"
        );
    }

    @Override
    public void execute() {
        Log.log(Level.INFO, "Shutting off connection to server.");
        client.close();
        Log.log(Level.INFO, "Goodbye.");
        System.exit(-1);
    }
}
