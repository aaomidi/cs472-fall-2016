package com.aaomidi.ftpserver.engine;

import com.aaomidi.ftpserver.config.auth.Auth;
import com.aaomidi.ftpserver.engine.command.FTPCommand;
import com.aaomidi.ftpserver.util.Log;
import com.aaomidi.ftpserver.util.Type;
import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

public class FTPConnection {
    private final FTPServer ftpServer;
    @Getter
    private final Socket controlSocket;
    private PrintWriter writer;
    @Getter
    private String stateInformation = "";
    private FTPState state = FTPState.NOT_AUTHENTICATED;
    @Getter
    @Setter
    private File currentWorkingDirectory;

    @Getter
    @Setter
    private ServerSocket serverDataSocket;
    @Getter
    @Setter
    private Socket dataSocket;

    @Getter
    @Setter
    private DataSocketType dataSocketType = DataSocketType.NONE;

    public FTPConnection(FTPServer ftpServer, Socket controlSocket) {
        this.ftpServer = ftpServer;
        this.controlSocket = controlSocket;

        Blacklist bl = ftpServer.getBlacklist().get(controlSocket.getInetAddress());
        if (bl == null) {
            bl = new Blacklist();
            ftpServer.getBlacklist().put(controlSocket.getInetAddress(), bl);
        }
        if (!bl.canWork()) {

            try {
                writeToControl("503 Bye.");
            } catch (Exception e) {
            }
            return;
        }

        Log.log(Level.INFO, Type.LOCAL, "Incoming connection from: %s:%d", controlSocket.getInetAddress().getHostAddress(), controlSocket.getPort());
        currentWorkingDirectory = new File(System.getProperty("user.dir"));


        try {
            controlSocket.setSoTimeout(100);
        } catch (Exception e) {
            Log.log(Level.SEVERE, Type.LOCAL, e.getMessage());
        }

        try {
            writer = new PrintWriter(new OutputStreamWriter(controlSocket.getOutputStream()), true);
        } catch (IOException e) {
            Log.log(Level.SEVERE, Type.LOCAL, e.getMessage());
        }
        try {
            writeToControl("220 Welcome to my FTP :)");
        } catch (Exception e) {
            e.printStackTrace();
        }
        listen();

    }

    private void listen() {
        new Thread(() -> {
            while (true) {
                if (state == FTPState.QUIT)
                    return;
                try {
                    if (controlSocket.isClosed() || !controlSocket.isConnected()) {
                        return;
                    }
                    List<String> message = getSocketOutput(controlSocket, 1);

                    if (message == null || message.isEmpty()) continue;
                    String input = message.get(0);
                    if (input == null) continue;


                    manageInput(input);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void manageInput(String input) throws Exception {
        if (state == FTPState.QUIT)
            return;
        input = input.trim();
        Log.log(Level.INFO, Type.LOCAL, "State: %s, State info: %s", state, stateInformation);
        Log.log(Level.INFO, Type.CONTROL, input);

        String[] split = input.split(" ");
        List<String> args = new ArrayList<>(split.length - 1);

        args.addAll(Arrays.asList(split).subList(1, split.length));

        FTPCommand ftpCommand = ftpServer.getCommands().get(split[0].toLowerCase());
        if (ftpCommand == null) {
            Log.log(Level.INFO, Type.LOCAL, "Command not recognized.");
            writeToControl("202 Not implemented.");
        } else {
            if (ftpCommand.getRequiredState() != null && ftpCommand.getRequiredState() != this.state) {
                if (state == FTPState.LOGGING_IN) {
                    state = FTPState.NOT_AUTHENTICATED;
                }
                writeToControl("503 Bad sequence of commands");
            } else {
                writeToControl(ftpCommand.execute(this, split[0], args));
            }
        }
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void writeToControl(String message) throws Exception {
        if (message == null) return;
        if (state == FTPState.QUIT)
            return;

        Log.log(Level.INFO, Type.CONTROL, "Writing to control: %s", message);
        writer.print(message + "\r\n");
        writer.flush();
    }

    /**
     * Gets the output from a socket with a line count if applicable.
     *
     * @param socket
     * @param count
     * @return
     * @throws IOException
     */
    private List<String> getSocketOutput(Socket socket, int count) throws IOException {
        List<String> output = new LinkedList<>();
        InputStream is = socket.getInputStream();

        try {
            String msg;
            byte[] buffer = new byte[1024];
            int read = 99;
            int i = 0;
            while (read > 0 && i++ != count) {
                read = is.read(buffer);
                if (read == -1) {
                    socket.close();
                    return output;
                }
                msg = new String(buffer, 0, read);
                output.add(msg);
            }

            Log.log(Level.FINE, Type.LOCAL, "Socket disconnected.");
        } catch (SocketTimeoutException ex) {
            return output;
        } catch (SocketException ex) {
            controlSocket.close();
        }
        return output;
    }

    public Socket createActiveDataConnection(int port) throws Exception {
        dataSocket = new Socket(controlSocket.getInetAddress(), port);
        setDataSocketType(DataSocketType.ACTIVE);

        return dataSocket;
    }

    public ServerSocket createPassiveDataConnection() throws Exception {
        serverDataSocket = new ServerSocket(0);
        serverDataSocket.setSoTimeout(1000);
        setDataSocketType(DataSocketType.PASSIVE);

        return serverDataSocket;
    }

    public void listenToPassiveDataConnection() {
        new Thread(() -> {
            try {
                this.changeState(FTPState.DATA_SOCKET);
                dataSocket = serverDataSocket.accept();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    public boolean writeToDataSocketAndClose(String message) {
        if (dataSocket == null) return false;
        if (message == null) return false;
        if (dataSocket.isClosed()) {
            return false;
        }
        message = message + "\r\n";
        try {
            PrintWriter pw = new PrintWriter(dataSocket.getOutputStream());
            pw.write(message);
            pw.flush();
            dataSocket.close();
            if (serverDataSocket != null && !serverDataSocket.isClosed()) {
                serverDataSocket.close();
            }
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean writeToDataSocketAndClose(byte[] file) {
        if (dataSocket == null) return false;
        if (file == null) return false;
        if (dataSocket.isClosed()) {
            return false;
        }
        try {
            dataSocket.getOutputStream().write(file);
            dataSocket.getOutputStream().flush();
            dataSocket.close();
            if (serverDataSocket != null && !serverDataSocket.isClosed()) {
                serverDataSocket.close();
            }
            return true;
        } catch (Exception ex) {
            return false;
        }
    }


    public void changeState(FTPState state) {
        this.state = state;
    }

    public void changeInformation(String info) {
        this.stateInformation = info;
    }

    public Auth getAuthByUsername(String username) {
        return ftpServer.getAuthConfig().getAuths().get(username);
    }

    public void quit() {
        try {
            controlSocket.close();
            dataSocket.close();
            changeState(FTPState.QUIT);
        } catch (Exception ex) {
            //ignore.
        }
    }
}
