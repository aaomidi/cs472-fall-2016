package com.aaomidi.ftpserver.util;



import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

public class Log {
    private static final Set<File> files = new HashSet<>();
    private static final Level logLevel = Level.FINEST;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
    private static final List<String> logMessages = new CopyOnWriteArrayList<>();

    public static void addFileHandler(File file) {
        files.add(file);
        writeToFile();
    }

    public static void log(Level level, Type type, Object msg, Object... format) {
        if (logLevel.intValue() > level.intValue()) {
            return;
        }
        String m = null;
        try {
            m = String.format("%s - %s - %s - %s", sdf.format(new Date()), level.getName(), type.toString(), String.format(msg.toString(), format));
            print(m);
        } catch (Exception ex) {
            m = String.format("%s - %s - %s - %s", sdf.format(new Date()), level.getName(), type.toString(), msg.toString());
            System.out.println(m);
        }
        logMessages.add(m);
    }

    public static void print(Object msg, Object... format) {
        System.out.printf(msg.toString(), format);
        System.out.println("");
    }

    private static void writeToFile() {
        new Thread(() -> {
            while (true) {
                if (logMessages.isEmpty()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                for (File file : files) {
                    try {
                        FileWriter writer = new FileWriter(file, true);
                        for (String s : logMessages) {
                            writer.write(s + "\n");
                        }
                        writer.flush();
                        writer.close();
                    } catch (IOException e) {
                        throw new RuntimeException("Can not log to: " + file.getPath());
                    }
                }
                logMessages.clear();

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // Exit
                }
            }
        }).start();
    }
}
