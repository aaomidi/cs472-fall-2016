package com.aaomidi.ftpclient.engine;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum FTPMode {
    ACTIVE("Active"),
    PASSIVE("Passive"),
    EACTIVE("Extended Active"),
    EPASSIVE("Extended Passive");
    @Getter
    private final String modeName;
}
