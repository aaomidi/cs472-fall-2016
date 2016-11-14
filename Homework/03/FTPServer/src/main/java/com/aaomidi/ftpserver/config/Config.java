package com.aaomidi.ftpserver.config;

import com.aaomidi.ftpserver.config.auth.AuthConfig;
import com.aaomidi.ftpserver.engine.command.ftpcommands.EPRTCommand;
import com.aaomidi.ftpserver.engine.command.ftpcommands.EPSVCommand;
import com.aaomidi.ftpserver.engine.command.ftpcommands.PASVCommand;
import com.aaomidi.ftpserver.engine.command.ftpcommands.PORTCommand;
import com.aaomidi.ftpserver.util.Log;
import com.aaomidi.ftpserver.util.Type;
import com.google.gson.Gson;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;

import static com.aaomidi.ftpserver.Main.GSON;

@RequiredArgsConstructor
public class Config {
    private transient final static Gson gson = GSON;

    private final String authFile;
    @Getter
    private final String logDirectory;
    @Getter
    private final int numLogFiles;
    @Getter
    private final String portMode;
    @Getter
    private final String pasvMode;

    public void save(File file) throws IOException {
        FileWriter writer = new FileWriter(file);
        gson.toJson(this, Config.class, writer);
        writer.flush();
    }

    public AuthConfig readAuthConfig() throws IOException {
        File file = getAuthConfig();
        if (!file.exists()) {
            if (!file.createNewFile()) {
                throw new IOException("AuthConfig not created.");
            }
            AuthConfig authConfig = new AuthConfig();
            authConfig.save(file);
            return authConfig;
        }

        return gson.fromJson(new FileReader(file), AuthConfig.class);
    }

    public void checkModes() {
        if (portMode.equalsIgnoreCase("no") && pasvMode.equalsIgnoreCase("no")) {
            Log.log(Level.SEVERE, Type.LOCAL, "Both modes can not be disabled.");
            System.exit(-1);
        }
        if (portMode.equalsIgnoreCase("no")) {
            PORTCommand.ENABLED = false;
            EPRTCommand.ENABLED = false;
        }

        if (pasvMode.equalsIgnoreCase("no")) {
            PASVCommand.ENABLED = false;
            EPSVCommand.ENABLED = false;
        }
    }

    public File getAuthConfig() {
        return new File(authFile);
    }
}
