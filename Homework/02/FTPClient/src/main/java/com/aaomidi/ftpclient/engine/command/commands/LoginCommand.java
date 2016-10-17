package com.aaomidi.ftpclient.engine.command.commands;

import com.aaomidi.ftpclient.engine.FTPClient;
import com.aaomidi.ftpclient.engine.command.FTPCommand;
import com.aaomidi.ftpclient.engine.lang.StatusCodes;
import com.aaomidi.ftpclient.engine.lang.Type;
import com.aaomidi.ftpclient.util.Log;

import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;

public class LoginCommand extends FTPCommand {
    public LoginCommand(FTPClient client) {
        super(
                client,
                "login",
                "Starts the authentication sequence.",
                "letmein", "doorbell", "hai"
        );
    }

    @Override
    public void execute(String cmd, List<String> args) {
        Scanner scanner = new Scanner(System.in);

        /* Start Authentication */
        Log.log(Level.INFO, Type.LOCAL, "Please enter your username: ");
        String username = scanner.next();

        Log.log(Level.FINE, Type.LOCAL, "Authenticating %s.", username);
        LoginStatus loginStatus = null;
        try {
            client.writeControl(String.format("USER %s%n", username));
            List<String> output = client.getOutput();
            loginStatus = handleUsernameOutput(output);
        } catch (Exception ex) {
            Log.log(Level.SEVERE, Type.LOCAL, "Error when authenticating username: %s", username);
            return;
        }
        // If no password login was allowed, just skip the rest of the stuff.
        if (loginStatus != LoginStatus.PASSWORD) {
            Log.log(Level.SEVERE, Type.LOCAL, "Login failed.");
            return;
        }

        String password = scanner.next();

        try {
            client.writeControl(String.format("PASS %s%n", password));
            List<String> output = client.getOutput();
            loginStatus = handlePasswordOutput(output);
        } catch (Exception ex) {
            Log.log(Level.SEVERE, Type.LOCAL, "Error when authenticating username: %s", username);
            return;
        }
        if (loginStatus == LoginStatus.LOGGED_IN) {
            Log.log(Level.INFO, Type.LOCAL, "You're logged in. Do `help` to see commands you can do.");
        } else {
            Log.log(Level.SEVERE, Type.LOCAL, "Login failed.");
        }
        /* End Authentication */
    }

    private LoginStatus handleUsernameOutput(List<String> list) {
        String output = list.get(0);
        if (output == null) {
            Log.log(Level.SEVERE, Type.LOCAL, "ISSUE WHEN LOGGING YOU IN.");
            return LoginStatus.ERROR;
        }
        int code = StatusCodes.getStatusCodeFromString(output);

        Log.log(Level.INFO, Type.LOCAL, StatusCodes.getMessage(code));

        if (code == 331) {
            return LoginStatus.PASSWORD;
        } else if (code == 230) {
            return LoginStatus.LOGGED_IN;
        }
        return LoginStatus.ERROR;
    }

    private LoginStatus handlePasswordOutput(List<String> list) {
        String output = list.get(0);
        if (output == null) {
            Log.log(Level.SEVERE, Type.LOCAL, "ISSUE WHEN LOGGING YOU IN.");
            return LoginStatus.ERROR;
        }

        int code = StatusCodes.getStatusCodeFromString(output);
        Log.log(Level.INFO, Type.LOCAL, StatusCodes.getMessage(code));

        if (code == 230) {
            return LoginStatus.LOGGED_IN;
        }
        return LoginStatus.ERROR;
    }

    private enum LoginStatus {
        LOGGED_IN,
        PASSWORD,
        ERROR
    }
}
