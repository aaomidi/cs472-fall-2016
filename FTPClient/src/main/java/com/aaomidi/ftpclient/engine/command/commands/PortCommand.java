package com.aaomidi.ftpclient.engine.command.commands;

import com.aaomidi.ftpclient.engine.FTPClient;
import com.aaomidi.ftpclient.engine.command.FTPCommand;
import com.aaomidi.ftpclient.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

public class PortCommand extends FTPCommand {
    public PortCommand(FTPClient client) {
        super(
                client,
                "port",
                "Tells the server what port to connect to. Example: port 11422",
                "connect2me"
        );
    }

    @Override
    public void execute(String cmd, List<String> args) {
        if (args.size() < 1) {
            Log.log(Level.SEVERE, "Not enough arguments. Command usage: %s", getHelp());
            return;
        }

        short port;
        try {
            port = Short.valueOf(args.get(0));
        } catch (NumberFormatException ex) {
            Log.log(Level.SEVERE, "Port number wasn't a number.");
            return;
        }

        try {
            client.createActiveDataConnection(port);
        } catch (IOException e) {
            Log.log(Level.SEVERE, "Unable to make data connection. Message: %s. Stacktrace: ", e.getMessage());
            e.printStackTrace();
        }

        //String command = String.format("PORT %s,%s,%s,%s,%s,%s");
        try {
            client.writeControl("");
        } catch (IOException e) {
            Log.log(Level.SEVERE, "Error when writing to control.");
            e.printStackTrace();
        }
    }
}
