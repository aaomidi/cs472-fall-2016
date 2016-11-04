package com.aaomidi.ftpserver.engine.command.ftpcommands;

import com.aaomidi.ftpserver.Main;
import com.aaomidi.ftpserver.engine.FTPConnection;
import com.aaomidi.ftpserver.engine.FTPState;
import com.aaomidi.ftpserver.engine.command.FTPCommand;

import java.io.File;
import java.util.List;

public class CWDCommand extends FTPCommand {
    public CWDCommand(Main instance) {
        super(
                instance,
                "cwd",
                "Change working directory.",
                FTPState.AUTHENTICATED
        );
    }

    @Override
    public String execute(FTPConnection connection, String command, List<String> args) {
        /*
         * PWD
         */

        if (args.size() != 1) {
            return "500 Syntax error.";
        }

        File newFile = new File(args.get(0));

        if (!newFile.exists()) {
            return "530 File doesn't exist.";
        } else {
            connection.setCurrentWorkingDirectory(newFile);

            return String.format("257 \"%s\" is current directory.", connection.getCurrentWorkingDirectory().getAbsolutePath());
        }
    }
}
