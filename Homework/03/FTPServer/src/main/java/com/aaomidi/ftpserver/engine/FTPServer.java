package com.aaomidi.ftpserver.engine;

import com.aaomidi.ftpserver.Main;
import com.aaomidi.ftpserver.config.Config;
import com.aaomidi.ftpserver.config.auth.AuthConfig;
import com.aaomidi.ftpserver.engine.command.FTPCommand;
import com.aaomidi.ftpserver.engine.command.ftpcommands.*;
import com.aaomidi.ftpserver.util.Type;
import com.aaomidi.ftpserver.util.Log;
import lombok.Getter;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.logging.Level;

public class FTPServer {
    private final Main instance;
    @Getter
    private final Config config;
    @Getter
    private final AuthConfig authConfig;
    private final int portNumber;
    @Getter
    private final HashMap<String, FTPCommand> commands = new HashMap<>();


    private ServerSocket controlSocket;

    public FTPServer(Main instance, Config config, AuthConfig authConfig, int portNumber) {
        this.instance = instance;
        this.config = config;
        this.authConfig = authConfig;
        this.portNumber = portNumber;
        this.registerCommands();
    }

    public void startControlSocket() throws IOException {
        Log.log(Level.INFO, Type.LOCAL, "Starting control socket...");
        controlSocket = new ServerSocket(portNumber);
        Log.log(Level.INFO, Type.LOCAL, "\tStarted control socket.");
    }

    public void listenToControl() {
        new Thread(() -> {
            while (true) {
                try {
                    Socket socket = controlSocket.accept();
                    new FTPConnection(this, socket);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).start();
    }

    private void registerCommands() {
        registerCommand(new CDUPCommand(instance));
        registerCommand(new CWDCommand(instance));
        registerCommand(new EPRTCommand(instance));
        registerCommand(new EPSVCommand(instance));
        registerCommand(new LISTCommand(instance));
        registerCommand(new NOOPCommand(instance));
        registerCommand(new PASSCommand(instance));
        registerCommand(new PASVCommand(instance));
        registerCommand(new PORTCommand(instance));
        registerCommand(new PWDCommand(instance));
        registerCommand(new QUITCommand(instance));
        registerCommand(new RETRCommand(instance));
        registerCommand(new USERCommand(instance));
    }

    private void registerCommand(FTPCommand command) {
        for (String s : command.getAliases()) {
            this.commands.put(s.toLowerCase(), command);
        }
        this.commands.put(command.getName().toLowerCase(), command);
    }
}
