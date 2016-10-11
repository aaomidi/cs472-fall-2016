package com.aaomidi.ftpclient.engine.command.commands;

import com.aaomidi.ftpclient.engine.FTPClient;
import com.aaomidi.ftpclient.engine.command.FTPCommand;
import com.aaomidi.ftpclient.engine.lang.Type;
import com.aaomidi.ftpclient.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

public class LsCommand extends FTPCommand {
    public LsCommand(FTPClient client) {
        super(
                client,
                "ls",
                "Prints list of files.",
                "ls", "show"
        );
    }

    @Override
    public void execute(String cmd, List<String> args) {
        try {
            client.getDataLock().lock();
            client.prepareConnection();
            client.writeControl("list");
            client.printOutput(client.getOutput(), Level.INFO, Type.CONTROL);
        } catch (IOException e) {
            Log.log(Level.SEVERE, Type.LOCAL, "Error when writing/reading to/from control in port.");
            e.printStackTrace();
        }finally {
            client.getDataLock().unlock();
        }
    }
}
