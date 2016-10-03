package com.aaomidi.ftpclient.engine.command.commands;

import com.aaomidi.ftpclient.engine.FTPClient;
import com.aaomidi.ftpclient.engine.command.FTPCommand;
import com.aaomidi.ftpclient.engine.lang.Type;
import com.aaomidi.ftpclient.util.Log;

import java.util.List;
import java.util.logging.Level;

public class RetrCommand extends FTPCommand {
    public RetrCommand(FTPClient client) {
        super(
                client,
                "Retrieve",
                "Retrieves a file.",
                "retr", "download", "get", "iwant", "plzgiveme", "feedme"
        );
    }

    @Override
    public void execute(String cmd, List<String> args) {
        client.getDataLock().lock();
        if (args.size() == 0) {
            Log.log(Level.INFO, Type.LOCAL, "Please specify the file you want to get.");
            client.getDataLock().unlock();
            return;
        }

        client.setFileTransfer(true);
        client.setFileName(args.get(0));
        try {
            client.prepareConnection();
            client.writeControl(String.format("RETR %s", args.get(0)));
            client.printOutput(client.getOutput(), Level.INFO, Type.CONTROL);
        } catch (Exception e) {
            e.printStackTrace();
        }
        client.getDataLock().unlock();
    }
}
