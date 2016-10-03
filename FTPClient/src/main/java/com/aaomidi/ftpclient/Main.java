package com.aaomidi.ftpclient;

import com.aaomidi.ftpclient.engine.FTPClient;
import com.aaomidi.ftpclient.engine.lang.Type;
import com.aaomidi.ftpclient.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;

public class Main {
    public static void main(String... args) {
        /*
            Expected:
            1. Server IP.
            2. Log file path.
            3. Port (Optional)
         */
        Scanner scanner = new Scanner(System.in);

        int argCount = args.length;

        if (argCount < 2) {
            Log.log(Level.SEVERE, Type.LOCAL, "Not enough arguments were entered. Shutting down.");
            System.exit(-1);
        }

        FTPClient.FTPClientBuilder builder = FTPClient.FTPClientBuilder.builder();
        builder.hostname(args[0]);

        File file = new File(args[1]);
        if (file.exists()) {
            Log.log(Level.INFO, Type.LOCAL, "This file will be over-written. Press any key to continue.");
            scanner.nextLine();
        }

        Log.log(Level.FINE, Type.LOCAL, "Initiating log file.");
        try {
            prepareFile(file);
        } catch (IOException e) {
            Log.log(Level.SEVERE, Type.LOCAL, "Problem initiating the file. Exiting.");
            e.printStackTrace();
            System.exit(-1);
        }
        Log.log(Level.FINE, Type.LOCAL, "\tLog file initiated.");


        if (argCount > 2) {
            try {
                builder.port(Short.valueOf(args[2]));
            } catch (NumberFormatException ex) {
                Log.log(Level.SEVERE, Type.LOCAL, "Port number was invalid, shutting down.");
                System.exit(-1);
            }
        } else {
            Log.log(Level.FINE, Type.LOCAL, "Port argument was left empty, defaulting to port 21.");
            builder.port((short) 21);
        }

        Log.log(Level.INFO, Type.LOCAL, "Please enter your username: ");
        String username = scanner.next();
        builder.username(username);
        Log.log(Level.FINEST, Type.LOCAL, username);

        Log.log(Level.INFO, Type.LOCAL, "Please enter your password: ");
        String password = scanner.next();
        builder.password(password);
        Log.log(Level.FINEST, Type.LOCAL, password);

        FTPClient client = builder.build();

        try {
            client.connect();
            client.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Cleans the log file. Basically overwrites the previous one if it exists.
     *
     * @throws IOException
     */
    private static void prepareFile(File logFile) throws IOException {
        logFile.delete();
        logFile.createNewFile();
        Log.addFileHandler(logFile);
    }
}
