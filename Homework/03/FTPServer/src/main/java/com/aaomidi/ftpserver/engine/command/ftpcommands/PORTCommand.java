package com.aaomidi.ftpserver.engine.command.ftpcommands;

import com.aaomidi.ftpserver.Main;
import com.aaomidi.ftpserver.engine.FTPConnection;
import com.aaomidi.ftpserver.engine.FTPRegex;
import com.aaomidi.ftpserver.engine.FTPState;
import com.aaomidi.ftpserver.engine.command.FTPCommand;

import java.util.List;
import java.util.regex.Matcher;

public class PORTCommand extends FTPCommand {
    public static boolean ENABLED = true;

    public PORTCommand(Main instance) {
        super(
                instance,
                "PORT",
                "Create an active connection with server.",
                FTPState.AUTHENTICATED
        );
    }

    @Override
    public String execute(FTPConnection connection, String command, List<String> args) {
        if (!ENABLED) {
            return "502 Command not implemented.";
        }

        if (args.size() != 1) {
            return "421 Error";
        }

        String line = args.get(0);
        Matcher matcher = FTPRegex.ACTIVE_PORT.matcher(line);
        if (!matcher.matches()) {
            return "421 Error";
        }

        int port;
        try {
            port = Short.valueOf(matcher.group(5)) * 256 + Short.valueOf(matcher.group(6));
        } catch (Exception ex) {
            return "421 Error";
        }


        try {
            connection.createActiveDataConnection(port);
        } catch (Exception e) {
            e.printStackTrace();
            connection.changeState(FTPState.AUTHENTICATED);
            return "421 Error";
        }

        connection.changeState(FTPState.DATA_SOCKET);
        return "200 Port command successful";
    }
}
