package com.aaomidi.ftpserver.engine.command.ftpcommands;

import com.aaomidi.ftpserver.Main;
import com.aaomidi.ftpserver.beans.Authentication;
import com.aaomidi.ftpserver.config.auth.Auth;
import com.aaomidi.ftpserver.engine.Blacklist;
import com.aaomidi.ftpserver.engine.FTPConnection;
import com.aaomidi.ftpserver.engine.FTPState;
import com.aaomidi.ftpserver.engine.command.FTPCommand;

import java.security.SecureRandom;
import java.util.List;

public class PASSCommand extends FTPCommand {
    private SecureRandom secureRandom = new SecureRandom();

    public PASSCommand(Main instance) {
        super(
                instance,
                "pass",
                "Authenticates a password with the server.",
                FTPState.LOGGING_IN);
    }

    @Override
    public String execute(FTPConnection connection, String command, List<String> args) {
        /*
         * PASS password
         */
        if (args.size() < 1) {
            clearState(connection);
            return "500 Syntax error.";
        }
        String password = args.get(0);

        Blacklist bl = getInstance().getServer().getBlacklist().get(connection.getControlSocket().getInetAddress());
        bl.addCount();

        if (!bl.canWork()) {
            connection.quit();
            try {
                connection.writeToControl("221 okay.");
            } catch (Exception e) {
            }
            return null;
        }

        Auth auth = connection.getAuthByUsername(connection.getStateInformation());
        if (auth == null) {
            clearState(connection);
            return "530 Not logged in.";
        } else {
            Authentication authentication = new Authentication(connection.getStateInformation(), password, auth);
            try {
                if (authentication.verify()) {
                    clearState(connection);
                    connection.changeState(FTPState.AUTHENTICATED);
                    return "230 Logged in, enjoy.";
                }
            } catch (Exception e) {

            }
            clearState(connection);

            return "530 Not logged in.";
        }
    }

    private void clearState(FTPConnection connection) {
        connection.changeState(FTPState.NOT_AUTHENTICATED);
        connection.changeInformation("");

    }
}
