package com.aaomidi.ftpserver.engine.command.ftpcommands;

import com.aaomidi.ftpserver.Main;
import com.aaomidi.ftpserver.engine.FTPConnection;
import com.aaomidi.ftpserver.engine.command.FTPCommand;

import java.util.List;

public class QUITCommand extends FTPCommand {
    public QUITCommand(Main instance) {
        super(
                instance,
                "quit",
                "Quits and ends connection.",
                null
        );
    }

    @Override
    public String execute(FTPConnection connection, String command, List<String> args) {
        try {
            connection.writeToControl("221 okay.");
        } catch (Exception e) {
        }
        connection.quit();
        return null;
    }
}
