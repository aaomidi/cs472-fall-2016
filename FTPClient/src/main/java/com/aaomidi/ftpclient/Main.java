package com.aaomidi.ftpclient;

import com.aaomidi.ftpclient.engine.FTPClient;
import com.aaomidi.ftpclient.engine.FTPMode;
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

        /* Log file stuff */
        File file = new File(args[1]);
        if (file.exists()) {
            Log.log(Level.INFO, Type.LOCAL, "Log file found. Appending...");
        } else {
            Log.log(Level.INFO, Type.LOCAL, "Log file doesn't exist. Creating...");
            try {
                file.createNewFile();
            } catch (IOException e) {
                Log.log(Level.SEVERE, Type.LOCAL, "Log file creation failed. Exiting.");
                System.exit(-1);
            }
        }

        try {
            prepareFile(file);
        } catch (IOException e) {
            Log.log(Level.SEVERE, Type.LOCAL, "Problem initiating the file. Exiting.");
            e.printStackTrace();
            System.exit(-1);
        }
        Log.log(Level.FINE, Type.LOCAL, "\tLog file initiated.");
        /* End Log file */


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

        Log.log(Level.INFO, Type.LOCAL, "Choose a mode:\n\t1. Active\n\t2. Passive\n\t3. Extended Active\n\t4. Extended Passive");
        int choice = scanner.nextInt();
        switch (choice) {
            case 1:
                builder.setMode(FTPMode.ACTIVE);
                break;
            case 2:
                builder.setMode(FTPMode.PASSIVE);
                break;
            case 3:
                builder.setMode(FTPMode.EACTIVE);
                break;
            case 4:
                builder.setMode(FTPMode.EPASSIVE);
                break;
            default:
        }
        FTPClient client = builder.build();

        try {
            client.connect();
            client.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Append to logfile.
     *
     * @throws IOException
     */
    private static void prepareFile(File logFile) throws IOException {
        //logFile.delete();
        //logFile.createNewFile();
        Log.addFileHandler(logFile);
    }
}
