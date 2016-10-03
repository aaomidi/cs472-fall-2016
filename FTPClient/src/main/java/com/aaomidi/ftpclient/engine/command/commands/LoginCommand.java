package com.aaomidi.ftpclient.engine.command.commands;

import com.aaomidi.ftpclient.engine.FTPClient;
import com.aaomidi.ftpclient.engine.command.FTPCommand;
import com.aaomidi.ftpclient.engine.lang.Type;
import com.aaomidi.ftpclient.util.Log;

import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;

public class LoginCommand extends FTPCommand {
    public LoginCommand(FTPClient client) {
        super(
                client,
                "Login",
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
        try {
            client.writeControl(String.format("USER %s%n", username));
            client.printOutput(client.getOutput(), Level.INFO, Type.CONTROL);
        } catch (Exception ex) {
            Log.log(Level.SEVERE, Type.LOCAL, "Error when authenticating username: %s", username);
            return;
        }

        Log.log(Level.INFO, Type.LOCAL, "Please enter your password: ");
        String password = scanner.next();

        try {
            client.writeControl(String.format("PASS %s%n", password));
            client.printOutput(client.getOutput(), Level.INFO, Type.CONTROL);
        } catch (Exception ex) {
            Log.log(Level.SEVERE, Type.LOCAL, "Error when authenticating username: %s", username);
            return;
        }
        /* End Authentication */
    }
}
