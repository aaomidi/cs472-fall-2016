package com.aaomidi.ftpclient.engine;

import com.aaomidi.ftpclient.engine.command.FTPCommand;
import com.aaomidi.ftpclient.engine.command.commands.*;
import com.aaomidi.ftpclient.engine.lang.FTPRegex;
import com.aaomidi.ftpclient.engine.lang.StatusCodes;
import com.aaomidi.ftpclient.engine.lang.Type;
import com.aaomidi.ftpclient.util.Log;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.net.ssl.SSLContext;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.regex.Matcher;

@RequiredArgsConstructor
public class FTPClient {
    private final String hostname;
    private final Integer port;
    @Getter
    private final FTPMode mode;
    private final boolean ssl;

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
    private Socket passiveSocket;

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
    public void connect() throws UnknownHostException, Exception {
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

        printOutput(getSocketOutput(controlSocket, Integer.MAX_VALUE), Level.INFO, Type.CONTROL);
        Log.log(Level.INFO, Type.LOCAL, "You've connected to the server. Type 'help' to see all the commands you can do, or do login to start logging in.");
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

    /**
     * Method that blocks the thread and starts a connection.
     */
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
                    } else {
                        cmd.execute(split[0], args);
                        if (cmd.getName().equalsIgnoreCase("quit")) {
                            return;
                        }
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
     * Creates a PASV connection to server.
     *
     * @throws IOException
     */
    public void createPassiveDataConnection() throws IOException {
        /*
         * Grammar:
         * PASV <CRLF>
         */

        String command = "PASV";
        writeControl(command);

        List<String> output = getOutput();
        String line = output.get(0);
        if (line == null) {
            Log.log(Level.SEVERE, Type.LOCAL, "Can not enter passive mode. Entry was null.");
            return;
        }

        System.out.println(line);
        int statusCode = StatusCodes.getStatusCodeFromString(line);

        if (statusCode != 227) {
            Log.log(Level.SEVERE, Type.LOCAL, "Can not enter passive mode. Status not 227");
            return;
        }

        Log.log(Level.FINEST, Type.LOCAL, "Response from server: " + line);

        Matcher matcher = FTPRegex.PASSIVE_PORT.matcher(line);
        if (!matcher.matches()) {
            return;
        }
        int port;
        try {
            port = Short.valueOf(matcher.group(5)) * 256 + Short.valueOf(matcher.group(6));
        } catch (Exception ex) {
            Log.log(Level.SEVERE, Type.LOCAL, "Issue with response from the server.");
            return;
        }

        passiveSocket = new Socket(controlSocket.getInetAddress(), port);
        passiveSocket.setSoTimeout(1000);
        listenToData();
    }

    /**
     * Creates an active data connection at a specific port
     *
     * @param port
     * @throws IOException
     */
    public void createActiveDataConnection(short port) throws IOException {
        /*
         * Grammar:
         * PORT <host1>,<host2>,<host3>,<host4>,<port1>,<port2>
         * Where each argument is 8 bits
         */

        if (port < 0) {
            Log.log(Level.FINE, Type.LOCAL, "A port less than 0 was entered. We're just going to assume 0 was meant and go along with that.");
            port = 0;
        }

        activeDataServerSocket = new ServerSocket(port);
        activeDataServerSocket.setSoTimeout(500);
        listenToData();

        Socket clientSocket = this.getControlSocket();
        ServerSocket serverSocket = this.getActiveDataServerSocket();
        byte[] addr = clientSocket.getLocalAddress().getAddress();

        String command = String.format("PORT %d,%d,%d,%d,%d,%d",
                (int) addr[0] & 0xFF,
                (int) addr[1] & 0xFF,
                (int) addr[2] & 0xFF,
                (int) addr[3] & 0xFF,
                serverSocket.getLocalPort() / 256,
                serverSocket.getLocalPort() % 256);

        Log.log(Level.FINER, Type.LOCAL, command);

        this.writeControl(command);
        this.printOutput(this.getOutput(), Level.INFO, Type.CONTROL);
    }

    /**
     * Call when expecting a data connection. It locks and unlocks a reentrantLock lock.
     */
    private void listenToData() {
        new Thread(() -> {
            try {
                dataLock.lock();
                Socket dataSocket = null;
                switch (mode) {
                    case EACTIVE:
                    case ACTIVE: {
                        if (activeDataServerSocket.isClosed()) {
                            dataLock.unlock();
                            return;
                        }
                        dataSocket = activeDataServerSocket.accept();
                        // Is someone else trying to connect to our server?!!
                        if (!dataSocket.getInetAddress().equals(controlSocket.getInetAddress())) {
                            dataLock.unlock();
                            Log.log(Level.SEVERE, Type.LOCAL, "UNKNOWN CONNECTION. Possible hack attempt blocked.");
                            return;
                        }
                        break;
                    }
                    case EPASSIVE:
                    case PASSIVE: {
                        dataSocket = passiveSocket;

                        break;
                    }

                }

                if (isFileTransfer()) {
                    setFileTransfer(false);

                    Log.log(Level.INFO, Type.DATA, "Transferring file %s.", fileName);
                    InputStream inputStream = dataSocket.getInputStream();
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
                    printOutput(getSocketOutput(dataSocket, Integer.MAX_VALUE), Level.INFO, Type.DATA);
                }
                activeDataServerSocket.close();
                dataLock.unlock();
            } catch (Exception ex) {
                return;
            } finally {
                try {
                    dataLock.unlock();
                } catch (Exception ex) {
                    //ignore
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
                    //Log.log(Level.FINEST, Type.LOCAL, "Sending keep alive.");
                    //writeControl("NOOP");
                    //getOutput();
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
    private void createConnection() throws Exception {
        if (ssl) {
            controlSocket = SSLContext.getDefault().getSocketFactory().createSocket(inetAddress, port);
        } else {
            controlSocket = new Socket(inetAddress, port);
        }
        controlSocket.setSoTimeout(500);
    }

    /**
     * Register our commands.
     */
    private void registerCommands() {
        registerCommand(new HomeCommand(this));
        registerCommand(new CdCommand(this));
        registerCommand(new HelpCommand(this));
        registerCommand(new LsCommand(this));
        registerCommand(new LoginCommand(this));
        registerCommand(new PwdCommand(this));
        registerCommand(new QuitCommand(this));
        registerCommand(new GetCommand(this));
    }

    /**
     * Registers a specific command. Registers its aliases for faster lookup.
     *
     * @param command
     */
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

    /**
     * Gets the output from a socket with a line count if applicable.
     *
     * @param socket
     * @param count
     * @return
     * @throws IOException
     */
    public List<String> getSocketOutput(Socket socket, int count) throws IOException {
        List<String> output = new LinkedList<>();
        InputStream is = socket.getInputStream();

        try {
            String msg;
            byte[] buffer = new byte[1024];
            int read = 99;
            int i = 0;
            while (!socket.isClosed() && socket.isConnected() && read > 0 && i++ != count) {
                read = is.read(buffer);
                if (read == -1) {
                    socket.close();
                    return output;
                }
                msg = new String(buffer, 0, read);
                output.add(msg);
                if(msg.contains("\r\n")){
                    break;
                }
            }

            Log.log(Level.FINE, Type.LOCAL, "Socket disconnected.");
        } catch (SocketTimeoutException ex) {
            return output;
        }
        return output;
    }

    /**
     * Prepares EPRT connection.
     *
     * @param port
     * @throws IOException
     */
    public void createExtendedActiveDataConnection(short port) throws IOException {

        if (port < 0) {
            Log.log(Level.FINE, Type.LOCAL, "A port less than 0 was entered. We're just going to assume 0 was meant and go along with that.");
            port = 0;
        }

        activeDataServerSocket = new ServerSocket(port);
        activeDataServerSocket.setSoTimeout(500);
        listenToData();

        Socket clientSocket = this.getControlSocket();
        ServerSocket serverSocket = this.getActiveDataServerSocket();
        InetAddress addr = clientSocket.getLocalAddress();

        int ip = 1;
        if (clientSocket.getInetAddress() instanceof Inet6Address) {
            ip = 2;
        }

        String command = String.format("EPRT |%d|%s|%d|",
                ip,
                addr.getHostAddress(),
                serverSocket.getLocalPort());

        Log.log(Level.FINER, Type.LOCAL, command);

        this.writeControl(command);
        this.printOutput(this.getOutput(), Level.INFO, Type.CONTROL);
    }

    /**
     * Prepares EPSV connection.
     *
     * @throws IOException
     */
    public void createExtendedPassiveDataConnection() throws IOException {
        /*
         * Grammar:
         * EPSV <CRLF>
         */

        String command = "EPSV 2";
        writeControl(command);

        List<String> output = getOutput();
        //printOutput(output, Level.FINER, Type.CONTROL);
        String line = output.get(0);
        if (line == null) {
            Log.log(Level.SEVERE, Type.LOCAL, "Can not enter passive mode.");
            return;
        }

        int statusCode = StatusCodes.getStatusCodeFromString(line);

        if (statusCode != 229) {
            Log.log(Level.SEVERE, Type.LOCAL, "Can not enter passive mode.");
            return;
        }

        Log.log(Level.FINEST, Type.LOCAL, "Response from server: " + line);

        Matcher matcher = FTPRegex.EPASSIVE_PORT.matcher(line);
        if (!matcher.find()) {
            System.out.println("Matcher didn't match.");
            return;
        }
        int port;
        try {
            port = Integer.valueOf(matcher.group(3));
        } catch (NumberFormatException ex) {
            Log.log(Level.SEVERE, Type.LOCAL, "Issue with response from the server.");
            return;
        }

        passiveSocket = new Socket(controlSocket.getInetAddress(), port);
        listenToData();
    }

    /**
     * Prepares the data connection depending on the mode we're in.
     *
     * @throws IOException
     */
    public void prepareConnection() throws IOException {
        switch (this.mode) {
            case ACTIVE:
                this.createActiveDataConnection((short) 0);
                break;
            case PASSIVE:
                this.createPassiveDataConnection();
                break;
            case EACTIVE:
                this.createExtendedActiveDataConnection((short) 0);
                break;
            case EPASSIVE:
                this.createExtendedPassiveDataConnection();
                break;
        }
    }

    @ToString
    public static class FTPClientBuilder {
        private String hostname;
        private int port;
        @Getter
        private FTPMode mode;
        @Getter
        private boolean ssl;

        public static FTPClientBuilder builder() {
            return new FTPClientBuilder();
        }

        public FTPClientBuilder() {

        }

        public FTPClientBuilder hostname(String hostname) {
            this.hostname = hostname;
            return this;
        }

        public FTPClientBuilder port(int port) {
            this.port = port;
            return this;
        }

        public FTPClient build() {
            return new FTPClient(hostname, port, mode, ssl);
        }

        public FTPClientBuilder setSSL(boolean ssl) {
            this.ssl = ssl;
            return this;
        }

        public FTPClientBuilder setMode(FTPMode mode) {
            this.mode = mode;
            return this;
        }
    }

    /**
     * Gets a list of strings as output.
     *
     * @return
     * @throws IOException
     */
    public List<String> getOutput() throws IOException {
        return getSocketOutput(controlSocket, 99);
    }

}
