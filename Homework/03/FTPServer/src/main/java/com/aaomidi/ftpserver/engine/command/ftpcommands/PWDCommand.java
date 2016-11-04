package com.aaomidi.ftpserver.engine.command.ftpcommands;

import com.aaomidi.ftpserver.Main;
import com.aaomidi.ftpserver.engine.FTPConnection;
import com.aaomidi.ftpserver.engine.FTPState;
import com.aaomidi.ftpserver.engine.command.FTPCommand;

import java.io.File;
import java.util.List;

public class PWDCommand extends FTPCommand {
    public PWDCommand(Main instance) {
        super(
                instance,
                "pwd",
                "Print working directory",
                FTPState.AUTHENTICATED
        );
    }

    @Override
    public String execute(FTPConnection connection, String command, List<String> args) {
        /*
         * PWD
         */

        File file = connection.getCurrentWorkingDirectory();
        String path = file.getAbsolutePath();
        return String.format("257 \"%s\" is current directory.", path);
    }

}
