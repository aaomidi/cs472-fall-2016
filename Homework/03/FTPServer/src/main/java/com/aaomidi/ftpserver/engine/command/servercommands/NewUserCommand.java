package com.aaomidi.ftpserver.engine.command.servercommands;

import com.aaomidi.ftpserver.Main;
import com.aaomidi.ftpserver.beans.Authentication;
import com.aaomidi.ftpserver.config.auth.Auth;
import com.aaomidi.ftpserver.config.auth.AuthConfig;
import com.aaomidi.ftpserver.engine.command.ServerCommand;
import com.aaomidi.ftpserver.util.Type;
import com.aaomidi.ftpserver.util.Log;

import java.util.List;
import java.util.logging.Level;

public class NewUserCommand extends ServerCommand {

    public NewUserCommand(Main main) {
        super(
                main,
                "newuser",
                "Adds a new user to the server.",
                "adduser", "useradd", "plzaddme"
        );
    }

    @Override
    public void execute(String command, List<String> args) {
        if (args.size() != 2) {
            Log.log(Level.SEVERE, Type.LOCAL, "Not enough arguments.");
            return;
        }
        String username = args.get(0);
        String password = args.get(1);
        AuthConfig authConfig = getInstance().getServer().getAuthConfig();

        Authentication authentication = new Authentication(username, password, null);
        try {
            Auth auth = authentication.encrypt();
            authConfig.getAuths().put(username, auth);
            authConfig.save(getInstance().getServer().getConfig().getAuthConfig());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        Log.log(Level.INFO, Type.LOCAL, "Auth added.");
    }
}
