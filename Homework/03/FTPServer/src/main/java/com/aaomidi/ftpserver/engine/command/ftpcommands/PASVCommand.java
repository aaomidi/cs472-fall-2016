package com.aaomidi.ftpserver.engine.command.ftpcommands;

import com.aaomidi.ftpserver.Main;
import com.aaomidi.ftpserver.engine.FTPConnection;
import com.aaomidi.ftpserver.engine.FTPState;
import com.aaomidi.ftpserver.engine.command.FTPCommand;

import java.net.ServerSocket;
import java.util.List;

public class PASVCommand extends FTPCommand {
    public static boolean ENABLED = true;

    public PASVCommand(Main instance) {
        super(
                instance,
                "pasv",
                "Create a passive connection with server.",
                FTPState.AUTHENTICATED
        );
    }

    @Override
    public String execute(FTPConnection connection, String command, List<String> args) {
        if (!ENABLED) {
            return "502 Command not implemented.";
        }
        ServerSocket serverSocket = null;
        try {
            serverSocket = connection.createPassiveDataConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (serverSocket != null) {
            byte[] addr = connection.getControlSocket().getLocalAddress().getAddress();
            String msg = String.format("227 Entering Passive Mode (%d,%d,%d,%d,%d,%d)", (int) addr[0] & 0xFF,
                    (int) addr[1] & 0xFF,
                    (int) addr[2] & 0xFF,
                    (int) addr[3] & 0xFF,
                    serverSocket.getLocalPort() / 256,
                    serverSocket.getLocalPort() % 256);

            connection.listenToPassiveDataConnection();

            return msg;
        }
        return "421 Error.";
    }
}
