package com.aaomidi.ftpserver.engine.command.ftpcommands;

import com.aaomidi.ftpserver.Main;
import com.aaomidi.ftpserver.engine.FTPConnection;
import com.aaomidi.ftpserver.engine.FTPState;
import com.aaomidi.ftpserver.engine.command.FTPCommand;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class LISTCommand extends FTPCommand {
    public LISTCommand(Main instance) {
        super(
                instance,
                "list",
                "List files.",
                FTPState.DATA_SOCKET
        );
    }

    @Override
    public String execute(FTPConnection connection, String command, List<String> args) {
        /*
         * LIST
         */

        if (args.size() != 0) {
            return "500 Syntax error.";
        }

        File file = connection.getCurrentWorkingDirectory();
        DirectoryStream<Path> dirStream = null;
        try {
            dirStream = Files.newDirectoryStream(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }


        StringBuilder out = new StringBuilder();
        if (dirStream == null) {
            out.append("No files.\n");
        } else {
            for (Path entry : dirStream) {
                out.append(entry.getFileName());
                out.append("\n");
            }
        }

        try {
            connection.writeToControl(String.format("150 Opening data socket.\n226 Successfully transferred \"%s\"", file.getPath()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        new Thread(() -> connection.writeToDataSocketAndClose(out.toString().trim())).start();
        connection.changeState(FTPState.AUTHENTICATED);
        return null;
    }
}
