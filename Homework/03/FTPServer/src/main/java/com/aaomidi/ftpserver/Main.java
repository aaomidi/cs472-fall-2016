package com.aaomidi.ftpserver;

import com.aaomidi.ftpserver.config.Config;
import com.aaomidi.ftpserver.config.auth.AuthConfig;
import com.aaomidi.ftpserver.engine.FTPServer;
import com.aaomidi.ftpserver.engine.command.ServerCommand;
import com.aaomidi.ftpserver.engine.command.servercommands.HelpCommand;
import com.aaomidi.ftpserver.engine.command.servercommands.NewUserCommand;
import com.aaomidi.ftpserver.util.Log;
import com.aaomidi.ftpserver.util.Type;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;

public class Main {
    public final static Gson GSON;

    static {
        GSON = new GsonBuilder().setPrettyPrinting().create();
    }

    @Getter
    private final HashMap<String, ServerCommand> commands = new HashMap<>();
    @Getter
    private FTPServer server;

    public static void main(String... args) {
        if (args.length < 2) {
            throw new Error("Not enough arguments. Goodbye.");
        }

        String logPath = args[0];
        String port = args[1];

        String configPath = "config.json";
        if (args.length > 2) {
            configPath = args[2];
        }

        new Main(logPath, port, configPath);
    }

    public Main(String logPath, String port, String configPath) {
        File logFile = new File(logPath);
        try {
            prepareLogFile(logFile);
        } catch (Exception ex) {
            throw new Error(ex);
        }
        int portNumber;
        try {
            portNumber = Integer.valueOf(port);
        } catch (NumberFormatException ex) {
            portNumber = -1;
        }
        if (portNumber < 1 || portNumber > 65535) {
            portNumber = 24142;
            Log.log(Level.SEVERE, Type.LOCAL, "Port number not recognized. Using %d", portNumber);
        }

        Log.log(Level.INFO, Type.LOCAL, "Server will start with the port number: %d", portNumber);

        File configFile = new File(configPath);
        Config config;
        AuthConfig authConfig;
        try {
            config = prepareConfigFile(configFile);
            authConfig = config.readAuthConfig();
        } catch (Exception ex) {
            throw new Error(ex);
        }
        Log.log(Level.INFO, Type.LOCAL, "Welcome to Amir's FTP Server. The server is going to prepare the sockets. Remember you can use `newuser` to create a new user for the server.\n");

        server = new FTPServer(this, config, authConfig, portNumber);

        try {
            server.startControlSocket();
            server.listenToControl();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        this.registerCommands();
        this.listenToUserInput();
    }

    private void registerCommands() {
        registerCommand(new HelpCommand(this));
        registerCommand(new NewUserCommand(this));
    }

    private void registerCommand(ServerCommand command) {
        for (String s : command.getAliases()) {
            this.commands.put(s.toLowerCase(), command);
        }
        this.commands.put(command.getName().toLowerCase(), command);
    }

    public void listenToUserInput() {
        new Thread(() -> {
            while (true) {
                Scanner scanner = new Scanner(System.in);
                String input = scanner.nextLine();

                String[] split = input.split(" ");
                List<String> args = new ArrayList<>(split.length - 1);

                for (int i = 1; i < split.length; i++) {
                    String s = split[i];
                    args.add(s);
                }

                ServerCommand serverCommand = commands.get(split[0].toLowerCase());
                if (serverCommand == null) {
                    Log.log(Level.INFO, Type.LOCAL, "Command not recognized.");
                } else {
                    serverCommand.execute(split[0], args);
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void prepareLogFile(File logFile) throws IOException {
        if (!logFile.exists()) {
            if (!logFile.createNewFile()) {
                throw new IOException("Log file not created.");
            }
        }
        Log.addFileHandler(logFile);
    }

    private Config prepareConfigFile(File file) throws IOException {
        if (!file.exists()) {
            if (!file.createNewFile()) {
                throw new IOException("Config file not created.");
            }
            Config config = new Config("auth.json");
            config.save(file);
            return config;
        }
        return GSON.fromJson(new FileReader(file), Config.class);
    }

}
