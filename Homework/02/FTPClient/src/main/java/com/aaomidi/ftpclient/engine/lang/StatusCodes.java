package com.aaomidi.ftpclient.engine.lang;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

public class StatusCodes {
    private final static Map<Integer, String> VALUES = new HashMap<>();

    static {
        /* USER */
        VALUES.put(230, "Login successful.");
        VALUES.put(331, "Username was successful. Please enter password: ");
        VALUES.put(332, "Account is needed to login.");
        VALUES.put(500, "Syntax error. This shouldn't happen.");
        VALUES.put(501, "Syntax error. This shouldn't happen.");
        VALUES.put(421, "Server issue.");

        /* PASS */
        VALUES.put(202, "Command not implemented on server.");
        VALUES.put(530, "Not logged in");
        VALUES.put(503, "Bad sequence of commands. This shouldn't happen.");

        /* CWD */
        VALUES.put(550, "Requested action was not taken.");
        VALUES.put(250, "Requested action was successful.");

        /* PWD */
        VALUES.put(257, "Path was created.");

        /* CDUP */
        VALUES.put(200, "Command was okay.");

        /* QUIT */
        VALUES.put(211, "System status.");

        /* PASV */
        VALUES.put(227, "Entering passive mode");

        /* RETR */
        VALUES.put(125, "Transfer starting.");
        VALUES.put(110, "This shouldn't happen.");
        VALUES.put(150, "File status is oka; about to open a data connection.");
        VALUES.put(226, "Closing data connection.");
        VALUES.put(425, "Can't open data connection.");
        VALUES.put(426, "Connection closed; transfer aborted.");
        VALUES.put(450, "Requested file is not available.");
        VALUES.put(451, "Local error in processing file.");


    }

    public static int getStatusCodeFromString(String s) {
        Matcher m = FTPRegex.RESPONSE_CODE.matcher(s);

        if (!m.matches()) {
            return -1;
        }

        return Integer.valueOf(m.group(1));
    }

    public static String getMessage(int code) {
        return VALUES.getOrDefault(code, String.format("Status code %d not defined.", code));
    }

}
