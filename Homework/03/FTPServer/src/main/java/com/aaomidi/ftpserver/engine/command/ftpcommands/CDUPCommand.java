package com.aaomidi.ftpserver.engine.command.ftpcommands;

import com.aaomidi.ftpserver.Main;
import com.aaomidi.ftpserver.engine.FTPConnection;
import com.aaomidi.ftpserver.engine.FTPState;
import com.aaomidi.ftpserver.engine.command.FTPCommand;

import java.io.File;
import java.util.List;

public class CDUPCommand extends FTPCommand {
    public CDUPCommand(Main instance) {
        super(
                instance,
                "cdup",
                "Change to parent directory.",
                FTPState.AUTHENTICATED
        );
    }

    @Override
    public String execute(FTPConnection connection, String command, List<String> args) {
        File newFile = connection.getCurrentWorkingDirectory().getParentFile();

        if (!newFile.exists()) {
            return "530 File doesn't exist.";
        } else {
            connection.setCurrentWorkingDirectory(newFile);

            return String.format("257 \"%s\" is current directory.", connection.getCurrentWorkingDirectory().getAbsolutePath());
        }
    }
}
