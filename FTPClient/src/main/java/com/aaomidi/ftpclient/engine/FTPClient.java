package com.aaomidi.ftpclient.engine;

import com.aaomidi.ftpclient.engine.command.FTPCommand;
import com.aaomidi.ftpclient.engine.command.commands.*;
import com.aaomidi.ftpclient.engine.lang.Type;
import com.aaomidi.ftpclient.util.Log;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

@RequiredArgsConstructor
public class FTPClient {
    private final String hostname;
    private final Short port;
    @Getter
    private final boolean isActiveMode;

    @Getter
    private final HashMap<String, FTPCommand> commands = new HashMap<>();
    @Getter

    private final ReentrantLock dataLock = new ReentrantLock(true);
    @Getter
    @Setter
    private boolean fileTransfer = false;
    @Getter
    @Setter
    private String fileName;

    private InetAddress inetAddress = null;
    @Getter
    private Socket controlSocket;

    @Getter
    private ServerSocket activeDataServerSocket;

    private InputStream controlSocketInputStream;
    private OutputStream controlSocketOutputStream;

    private PrintWriter controlWriter;


    /**
     * Opens a connection to the FTP server.
     *
     * @throws UnknownHostException If the host was not found.
     * @throws IOException          Throws if file is broken or not.
     */
    public void connect() throws UnknownHostException, IOException {
        List<String> output;

        Log.log(Level.FINE, Type.LOCAL, "Registering commands.");
        this.registerCommands();
        Log.log(Level.FINE, Type.LOCAL, "\tRegistered.");

        inetAddress = InetAddress.getByName(hostname);

        Log.log(Level.FINE, Type.LOCAL, "Creating the connection to the server.");
        createConnection();
        Log.log(Level.INFO, Type.LOCAL, "\tSuccessfully connected to %s:%d.", hostname, port);

        Log.log(Level.FINE, Type.LOCAL, "Creating the streams of the control socket.");
        controlSocketInputStream = controlSocket.getInputStream();
        controlSocketOutputStream = controlSocket.getOutputStream();
        Log.log(Level.FINE, Type.LOCAL, "\tSuccessfully created the streams.");

        controlWriter = new PrintWriter(new OutputStreamWriter(controlSocketOutputStream), true);

        printOutput(getOutput(), Level.INFO, Type.CONTROL);
        keepAlive();

    }

    /**
     * Prints a list of strings.
     *
     * @param output The list of strings.
     * @param level  The log level.
     */
    public void printOutput(List<String> output, Level level, Type type) {
        output.forEach(s -> Log.log(level, type, s));
    }

    /**
     * "
     * Writes a message to the control socket.
     *
     * @param msg Message to write.
     * @throws IOException
     */
    public void writeControl(String msg) throws IOException {
        controlWriter.println(msg);
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            try {
                String command = scanner.nextLine();
                if (!command.equals("")) {
                    String[] split = command.split(" ");
                    List<String> args = new ArrayList<>(split.length - 1);

                    for (int i = 1; i < split.length; i++) {
                        String s = split[i];
                        args.add(s);
                    }

                    FTPCommand cmd = commands.get(split[0].toLowerCase());

                    if (cmd == null) {
                        Log.log(Level.INFO, Type.LOCAL, "Command not recognized.");
                        writeControl(command);
                        printOutput(getOutput(), Level.INFO, Type.CONTROL);
                    } else {
                        cmd.execute(split[0], args);
                    }
                }

                Thread.sleep(100);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Creates an active data connection at a specific port
     *
     * @param port
     * @throws IOException
     */
    public void createActiveDataConnection(short port) throws IOException {
        if (activeDataServerSocket != null && !activeDataServerSocket.isClosed() && activeDataServerSocket.isBound()) {
            throw new IOException("Data connection already exists.");
        }

        activeDataServerSocket = new ServerSocket(port);
        activeDataServerSocket.setSoTimeout(1100);
        listenToData();
    }

    private void listenToData() {
        new Thread(() -> {
            while (true) {
                try {
                    dataLock.lock();

                    if (activeDataServerSocket.isClosed()) {
                        dataLock.unlock();
                        Thread.sleep(1000);
                        continue;
                    }

                    Socket activeDataSocket = activeDataServerSocket.accept();

                    if (isFileTransfer()) {
                        setFileTransfer(false);

                        Log.log(Level.INFO, Type.DATA, "Transferring file %s.", fileName);
                        InputStream inputStream = activeDataSocket.getInputStream();
                        File result = new File(fileName);
                        result.createNewFile();
                        OutputStream outputStream = new FileOutputStream(result);

                        int read = 0;
                        byte[] bytes = new byte[1024];

                        while ((read = inputStream.read(bytes)) != -1) {
                            outputStream.write(bytes, 0, read);
                        }
                        outputStream.flush();
                        outputStream.close();

                        Log.log(Level.INFO, Type.DATA, "\tTransfer of %s completed.", fileName);
                    } else {
                        printOutput(getSocketOutput(activeDataSocket), Level.INFO, Type.DATA);
                    }
                    activeDataServerSocket.close();
                    dataLock.unlock();
                    Thread.sleep(1000);
                } catch (Exception ex) {
                    continue;
                } finally {
                    try {
                        dataLock.unlock();
                    } catch (Exception ex) {
                        //ignore
                    }
                }

            }

        }).start();
    }

    /**
     * Sends a keep alive every 20 seconds. This keeps the connection alive.
     */
    private void keepAlive() {
        new Thread(() -> {
            while (true) {
                try {
                    Log.log(Level.FINEST, Type.LOCAL, "Sending keep alive.");
                    writeControl("NOOP");
                    getOutput();
                    Thread.sleep(20000);
                } catch (Exception e) {
                    Log.log(Level.SEVERE, Type.LOCAL, "ERROR WHEN SENDING KEEP ALIVE.");
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
        controlSocket.setSoTimeout(250);
    }

    private void registerCommands() {
        registerCommand(new CdupCommand(this));
        registerCommand(new CwdCommand(this));
        registerCommand(new HelpCommand(this));
        registerCommand(new ListCommand(this));
        registerCommand(new LoginCommand(this));
        registerCommand(new PortCommand(this));
        registerCommand(new PwdCommand(this));
        registerCommand(new QuitCommand(this));
        registerCommand(new RetrCommand(this));
    }

    private void registerCommand(FTPCommand command) {
        for (String s : command.getAliases()) {
            this.commands.put(s.toLowerCase(), command);
        }
        this.commands.put(command.getName().toLowerCase(), command);
    }

    /**
     * Closes all FTP related connections.
     */
    public void close() {
        try {
            controlSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> getSocketOutput(Socket socket) throws IOException {
        List<String> output = new LinkedList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        try {
            String msg;
            while ((msg = reader.readLine()) != null && !msg.equals("")) {
                output.add(msg);
            }
        } catch (SocketTimeoutException ex) {
            return output;
        }
        return output;
    }

    @ToString
    public static class FTPClientBuilder {
        private String hostname;
        private short port;
        private boolean isActive;

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

        public FTPClientBuilder isActive(boolean x) {
            this.isActive = x;
            return this;
        }

        public FTPClient build() {
            return new FTPClient(hostname, port, isActive);
        }
    }

    /**
     * Gets a list of strings as output.
     *
     * @return
     * @throws IOException
     */
    public List<String> getOutput() throws IOException {
        return getSocketOutput(controlSocket);
    }

}
