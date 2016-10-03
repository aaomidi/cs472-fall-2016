package com.aaomidi.ftpclient.engine;

import com.aaomidi.ftpclient.engine.command.FTPCommand;
import com.aaomidi.ftpclient.engine.command.commands.QuitCommand;
import com.aaomidi.ftpclient.util.Log;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;

@RequiredArgsConstructor
public class FTPClient {
    private final String hostname;
    private final Short port;
    private final String username;
    private final String password;

    @Getter
    private final HashMap<String, FTPCommand> commands = new HashMap<>();

    private InetAddress inetAddress = null;

    private Socket controlSocket;

    private InputStream controlSocketInputStream;
    private OutputStream controlSocketOutputStream;

    private BufferedReader controlReader;
    private PrintWriter controlWriter;

    /**
     * Opens a connection to the FTP server.
     *
     * @throws UnknownHostException If the host was not found.
     * @throws IOException          Throws if file is broken or not.
     */
    public void connect() throws UnknownHostException, IOException {
        List<String> output;

        Log.log(Level.FINE, "Registering commands.");
        this.registerCommands();
        Log.log(Level.FINE, "\tRegistered.");

        inetAddress = InetAddress.getByName(hostname);

        Log.log(Level.FINE, "Creating the connection to the server.");
        createConnection();
        Log.log(Level.INFO, "\tSuccessfully connected to %s:%d.", hostname, port);

        Log.log(Level.FINE, "Creating the streams of the control socket.");
        controlSocketInputStream = controlSocket.getInputStream();
        controlSocketOutputStream = controlSocket.getOutputStream();
        Log.log(Level.FINE, "\tSuccessfully created the streams.");

        controlReader = new BufferedReader(new InputStreamReader(controlSocketInputStream));
        controlWriter = new PrintWriter(new OutputStreamWriter(controlSocketOutputStream), true);

        printOutput(getOutput(), Level.INFO);

        /* Start Authentication */
        Log.log(Level.FINE, "Authenticating %s.", username);

        writeControl(String.format("USER %s%n", username));
        printOutput(getOutput(), Level.INFO);

        PrintWriter writer;

        writeControl(String.format("PASS %s%n", password));
        printOutput(getOutput(), Level.INFO);
        /* End Authentication */

        keepAlive();

    }

    /**
     * Prints a list of strings.
     *
     * @param output The list of strings.
     * @param level  The log level.
     */
    private void printOutput(List<String> output, Level level) {
        output.forEach(s -> Log.log(level, s));
    }

    /**
     * "
     * Writes a message to the control socket.
     *
     * @param msg Message to write.
     * @throws IOException
     */
    private void writeControl(String msg) throws IOException {
        controlWriter.println(msg);
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            try {
                String command = scanner.nextLine().toLowerCase();
                FTPCommand cmd = commands.get(command);
                if (cmd == null) {
                    Log.log(Level.INFO, "Command not recognized.");
                    break;
                }
                cmd.execute();

                Thread.sleep(100);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Sends a keep alive every 20 seconds. This keeps the connection alive.
     */
    private void keepAlive() {
        new Thread(() -> {
            while (true) {
                try {
                    Log.log(Level.FINEST, "Sending keep alive.");
                    writeControl("NOOP");
                    getOutput();
                    Thread.sleep(20000);
                } catch (Exception e) {
                    Log.log(Level.SEVERE, "ERROR WHEN SENDING KEEP ALIVE.");
                    System.exit(-1);
                }
            }
        }).start();
    }

    /**
     * Creates the actual socket connections to the ftp server.
     *
     * @throws IOException
     */
    private void createConnection() throws IOException {
        controlSocket = new Socket(inetAddress, port);
        controlSocket.setSoTimeout(150);
    }

    private void registerCommands() {
        registerCommand(new QuitCommand(this));
    }

    private void registerCommand(FTPCommand command) {
        for (String s : command.getAliases()) {
            this.commands.put(s.toLowerCase(), command);
        }
        this.commands.put(command.getName().toLowerCase(), command);
    }

    public void close() {
        try {
            controlSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @ToString
    public static class FTPClientBuilder {
        private String hostname;
        private short port;

        private String username;
        private String password;

        public static FTPClientBuilder builder() {
            return new FTPClientBuilder();
        }

        public FTPClientBuilder() {

        }

        public FTPClientBuilder hostname(String hostname) {
            this.hostname = hostname;
            return this;
        }

        public FTPClientBuilder port(short port) {
            this.port = port;
            return this;
        }


        public FTPClientBuilder username(String username) {
            this.username = username;
            return this;
        }

        public FTPClientBuilder password(String password) {
            this.password = password;
            return this;
        }

        public FTPClient build() {
            return new FTPClient(hostname, port, username, password);
        }
    }

    /**
     * Gets a list of strings as output.
     *
     * @return
     * @throws IOException
     */
    private List<String> getOutput() throws IOException {
        List<String> output = new LinkedList<>();
        try {
            String msg;
            while ((msg = controlReader.readLine()) != null && !msg.equals("")) {
                output.add(msg);
            }
        } catch (SocketTimeoutException ex) {
            return output;
        }
        return output;
    }

}
