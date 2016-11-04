package com.aaomidi.ftpserver.config;

import com.aaomidi.ftpserver.config.auth.AuthConfig;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import static com.aaomidi.ftpserver.Main.GSON;

@RequiredArgsConstructor
public class Config {
    private transient final static Gson gson = GSON;


    private final String authConfigLocation;

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

    public File getAuthConfig() {
        return new File(authConfigLocation);
    }
}
