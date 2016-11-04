package com.aaomidi.ftpserver.engine.command.ftpcommands;

import com.aaomidi.ftpserver.Main;
import com.aaomidi.ftpserver.engine.FTPConnection;
import com.aaomidi.ftpserver.engine.FTPState;
import com.aaomidi.ftpserver.engine.command.FTPCommand;

import java.net.Inet6Address;
import java.net.ServerSocket;
import java.util.List;

public class EPSVCommand extends FTPCommand {
    public EPSVCommand(Main instance) {
        super(
                instance,
                "epsv",
                "Create an extended passive connection with server.",
                FTPState.AUTHENTICATED
        );
    }

    @Override
    public String execute(FTPConnection connection, String command, List<String> args) {
        ServerSocket serverSocket = null;
        try {
            serverSocket = connection.createPassiveDataConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (serverSocket != null) {
            int type = 1;
            if (serverSocket.getInetAddress() instanceof Inet6Address) {
                type = 2;
            }
            String msg = String.format("229 Entering Extended Passive Mode |%d|%s|%d|",
                    type,
                    serverSocket.getInetAddress().getHostAddress(),
                    serverSocket.getLocalPort()
            );
            connection.listenToPassiveDataConnection();
            return msg;
        }
        return "421 Error.";
    }
}
