package com.aaomidi.ftpclient.engine.command.commands;

import com.aaomidi.ftpclient.engine.FTPClient;
import com.aaomidi.ftpclient.engine.command.FTPCommand;
import com.aaomidi.ftpclient.engine.lang.Type;
import com.aaomidi.ftpclient.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.logging.Level;

public class PortCommand extends FTPCommand {
    public PortCommand(FTPClient client) {
        super(
                client,
                "port",
                "Tells the server what port to connect to. If port number is left empty, the client OS will pick a port at random. Example: port 11422, or, port",
                "connect2me", "active"
        );
    }

    @Override
    public void execute(String cmd, List<String> args) {
        short port = 0;
        if (args.size() > 0) {
            try {
                port = Short.valueOf(args.get(0));
            } catch (NumberFormatException ex) {
                Log.log(Level.SEVERE, Type.LOCAL, "Port number wasn't a number.");
                return;
            }
        }

        try {
            client.createActiveDataConnection(port);
        } catch (IOException e) {
            Log.log(Level.SEVERE, Type.LOCAL, "Unable to make data connection. Message: %s. Stacktrace: ", e.getMessage());
            e.printStackTrace();
        }


        Socket clientSocket = client.getControlSocket();
        ServerSocket serverSocket = client.getActiveDataServerSocket();
        byte[] addr = clientSocket.getLocalAddress().getAddress();

        String command = String.format("PORT %d,%d,%d,%d,%d,%d",
                (int) addr[0] & 0xFF,
                (int) addr[1] & 0xFF,
                (int) addr[2] & 0xFF,
                (int) addr[3] & 0xFF,
                serverSocket.getLocalPort() / 256,
                serverSocket.getLocalPort() % 256);

        Log.log(Level.FINER,Type.LOCAL, command);

        try {
            client.writeControl(command);
            client.printOutput(client.getOutput(), Level.INFO,Type.CONTROL);
        } catch (IOException e) {
            Log.log(Level.SEVERE,Type.LOCAL, "Error when writing/reading to/from control in port.");
            e.printStackTrace();
        }
    }
}
