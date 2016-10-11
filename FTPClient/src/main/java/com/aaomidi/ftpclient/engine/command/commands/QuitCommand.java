package com.aaomidi.ftpclient.engine.command.commands;

import com.aaomidi.ftpclient.engine.FTPClient;
import com.aaomidi.ftpclient.engine.command.FTPCommand;
import com.aaomidi.ftpclient.engine.lang.Type;
import com.aaomidi.ftpclient.util.Log;

import java.util.List;
import java.util.logging.Level;

public class QuitCommand extends FTPCommand {
    public QuitCommand(FTPClient client) {
        super(
                client,
                "quit",
                "Destroys the FTP connection and stops all communications between you and the server",
                // Yes I'm bored and I'm writing silly aliases
                "q", ":q", ":q!", "leave", "kill", "die", "dietbh", "suicide", "pullthetrigger", "kickawaythestool"
        );
    }

    @Override
    public void execute(String command, List<String> args) {
        Log.log(Level.INFO, Type.LOCAL, "Shutting off connection to server.");
        client.close();
        Log.log(Level.INFO, Type.LOCAL, "Goodbye.");
        System.exit(-1);
    }
}
