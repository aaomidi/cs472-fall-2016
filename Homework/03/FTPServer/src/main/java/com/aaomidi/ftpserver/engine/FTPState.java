package com.aaomidi.ftpserver.engine;

public enum FTPState {
    NOT_AUTHENTICATED,
    LOGGING_IN,
    AUTHENTICATED,
    DATA_SOCKET,
    QUIT
}
