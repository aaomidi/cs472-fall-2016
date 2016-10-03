package com.aaomidi.ftpclient.engine.command.commands;

import com.aaomidi.ftpclient.engine.FTPClient;
import com.aaomidi.ftpclient.engine.command.FTPCommand;
import com.aaomidi.ftpclient.engine.lang.Type;
import com.aaomidi.ftpclient.util.Log;

import java.util.List;
import java.util.logging.Level;

public class CwdCommand extends FTPCommand {
    public CwdCommand(FTPClient client) {
        super(
                client,
                "ChangeWorkingDirectory",
                "Changes the working directory. Example: cd /folder/",
                "cd", "cwd", "goto"
        );
    }

    @Override
    public void execute(String cmd, List<String> args) {
        if (args.size() == 0) {
            Log.log(Level.INFO, Type.LOCAL, "Please specify the folder you want to go to.");
            return;
        }

        try {
            client.writeControl(String.format("cwd %s", args.get(0)));
            client.printOutput(client.getOutput(), Level.INFO, Type.CONTROL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
