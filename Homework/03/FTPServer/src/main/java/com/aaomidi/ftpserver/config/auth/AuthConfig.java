package com.aaomidi.ftpserver.config.auth;

import com.aaomidi.ftpserver.config.Config;
import com.google.gson.Gson;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.aaomidi.ftpserver.Main.GSON;

@RequiredArgsConstructor
public class AuthConfig {
    private transient final static Gson gson = GSON;
    @Getter
    private final Map<String, Auth> auths = new HashMap<>();


    public void save(File file) throws IOException {
        FileWriter writer = new FileWriter(file);
        gson.toJson(this, AuthConfig.class, writer);
        writer.flush();
    }
}
