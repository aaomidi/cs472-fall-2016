package com.aaomidi.ftpserver.engine.command.ftpcommands;

import com.aaomidi.ftpserver.Main;
import com.aaomidi.ftpserver.engine.FTPConnection;
import com.aaomidi.ftpserver.engine.FTPState;
import com.aaomidi.ftpserver.engine.command.FTPCommand;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

public class RETRCommand extends FTPCommand {
    public RETRCommand(Main instance) {
        super(
                instance,
                "retr",
                "Retrieve a file.",
                FTPState.DATA_SOCKET
        );
    }

    @Override
    public String execute(FTPConnection connection, String command, List<String> args) {
        if (args.size() != 1) {
            return "421 Error.";
        }
        try {
            connection.writeToControl("150 opening data connection.");
        } catch (Exception e) {
            e.printStackTrace();
        }
        connection.changeState(FTPState.AUTHENTICATED);
        String fileName = args.get(0);
        File file = new File(connection.getCurrentWorkingDirectory(), fileName);

        try {
            byte[] data = Files.readAllBytes(file.toPath());
            if (!connection.writeToDataSocketAndClose(data)) {
                return "421 Error.";
            }
        } catch (Exception e) {
            return "421 Error.";
        }
        return String.format("226 Successfully transferred \"%s\"", file.getName());
    }
}
